import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Pure Kotlin rules engine (docs/rules.md). No Android dependency, so the
// backend can reuse it if it runs on the JVM (DEC-15). Set up in ticket A5.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
