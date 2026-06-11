plugins {
    alias(libs.plugins.loom)

    id("gooeylibs.loader-conventions")
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)

    implementation(libs.adventure.api)
    implementation(libs.adventure.minimessage)
    implementation(libs.adventure.platform.fabric)

    setOf(
        "fabric-lifecycle-events-v1",
        "fabric-command-api-v2"
    ).forEach { implementation(fabricApi.module(it, "0.151.0+26.1.2")) }

    // API Inclusion
    api(project(":launchers:fabric:api-repack", configuration = "namedElements"))
    include(projects.launchers.fabric.apiRepack)
}

tasks {
    val version: String = rootProject.property("modVersion") as String
    processResources {
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }

    jar {
        archiveBaseName.set("GooeyLibs-Fabric")
        archiveVersion.set(project.version as String)
    }
}

publishing {
    publications {
        create<MavenPublication>("fabric") {
            from(components["java"])
            groupId = "ca.landonjw.gooeylibs"
            artifactId = "fabric"
            version = rootProject.version.toString()
        }
    }
}

modrinth {
    loaders.set(listOf("fabric"))
    uploadFile.set(tasks["jar"])
}
