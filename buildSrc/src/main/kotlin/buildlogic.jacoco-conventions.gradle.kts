plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.13"
}

// Generate coverage report automatically after every test run.
tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
    finalizedBy("printCoverage")
}

// Lightweight coverage summary printed after every test run.
// Pass -PshowCoverage=true for a per-metric breakdown with a visual bar.
tasks.register<PrintCoverageTask>("printCoverage") {
    reportDir.convention(layout.buildDirectory.dir("reports/jacoco"))
    moduleName.convention(project.name)
    showCoverage.convention(providers.gradleProperty("showCoverage").map { it.toBoolean() })
}

// Coverage verification runs as part of the standard `check` lifecycle.
// Override the line-coverage floor per module:
//   extra["jacocoMinLineCoverage"] = "0.90"  // in the module's build.gradle.kts
tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn(tasks.named("test"))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum =
                    (project.findProperty("jacocoMinLineCoverage") as String? ?: "0.70")
                        .toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.0".toBigDecimal() // modules set their own branch floor when ready
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
