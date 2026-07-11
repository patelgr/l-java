# l-java

A learning Java project with production-grade tooling.

## Tooling

| Tool | Purpose |
|---|---|
| **Gradle 9** + Kotlin DSL | Build system with version catalog |
| **Spotless** + Palantir formatter | Opinionated code formatting |
| **Checkstyle 10** | Style and complexity rules |
| **Error Prone** | Compile-time bug detection |
| **NullAway** (JSpecify mode) | Null-safety enforced at compile time |
| **Nopen** | Every class must be `final`, `abstract`, or `@Open` |
| **SpotBugs** | Bytecode-level bug detection |
| **JUnit 5** + Mockito | Testing |
| **JaCoCo** | Coverage, gated at a 70% line floor (overridable per module) |
| **Qodana** (`qodana-jvm-community`) | IntelliJ data-flow analysis via the `LJava` severity-laddered inspection profile |

## Running

```bash
./gradlew build          # compile + test
./gradlew spotlessApply  # auto-format code
./gradlew checkstyleMain # run checkstyle
./gradlew dependencyUpdates  # check for newer dependency versions
```

## CI gates

What fails the build:
- **Compile** — `-Werror` (all lint categories except `processing`, `serial`, `classfile`).
- **Tests** — JUnit 5, run via `./gradlew test`.
- **Checkstyle** — zero warnings allowed (`maxWarnings = 0`).
- **SpotBugs** — advisory by default (`ignoreFailures = true`); a module opts into a hard
  gate by setting `extra["strictSpotbugs"] = true`.
- **Coverage** — JaCoCo line-coverage floor, 70% by default (see table above).
- **Qodana** — `ERROR`-tier inspections only (`.idea/inspectionProfiles/LJava.xml` via
  `qodana.yaml`); findings also land in the GitHub Security tab as SARIF.
- **CodeQL** — default query suite, `java-kotlin`, on push/PR to `main` plus a weekly scan.
- **Dependency review** — fails a PR that introduces a `high`-or-worse severity vulnerability.

## Key Concepts

### Closed-world classes (Nopen)
Every class must declare its extensibility intent:
- `final class` — cannot be subclassed (default for everything)
- `abstract class` — designed for extension
- `@Open class` — explicitly open (needed for CGLIB proxying, etc.)

### Null safety (NullAway + JSpecify)
The `@NullMarked` annotation on `package-info.java` means all reference types
in the package are non-null by default. Annotate with `@Nullable` only where
null is genuinely part of the contract.

### Money as a value object
`Money` demonstrates: immutability, value-based equality, defensive validation,
and arithmetic with proper rounding (banker's rounding / `HALF_EVEN`).
