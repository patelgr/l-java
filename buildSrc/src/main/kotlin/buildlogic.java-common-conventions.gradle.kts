plugins {
    java
    id("com.github.ben-manes.versions")
    id("buildlogic.spotless-conventions")
    id("buildlogic.checkstyle-conventions")
    id("buildlogic.errorprone-conventions")
    id("buildlogic.jacoco-conventions")
    id("buildlogic.spotbugs-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
}

dependencies {
    // JSpecify null-safety annotations (compile-time only; no runtime footprint)
    compileOnly(libs.findLibrary("jspecify").get())
    testCompileOnly(libs.findLibrary("jspecify").get())
}

java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.compilerArgs.add("-parameters")

    if (!name.contains("Test", ignoreCase = true)) {
        options.compilerArgs.addAll(
            listOf(
                "-Xlint:all",
                "-Xlint:-processing",
                "-Xlint:-serial",
                "-Xlint:-classfile",
                "-Werror",
            ),
        )
    } else {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }
}

tasks.withType<Jar>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return !(stableKeyword || regex.matches(version))
}

tasks
    .named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates")
    .configure {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }

// JUnit 4 import guard — catches stray @Test annotations that would be silently
// skipped at runtime (project has no junit-vintage-engine on the test classpath).
// Wired to run before both compileJava and compileTestJava so the failure is
// surfaced immediately, not after a slow test run.
tasks.register<CheckJunitLegacyTask>("checkJunitLegacy") {
    group = "verification"
    description = "Fails the build if any JUnit 4 import is found in Java source files."
    sourceFiles.from(fileTree("src") { include("**/*.java") })
    markerFile.set(layout.buildDirectory.file("contract-checks/junit-legacy.ok"))
}

plugins.withType<JavaPlugin> {
    tasks.named("compileJava") { dependsOn("checkJunitLegacy") }
    tasks.named("compileTestJava") { dependsOn("checkJunitLegacy") }
}
