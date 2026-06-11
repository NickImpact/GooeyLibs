plugins {
    id("gooeylibs.loader-conventions")
    id("net.neoforged.moddev") version libs.versions.neo.moddev.get()
}

neoForge {
    version = "26.1.2.75"
    validateAccessTransformers = true

    val client = runs.create("client")
    client.client()

    val gooey = mods.maybeCreate("gooeylibs")
    gooey.sourceSet(sourceSets.main.get())
}

dependencies {
    jarJar(implementation(projects.api)!!)
}

tasks {
    jar {
        archiveBaseName.set("GooeyLibs-Neoforge")
        archiveVersion.set(project.version as String)
    }

    processResources {
        val version: String = rootProject.property("modVersion") as String
        inputs.property("version", version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to version)
        }
    }
}

modrinth {
    loaders.set(listOf("neoforge"))
    uploadFile.set(tasks["jar"])
}

publishing {
    publications {
        create<MavenPublication>("neoforge") {
            from(components["java"])
            groupId = "ca.landonjw.gooeylibs"
            artifactId = "neoforge"
            version = rootProject.version.toString()
        }
    }
}
