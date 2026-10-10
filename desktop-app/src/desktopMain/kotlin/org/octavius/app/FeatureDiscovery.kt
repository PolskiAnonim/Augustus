package org.octavius.app

import io.github.classgraph.ClassGraph
import org.octavius.contract.FeatureModule

/**
 * Znajduje featury na classpathie: każdy `object` pod `org.octavius`, który implementuje [FeatureModule].
 *
 * Listy featurów nie ma nigdzie. `settings.gradle.kts` dołącza do builda każdy katalog `feature-*`,
 * `desktop-app` zależy od wszystkich, a tutaj feature pojawia się sam. Feature, którego katalogu na danej
 * gałęzi nie ma, znika tak samo, bez zostawiania po sobie importu czy wpisu.
 *
 * @return Featury posortowane po [FeatureModule.order], przy remisie po nazwie.
 * @throws IllegalStateException gdy implementacja nie jest `object`em albo gdy schematy featurów się
 * nie zgadzają (patrz [FeatureModule.schema]).
 */
fun discoverFeatures(): List<FeatureModule> {
    val features = ClassGraph()
        .enableClassInfo()
        .acceptPackages("org.octavius")
        .scan()
        .use { result ->
            result.getClassesImplementing(FeatureModule::class.java.name)
                .filter { !it.isAbstract && !it.isInterface }
                .map { info ->
                    info.loadClass().kotlin.objectInstance as? FeatureModule
                        ?: error("${info.name} implementuje FeatureModule, ale nie jest objectem, więc nie ma go jak wziąć.")
                }
        }
        .sortedWith(compareBy({ it.order }, { it.name }))

    for ((schema, owners) in features.filter { it.schema != null }.groupBy { it.schema }) {
        val names = owners.joinToString { it.name }
        check(schema != PUBLIC_SCHEMA) { "$names: schemat $PUBLIC_SCHEMA nie należy do żadnego featura." }
        check(owners.size == 1) { "Schemat $schema deklaruje kilka featurów naraz: $names." }
    }

    println("Featury: ${features.joinToString { it.name }}")
    return features
}
