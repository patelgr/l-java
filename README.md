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
| **JUnit 5** + Mockito | Testing |

## Running

```bash
./gradlew build          # compile + test
./gradlew spotlessApply  # auto-format code
./gradlew checkstyleMain # run checkstyle
./gradlew dependencyUpdates  # check for newer dependency versions
```

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
