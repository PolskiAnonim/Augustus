// Zbiera wszystkie serie z listy lektur NovelUpdates przez series finder. Samej strony listy nie da
// się użyć, gdy lista jest duża: serwer nie zdąży jej wyrenderować, Cloudflare ucina odpowiedź po
// 100 s i zostaje kilka tysięcy pierwszych wierszy.
//
// Uruchomienie: będąc zalogowanym otwórz dowolną stronę www.novelupdates.com i wklej całość w konsolę
// DevTools. Na końcu pobierze się JSON dla import_novelupdates_list.sql - przenieś go na dysk D:.
//
// Postęp zapisuje się po każdej stronie w IndexedDB strony (baza nu-list-<LIST>), więc zamknięcie
// karty czy przeglądarki niczego nie kasuje: żeby zatrzymać, zamknij kartę, żeby wznowić, wklej skrypt
// jeszcze raz. Wklejenie, gdy skrypt już działa (w tej albo innej karcie), niczego nie odpala drugi
// raz. Od zera: indexedDB.deleteDatabase('nu-list-2').
//
// Series finder zwraca na zapytanie najwyżej 100 stron po 25 wyników, dalsze strony powtarzają
// setną. Idziemy więc kursorem po dacie ostatniego wydania: sortujemy rosnąco, bierzemy do 100
// stron, a następne zapytanie zaczyna od najpóźniejszej daty z setnej strony - włącznie, powtórki
// odsiewa sid. Osobny przebieg dla każdego typu, bo typu nie ma w wierszu wyniku. Język jest,
// zapisujemy go tak, jak podaje NovelUpdates (cn, jp, kr, id...), a na enum mapuje dopiero SQL.
//
// Serie bez żadnego wydania mają zamiast daty N/A, a finder sortuje je po jakiejś ukrytej dacie, po
// której kursora ustawić się nie da, więc przebieg po datach część z nich gubi. Dlatego druga faza bierze osobno serie z najwyżej jednym rozdziałem (filtr "max 0" finder ignoruje),
// z podziałem na typ i język, sortowane po tytule. Tytuł jest unikalny, więc stronicowanie jest
// stabilne; grupę większą niż 2500 bierzemy od A i od Z, aż oba końce się spotkają.
//
// 429 przychodzi losowo, a nie od tempa, więc nie zwalniamy całego przebiegu - tylko ponawiamy
// stronę, która go dostała.

(async () => {
    const LIST = 2; // Trash
    const TYPES = {2444: 'WEB_NOVEL', 2443: 'LIGHT_NOVEL', 26874: 'PUBLISHED_NOVEL'};
    // Identyfikatory języków w finderze: CN, JP, KR, ID, PH, MY, TH, VN, KH.
    const LANGUAGES = [495, 496, 497, 9179, 9181, 9183, 9954, 9177, 18657];
    const PAGE_SIZE = 25;
    const MAX_PAGES = 100;
    const MAX_ATTEMPTS = 6;
    const DELAY_MS = 5400;

    const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
    const done = request => new Promise((resolve, reject) => {
        request.onsuccess = () => resolve(request.result);
        request.onerror = () => reject(request.error);
    });

    const openRequest = indexedDB.open(`nu-list-${LIST}`, 1);
    openRequest.onupgradeneeded = () => {
        openRequest.result.createObjectStore('series', {keyPath: 'sid'});
        openRequest.result.createObjectStore('chains');
    };
    const db = await done(openRequest);
    const read = (store, action) => done(action(db.transaction(store).objectStore(store)));

    // Wiersze i postęp łańcucha w jednej transakcji: po przerwie wracamy dokładnie za ostatnią
    // zapisaną stronę.
    const save = (rows, key, chain) => new Promise((resolve, reject) => {
        const transaction = db.transaction(['series', 'chains'], 'readwrite');
        for (const row of rows) transaction.objectStore('series').put(row);
        transaction.objectStore('chains').put(chain, key);
        transaction.oncomplete = resolve;
        transaction.onerror = () => reject(transaction.error);
    });

    const pageUrl = (nt, cursor, pg) => {
        const params = new URLSearchParams({sf: 1, nt, hd: LIST, mRLi: 'include'});
        if (cursor) {
            params.set('dt', cursor);
            params.set('mdt', 'min');
        }
        params.set('sort', 'sdate');
        params.set('order', 'asc');
        params.set('pg', pg);
        return '/series-finder/?' + params;
    };

    const noReleasesUrl = (nt, org, order, pg) =>
        '/series-finder/?' + new URLSearchParams({sf: 1, org, nt, rl: 1, mrl: 'max', hd: LIST, mRLi: 'include', sort: 'abc', order, pg});

    const parseRow = box => ({
        sid: box.querySelector('span[id^=sid]').id.slice(3),
        title: box.querySelector('.search_title a').textContent.trim(),
        org: [...box.querySelectorAll('span')].map(s => s.className.match(/^org([a-z]{2})$/)?.[1]).find(Boolean) ?? null,
        // Data w wierszu jest jako MM-DD-YYYY, filtr chce MM/DD/YYYY.
        date: box.querySelector('.search_stats')?.textContent.match(/(\d\d)-(\d\d)-(\d{4})/)?.slice(1).join('/') ?? null,
        stats: box.querySelector('.search_stats')?.textContent.replace(/\s+/g, ' ').trim() ?? '(brak statystyk)',
    });

    // MM/DD/YYYY -> YYYYMMDD, żeby daty dało się porównywać jako tekst.
    const sortKey = date => date.replace(/(\d\d)\/(\d\d)\/(\d{4})/, '$3$1$2');

    // Pusta strona bez "No results" to też odpowiedź zdławiona, tylko bez 429 - ponawiamy tak samo.
    const fetchPage = async url => {
        for (let attempt = 1; ; attempt++) {
            const response = await fetch(url);
            if (response.ok) {
                const doc = new DOMParser().parseFromString(await response.text(), 'text/html');
                const rows = [...doc.querySelectorAll('.search_main_box_nu')].map(parseRow);
                if (rows.length > 0 || doc.body.textContent.includes('No results')) return rows;
            }
            if (attempt === MAX_ATTEMPTS) {
                throw new Error(`[nu] ${url}: ${MAX_ATTEMPTS} nieudanych prób z rzędu, stoję - wklej skrypt ponownie, żeby wznowić`);
            }
            const waitMs = Number(response.headers.get('Retry-After')) * 1000 || 5000 * 3 ** (attempt - 1);
            console.log(`[nu] ${response.status} (próba ${attempt}), ponawiam za ${waitMs / 1000} s`);
            await sleep(waitMs);
        }
    };

    const scrape = async () => {
        const known = new Set(await read('series', store => store.getAllKeys()));
        const collect = (rows, type) => {
            const fresh = rows.filter(row => !known.has(row.sid))
                .map(row => ({sid: row.sid, title: row.title, org: row.org, type}));
            fresh.forEach(row => known.add(row.sid));
            return fresh;
        };

        for (const [nt, type] of Object.entries(TYPES)) {
            const chain = await read('chains', store => store.get(type)) ?? {cursor: null, pg: 1, previousSids: '', done: false};
            while (!chain.done) {
                const rows = await fetchPage(pageUrl(nt, chain.cursor, chain.pg));
                const fresh = collect(rows, type);
                console.log(`[nu] ${type} od ${chain.cursor ?? 'początku'} str. ${chain.pg}: ${rows.length} wierszy, ${fresh.length} nowych, razem ${known.size}`);

                const sids = rows.map(row => row.sid).join();
                if (rows.length < PAGE_SIZE || sids === chain.previousSids) {
                    chain.done = true;
                } else if (chain.pg < MAX_PAGES) {
                    chain.pg++;
                } else {
                    // Najpóźniejsza data ze strony, a nie z ostatniego wiersza - zdarzają się wiersze
                    // bez daty (N/A przy zerze rozdziałów) i jeden trafił kiedyś akurat na koniec setnej
                    // strony. Wypisujemy je, żeby było widać, co to za serie.
                    const undated = rows.filter(row => !row.date);
                    if (undated.length > 0) {
                        console.log(`[nu] ${type}: wiersze bez daty na granicy okna`, undated.map(row => `${row.sid} ${row.title} | ${row.stats}`));
                    }
                    const next = rows.map(row => row.date).filter(Boolean)
                        .sort((a, b) => sortKey(a).localeCompare(sortKey(b))).at(-1);
                    if (!next || next === chain.cursor) {
                        throw new Error(`[nu] ${type}: okno od ${chain.cursor} nie ma na setnej stronie daty późniejszej niż ${next}, kursor nie ma dokąd iść`);
                    }
                    chain.cursor = next;
                    chain.pg = 1;
                }
                chain.previousSids = sids;
                await save(fresh, type, chain);
                if (!chain.done) await sleep(DELAY_MS + Math.random() * 1000);
            }
        }

        for (const [nt, type] of Object.entries(TYPES)) {
            for (const org of LANGUAGES) {
                const key = `bez-wydań:${type}:${org}`;
                const chain = await read('chains', store => store.get(key)) ?? {order: 'asc', pg: 1, previousSids: '', ascSids: [], done: false};
                while (!chain.done) {
                    const rows = await fetchPage(noReleasesUrl(nt, org, chain.order, chain.pg));
                    const fresh = collect(rows, type);
                    console.log(`[nu] ${key} ${chain.order === 'asc' ? 'od A' : 'od Z'} str. ${chain.pg}: ${rows.length} wierszy, ${fresh.length} nowych, razem ${known.size}`);

                    const sids = rows.map(row => row.sid).join();
                    const met = chain.order === 'desc' && rows.some(row => chain.ascSids.includes(row.sid));
                    if (chain.order === 'asc') chain.ascSids.push(...rows.map(row => row.sid));
                    if (met || rows.length < PAGE_SIZE || sids === chain.previousSids) {
                        chain.done = true;
                    } else if (chain.pg < MAX_PAGES) {
                        chain.pg++;
                    } else if (chain.order === 'asc') {
                        chain.order = 'desc';
                        chain.pg = 1;
                    } else {
                        console.warn(`[nu] ${key}: ponad ${2 * PAGE_SIZE * MAX_PAGES} serii, środek alfabetu przepadł`);
                        chain.done = true;
                    }
                    chain.previousSids = sids;
                    await save(fresh, key, chain);
                    // Bez warunku: większość grup kończy się na jednej stronie i bez tej przerwy
                    // zapytania o kolejne szłyby jedno za drugim.
                    await sleep(DELAY_MS + Math.random() * 1000);
                }
            }
        }

        const series = await read('series', store => store.getAll());
        console.log(`[nu] gotowe: ${series.length} serii - porównaj z licznikiem na liście`);
        const link = document.createElement('a');
        link.href = URL.createObjectURL(new Blob([JSON.stringify(series)], {type: 'application/json'}));
        link.download = `novelupdates-list-${LIST}.json`;
        link.click();
    };

    await navigator.locks.request(`nu-list-${LIST}`, {ifAvailable: true}, lock =>
        lock ? scrape() : console.log('[nu] już działa w tej albo innej karcie - żeby zatrzymać, zamknij tamtą kartę'));
})();
