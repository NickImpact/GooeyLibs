plugins {
    id("gooeylibs.base-conventions")
    alias(libs.plugins.loom)
    alias(libs.plugins.indra)
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabric.loader)
}

loom {
    mixin {
        useLegacyMixinAp = false
    }
    runtimeOnlyLog4j.set(true)
}

tasks {
    val api = project(":api")
    jar {
        from(zipTree(api.tasks.jar.flatMap { it.archiveFile })) {
            exclude("META-INF/MANIFEST.MF")
        }
    }

    sourcesJar {
        from(zipTree(api.tasks.sourcesJar.flatMap { it.archiveFile }))
    }

    javadoc {
        enabled = false
    }
}

publishing {
    publications {
        create<MavenPublication>("fabric-api-repack") {
            from(components["java"])
            groupId = "ca.landonjw.gooeylibs"
            artifactId = "fabric-api-repack"
            version = rootProject.version.toString()
        }
    }
}
