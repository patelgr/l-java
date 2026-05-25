// Convention layering:
//   buildlogic.java-common-conventions  — base (compiler, spotless, checkstyle,
//                                         errorprone, jacoco, junit-guard)
//   buildlogic.java-library-conventions — common + `java-library` plugin (this file)
//
// To add an application convention, create
// `buildlogic.java-application-conventions.gradle.kts` that applies common +
// the `application` plugin (or a framework plugin) and adds app-specific
// defaults (main class, logging, packaging).

plugins {
    id("buildlogic.java-common-conventions")
    `java-library`
}
