// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {

        id("com.android.application") version "8.9.1" apply false
        id("org.jetbrains.kotlin.android") version "2.2.21" apply false

    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
