plugins {
    `java-library`

    id("org.cadixdev.licenser")

    id("net.kyori.indra")
    id("net.kyori.indra.git")
    id("maven-publish")
}

group = rootProject.group
version = rootProject.version

indra {
    javaVersions {
        minimumToolchain(25)
        target(25)
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Test> {
        useJUnitPlatform()
        jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true")
    }
}

license {
    header(rootProject.file("HEADER.txt"))
    properties {
        this.set("name", "GooeyLibs")
        this.set("years", "201x - 2026")
    }
}

publishing {
    repositories {
        maven("https://maven.impactdev.net/repository/development/") {
            name = "ImpactDev-Public"
            credentials {
                username = System.getenv("NEXUS_USER")
                password = System.getenv("NEXUS_PW")
            }
        }
    }
}