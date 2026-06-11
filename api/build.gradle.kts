plugins {
    id("gooeylibs.base-conventions")
    id("net.neoforged.moddev") version libs.versions.neo.moddev.get()
}

neoForge {
    neoFormVersion = "26.1.2-1"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.mixins.base)
    compileOnly(libs.mixins.extras)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

sourceSets {
    main {
        java.srcDirs(
            "src/accessor/java",
            "src/mixin/java"
        )

        resources.srcDirs(
            "src/accessor/resources",
            "src/mixin/resources"
        )
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    processResources {
        val version: String = rootProject.property("modVersion") as String
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to version)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("api") {
            from(components["java"])
            groupId = "ca.landonjw.gooeylibs"
            artifactId = "api"
            version = rootProject.version.toString()
        }
    }
}
