// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin

plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

rootProject.allprojects {
    this.afterEvaluate {
        if (this.plugins.hasPlugin(LibraryPlugin::class) && this.path.contains("eggs")) {
            val project = this
            this.extensions.configure<LibraryExtension>("android") {
                val s = project.name.substring(0, 1).toLowerCase()
                namespace = "com.android_$s.egg"
                resourcePrefix("${s}_")
                lint {
                    baseline = project.file("lint-baseline.xml")
                }
            }
        }
    }
}