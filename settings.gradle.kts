plugins {
    id("com.gradle.develocity") version "4.4.1"
}

rootProject.name = "l-java"

develocity {
    server = "https://scans.gradle.com"
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        publishing.onlyIf { false }
    }
}
