plugins {
    alias(libs.plugins.android).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
}

repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
