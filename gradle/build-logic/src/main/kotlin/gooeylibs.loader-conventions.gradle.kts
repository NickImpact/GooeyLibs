import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    id("gooeylibs.base-conventions")
    id("com.modrinth.minotaur")
}

dependencies {
    compileOnly(project(":api"))
}

modrinth {
    token.set(System.getenv("MODRINTH_GRADLE_TOKEN"))
    projectId.set("GooeyLibs")
    versionNumber.set("${rootProject.property("modVersion")}-${project.name}")
    versionName.set("${project.version} (${project.version.toString().capitalized()})")

    versionType.set("release")

    gameVersions.set(listOf("26.1", "26.1.1", "26.1.2"))
    debugMode.set(false)
}