group = "io.github.patelgr"
version = file("VERSION").readText().trim()

plugins {
    id("buildlogic.java-library-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Logging
    implementation(libs.findLibrary("slf4j-api").get())
    runtimeOnly(libs.findLibrary("logback-classic").get())

    // Testing
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("mockito-core").get())
    testImplementation(libs.findLibrary("mockito-junit-jupiter").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}
