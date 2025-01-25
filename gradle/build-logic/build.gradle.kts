plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.architectury.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
}

dependencies {
    implementation(libs.kotlin.jvm)
    implementation(libs.indra)
    implementation(libs.licenser)
    implementation(libs.shadow)
    implementation(libs.minotaur)

    implementation(libs.neo.moddev)
}