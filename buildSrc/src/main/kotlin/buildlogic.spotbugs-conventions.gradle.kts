plugins {
    id("com.github.spotbugs")
}

// Default: lenient — SpotBugs findings are warnings, not build failures.
// Per-module strict opt-in: set extra["strictSpotbugs"] = true in the
// module's build.gradle.kts to treat any finding as a build failure.
spotbugs {
    ignoreFailures.set(true)
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
    excludeFilter.set(rootProject.layout.projectDirectory.file("config/spotbugs/exclude.xml"))
}

project.afterEvaluate {
    if (project.extra.properties["strictSpotbugs"] == true) {
        spotbugs {
            ignoreFailures.set(false)
        }
        logger.lifecycle("${project.name}: SpotBugs strict mode enabled")
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    // SpotBugs analysis on test sources produces high false-positive rates
    // due to test-specific patterns (mocks, assertions, lifecycle methods).
    if (name.contains("test", ignoreCase = true)) {
        enabled = false
        return@configureEach
    }
    reports {
        create("html") { required.set(true) }
        create("xml") { required.set(true) }
    }
}
