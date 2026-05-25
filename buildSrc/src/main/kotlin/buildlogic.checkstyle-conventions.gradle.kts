plugins {
    checkstyle
}

checkstyle {
    toolVersion = "10.18.0"
    configDirectory.set(rootProject.layout.projectDirectory.dir("config/checkstyle"))
    isIgnoreFailures = false
    maxWarnings = 0
    isShowViolations = true
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
