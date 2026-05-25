import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("net.ltgt.gradle:gradle-errorprone-plugin:4.4.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.53.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.0.2")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.4.8")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.release.set(21)
}
