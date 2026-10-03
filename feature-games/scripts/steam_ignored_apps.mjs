// Lista gier zignorowanych na Steamie z nazwami, dla link_steam_ignored_apps.sql.
//
// Skąd userdata.json: zalogowany na Steamie otwierasz https://store.steampowered.com/dynamicstore/userdata/
// i zapisujesz stronę. Bierzemy z niej tylko rgIgnoredApps ({ appid: powód }; 0 to „nie interesuje mnie”,
// 2 to „grałem gdzie indziej”).
//
// Nazwy i typy daje IStoreBrowseService/GetItems - działa bez klucza API, w przeciwieństwie do
// IStoreService/GetAppList, a ISteamApps/GetAppList Valve usunął. Aplikacja zdjęta ze sklepu wraca bez
// nazwy i trafia do wyniku z name = null.
//
// Uruchomienie (wynik musi leżeć na D: - czyta go serwer PostgreSQL przez pg_read_file):
//   node steam_ignored_apps.mjs C:/Users/Kacper/Downloads/userdata.json D:/steam/ignored_apps.json

import { readFileSync, writeFileSync } from 'node:fs';

const [userdataPath, outputPath] = process.argv.slice(2);
if (!userdataPath || !outputPath) {
    console.error('użycie: node steam_ignored_apps.mjs <userdata.json> <wynik.json>');
    process.exit(1);
}

const ignored = JSON.parse(readFileSync(userdataPath, 'utf8')).rgIgnoredApps ?? {};
const appIds = Object.keys(ignored).map(Number);
const BATCH = 200;

const apps = [];
for (let i = 0; i < appIds.length; i += BATCH) {
    const batch = appIds.slice(i, i + BATCH);
    const input = {
        ids: batch.map(appid => ({ appid })),
        context: { language: 'english', country_code: 'PL' },
        data_request: {},
    };
    const url = 'https://api.steampowered.com/IStoreBrowseService/GetItems/v1/?input_json=' + encodeURIComponent(JSON.stringify(input));
    const response = await fetch(url);
    if (!response.ok) throw new Error(`GetItems: HTTP ${response.status} przy paczce od ${i}`);
    const items = (await response.json()).response.store_items ?? [];
    const byId = new Map(items.map(item => [item.appid ?? item.id, item]));
    for (const appid of batch) {
        const item = byId.get(appid);
        apps.push({ appid, name: item?.name ?? null, type: item?.type ?? null, reason: ignored[appid] });
    }
    console.log(`${Math.min(i + BATCH, appIds.length)} / ${appIds.length}`);
}

writeFileSync(outputPath, JSON.stringify(apps));
const withoutName = apps.filter(app => app.name === null).length;
console.log(`zapisano ${apps.length} aplikacji do ${outputPath}, bez nazwy: ${withoutName}`);
