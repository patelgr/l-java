plugins {
    id("com.diffplug.spotless")
}

val licenseHeaderText: String =
    run {
        val author = providers.gradleProperty("template.author").getOrElse("Author Name")
        rootProject
            .file("config/license/license.header")
            .readText()
            .replace("\$AUTHOR", author)
    }

spotless {
    java {
        target("src/*/java/**/*.java")
        licenseHeader(licenseHeaderText)
        palantirJavaFormat("2.91.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/src/**/*.gradle.kts", "buildSrc/*.gradle.kts")
        ktlint()
    }

    format("misc") {
        target(
            "*.md",
            "docs/**/*.md",
            "*.yml",
            "*.yaml",
            ".gitignore",
            ".gitattributes",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}
