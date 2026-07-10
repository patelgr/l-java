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

// Error Prone checks below are grouped by the KIND of bug each one prevents, not by
// which Error Prone module ships it upstream. When adding a check:
//   1. Pick the bucket by what breaks if the check stays silent (see descriptions
//      below) — not wherever Error Prone happens to file it in its own docs.
//   2. Default severity is ERROR. Use WARN only when false positives are common
//      enough that failing the build on them would be more disruptive than helpful,
//      and say why in a trailing comment.
//   3. Keep each bucket alphabetically sorted so diffs stay small and scannable.
//   4. If a check is generally sound but floods this template's own sample sources
//      with noise inherent to its nature (not an actual bug in the sample), move it
//      to the "Deliberately Off" footer with a one-line reason — never drop it
//      silently.
//
// CORRECTNESS         — the code produces a wrong result or silently swallows a bug.
// ARCHITECTURE & API  — protects a public contract, encapsulation boundary, or module
//                       structure, rather than a single line's behavior.
// TEMPLATE SAFETY     — hazards generic to the JVM/platform (concurrency, time, locale,
//                       crypto) that still apply once this template becomes someone's
//                       real project, as opposed to anything domain-specific.
// DISCIPLINE          — style, hygiene, and dead-code rules with no direct runtime bug,
//                       kept because they make the codebase consistent and reviewable.
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

        // ---- CORRECTNESS ----
        check(
            "CheckReturnValue",
            if (isTest) net.ltgt.gradle.errorprone.CheckSeverity.WARN else net.ltgt.gradle.errorprone.CheckSeverity.ERROR,
        )
        check("EqualsHashCode", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MissingDefault", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("NonOverridingEquals", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        // NullAway: JSpecify mode — @NullMarked packages are checked for null safety
        check("NullAway", if (isTest) net.ltgt.gradle.errorprone.CheckSeverity.WARN else net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        option("NullAway:JSpecifyMode", "true")
        option(
            "NullAway:AnnotatedPackages",
            project.findProperty("nullAway.annotatedPackages")?.toString() ?: "io.github.patelgr.ljava",
        )
        check("ReferenceEquality", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ReturnMissingNullable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jDoNotLogMessageOfExceptionExplicitly", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
        check("Slf4jSignOnlyFormat", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StreamResourceLeak", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StringSplitter", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryOptionalGet", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // ---- ARCHITECTURE & API ----
        check("BooleanParameter", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MutablePublicArray", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        // Nopen: every class must be final, abstract, or @Open
        check("Nopen", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("NotJavadoc", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("PackageLocation", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // ---- TEMPLATE SAFETY ----
        check("DefaultCharset", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ImmutableEnumChecker", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("JavaTimeDefaultTimeZone", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("TimeUnitMismatch", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // ---- DISCIPLINE ----
        check("BadImport", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("ClassName", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MethodCanBeStatic", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MissingOverride", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("MultipleTopLevelClasses", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("NonCanonicalType", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jFormatShouldBeConst", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
        check("Slf4jLoggerShouldBeFinal", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("Slf4jLoggerShouldBePrivate", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("StaticQualifiedUsingExpression", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("TypeParameterNaming", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryAssignment", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryBoxedVariable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnnecessaryParentheses", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnusedMethod", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("UnusedVariable", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)
        check("WildcardImport", net.ltgt.gradle.errorprone.CheckSeverity.ERROR)

        // ---- Deliberately Off / Not Enforced ----
        // (none yet — if a backported check floods the sample sources with noise
        // inherent to its nature, add it here with a one-line reason instead of
        // silently dropping it.)
    }
}
