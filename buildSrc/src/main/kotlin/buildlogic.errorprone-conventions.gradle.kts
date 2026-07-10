import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("net.ltgt.errorprone")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

repositories {
    mavenCentral()
}

dependencies {
    "errorprone"(libs.findLibrary("errorprone-core").get())
    "errorprone"(libs.findLibrary("nullaway").get())
    "errorprone"(libs.findLibrary("nopen-checker").get())
    "errorprone"(libs.findLibrary("errorprone-slf4j").get())
    "compileOnly"(libs.findLibrary("errorprone-annotations").get())
    "compileOnly"(libs.findLibrary("nopen-annotations").get())
    "compileOnly"(libs.findLibrary("spotbugs-annotations").get())
}

tasks.withType<JavaCompile>().configureEach {
    val isTest = name.contains("test", ignoreCase = true)
    val isCi = providers.environmentVariable("CI").getOrElse("false").equals("true", ignoreCase = true)

    val allowCompilerWarnings = (project.findProperty("allowCompilerWarnings") ?: "false").toString().toBoolean()
    if (isCi && allowCompilerWarnings) {
        throw GradleException("Compiler warnings must not be suppressed in CI via 'allowCompilerWarnings'")
    }
    if (allowCompilerWarnings) {
        options.compilerArgs.removeAll(listOf("-Werror"))
    }

    val requestedDisable = (project.findProperty("dangerouslyDisableErrorProne") ?: "false").toString().toBoolean()
    if (isCi && requestedDisable) {
        throw GradleException("Error Prone must not be disabled in CI via 'dangerouslyDisableErrorProne'")
    }

    options.errorprone {
        isEnabled.set(!requestedDisable)
        if (requestedDisable) return@errorprone

        disableWarningsInGeneratedCode.set(true)

        // NullAway: JSpecify mode — @NullMarked packages are checked for null safety
        check("NullAway", if (isTest) net.ltgt.gradle.errorprone.CheckSeverity.WARN else net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        option("NullAway:JSpecifyMode", "true")
        option(
            "NullAway:AnnotatedPackages",
            project.findProperty("nullAway.annotatedPackages")?.toString() ?: "io.github.patelgr.ljava",
        )

        // Nopen: every class must be final, abstract, or @Open
        check("Nopen", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // Code quality
        check("MissingOverride", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("WildcardImport", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnusedVariable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnusedMethod", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("DefaultCharset", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StringSplitter", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ImmutableEnumChecker", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryParentheses", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StreamResourceLeak", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("BooleanParameter", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("BadImport", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MultipleTopLevelClasses", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("PackageLocation", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StaticQualifiedUsingExpression", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryAssignment", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ClassName", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("EqualsHashCode", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MutablePublicArray", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("NonOverridingEquals", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ReferenceEquality", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("TypeParameterNaming", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MissingDefault", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("NonCanonicalType", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MethodCanBeStatic", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check(
            "CheckReturnValue",
            if (isTest) net.ltgt.gradle.errorprone.CheckSeverity.WARN else net.ltgt.gradle.errorprone.CheckSeverity.ERROR,
        )
        check("NotJavadoc", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // Time-safety
        check("JavaTimeDefaultTimeZone", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("TimeUnitMismatch", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // Null-safety
        check("ReturnMissingNullable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryBoxedVariable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryOptionalGet", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // SLF4J
        check("Slf4jLoggerShouldBePrivate", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jLoggerShouldBeFinal", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jSignOnlyFormat", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jFormatShouldBeConst", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
        check("Slf4jDoNotLogMessageOfExceptionExplicitly", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
    }
}
