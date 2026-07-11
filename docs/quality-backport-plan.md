# Static-Analysis Backport Plan — `l-java` template

**Date:** 2026-06-14
**Status:** Tier 1 + bug fixes applied 2026-07-11; Tier 2 profile done earlier as LJava.xml; Tier 3 deferred.
**Source of comparison:** `scircle/stock-analysis` (the mature app that descends from this template).

## Framing

`l-java` and `stock-analysis` share an identical `buildSrc` precompiled-convention-plugin
skeleton. `l-java` is the **ancestor**; `stock-analysis` is the **proving ground** that has
run real findings through Checkstyle, Error Prone, SpotBugs, and IntelliJ/Qodana. Every gap
below flows one direction: hardening stock-analysis learned that the template hasn't absorbed.

**The discipline:** backport the *muscle* (more checks, better structure, the IDE/CI inspection
split) without the *tattoos* (app-specific suppressions, `app.scircle.*` names, JPA exemptions).
The template's worth is being a **clean** start, so keep it generic.

Target stays **Java 21 LTS** (do not bump to 25 just because stock-analysis did).

---

## Fix regardless of backporting (template bugs)

- [x] **Orphan version pin.** `gradle/libs.versions.toml` declares `errorprone-slf4j = "0.1.28"`
  with no `[libraries]` entry and no usage. Either wire it (preferred — see Tier 1 SLF4J) or delete.
  Done 2026-07-11: wired (bumped to 0.1.29, the latest release).
- [x] **Spotless version contradiction.** `buildSrc/build.gradle.kts` pins
  `spotless-plugin-gradle:7.0.2`; `libs.versions.toml` declares `spotless = "8.4.0"`. The buildSrc
  pin wins for convention plugins, so the catalog entry is misleading. Reconcile to one version.
  Done 2026-07-11: reconciled to 8.4.0 everywhere; dropped the unused `[plugins]` alias.

---

## Tier 1 — quick, pure wins (no project-specific content)

- [x] Wire the 5 SLF4J Error Prone checks (activates the dead pin):
  `Slf4jLoggerShouldBePrivate`, `Slf4jLoggerShouldBeFinal`, `Slf4jSignOnlyFormat` (ERROR),
  `Slf4jFormatShouldBeConst`, `Slf4jDoNotLogMessageOfExceptionExplicitly` (WARN). Add the
  `errorprone-slf4j` `[libraries]` entry + `"errorprone"(...)` dependency.
  Done 2026-07-11.
- [x] Bump **Checkstyle 10.18.0 → 10.21.4** (prerequisite for the Java-21 checks below).
  Done 2026-07-11.
- [x] Reorganize **both** `errorprone-conventions.gradle.kts` and `config/checkstyle/checkstyle.xml`
  into documented buckets + a "Deliberately Off / Not Enforced" Chesterton's-Fence footer.
  Pure structure; large readability/maintainability gain.
  Done 2026-07-11: CORRECTNESS / ARCHITECTURE & API / TEMPLATE SAFETY (renamed from DOMAIN
  SAFETY) / DISCIPLINE (+ checkstyle-only FORMATTING / COMPLEXITY & SIZE) + Deliberately Off
  footer.
- [x] Backport **generalizable Error Prone checks** the template lacks:
  - Concurrency/memory-model: `SynchronizeOnNonFinalField`, `NonAtomicVolatileUpdate`,
    `LockNotBeforeTry`, `PrimitiveAtomicReference`, `ThreadLocalUsage`, `StaticGuardedByInstance`,
    `StaticAssignmentInConstructor`, `ThreadJoinLoop`, `ThreadPriorityCheck`, `AlreadyChecked`.
  - Time-safety: `JavaUtilDate`, `PreferJavaTimeOverload`, `DurationToLongTimeUnit`.
  - Locale-safety: `StringCaseLocaleUsage`.
  - Correctness: `EqualsIncompatibleType`, `ComparisonOutOfRange`, `OperatorPrecedence`,
    `StreamToString`, `MixedMutabilityReturnType`, `InsecureCryptoUsage`, `Finalize`, `EmptyIf`,
    `TryFailRefactoring`.
  - Null cluster: `NullableConstructor`, `NullablePrimitive`, `NullableVoid`, `OptionalNotPresent`,
    `ReturnsNullCollection`.
  - Immutability: `ImmutableAnnotationChecker`.
  - Discipline: `FieldCanBeFinal`, `OverrideThrowableToString`, `InconsistentCapitalization`,
    `InconsistentOverloads`, `UnnecessaryDefaultInEnumSwitch`, `ShortCircuitBoolean`,
    `UnnecessaryLambda`.
  - Architecture: `InterfaceWithOnlyStatics`, `MissingSummary`, `UnusedNestedClass`.
  Done 2026-07-11: all listed checks added, all ERROR, no deviations needed.
- [x] Backport **generalizable Checkstyle modules** the template lacks:
  - `ImportOrder` (java/javax/jakarta/* groups).
  - Java-21/22: `MissingNullCaseInSwitch`, `WhenShouldBeUsed`,
    `UnusedCatchParameterShouldBeUnnamed`, `UnusedLambdaParameterShouldBeUnnamed`.
  - Hygiene: `AvoidNestedBlocks`, `OneTopLevelClass`, `PackageDeclaration`, `FinalLocalVariable`,
    `OverloadMethodsDeclarationOrder`, `VariableDeclarationUsageDistance`,
    the three extra `UnnecessarySemicolon*` checks, `SuppressWarnings` (ban `all`).
  - Coupling ratchets: `ClassFanOutComplexity`, `ClassDataAbstractionCoupling`,
    `NestedForDepth`, `NestedTryDepth`, `MultipleStringLiterals`.
- [x] Replace the empty SpotBugs `config/spotbugs/exclude.xml` with the **generalizable**
  delegations only: `US_USELESS_SUPPRESSION_ON_CLASS/METHOD` (false-fire on records) and the
  NullAway hand-off (`NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`,
  `NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE`, `RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE`).
  Drop the Spring-specific `CT_CONSTRUCTOR_THROW`.
  Done 2026-07-11.

## Tier 2 — structural (highest leverage; scaffold, not copy)

- [x] **IntelliJ IDE + CI inspection profiles** (`.idea/inspectionProfiles/`). The template has
  none today, so new projects inherit IntelliJ's weak defaults. Port the two-profile model,
  genericized (strip `StockAnalysis*` names):
  - `<Template>IDE.xml` — broad authoring-time nudges (WEAK WARNING / WARNING / ERROR).
  - `<Template>CI.xml` — correctness/structural blockers only at ERROR, runnable by **Qodana**
    via a `qodana.yaml`. This adds a *fourth* analysis engine (IntelliJ data-flow:
    `DataFlowIssue`, `BigDecimalEquals`, `OptionalGetWithoutIsPresent`, cyclic-dependency checks)
    that javac-based tools structurally can't do.
  - Add `profiles_settings.xml` pointing `PROJECT_PROFILE` at the IDE profile.
  Done as `LJava.xml`: a single severity-laddered profile (deliberate design, better than
  this plan's two-profile suggestion) + `qodana.yaml`, predating this backport.
- [x] **`import-control.xml` scaffold** + the `ImportControl` Checkstyle wiring, with placeholder
  layering rules (commented examples) instead of `app.scircle.*` package names.
  Done 2026-07-11: allow-everything default, `ImportControl` wired but commented out with an
  "enable when your project has layers" note.

## Tier 3 — advanced (opt-in capability; bigger lift)

- [ ] Custom **Error Prone checker skeleton**: a `:checks` module (minimal `java-library`, no
  errorprone-conventions to avoid a cycle), `error_prone_check_api` + AutoService SPI, the
  three `--add-exports=jdk.compiler/...` flags, and one example `BugChecker`. Wire via
  `"errorprone"(project(":checks"))`.
- [ ] **Refaster** scaffold: `refaster-conventions` plugin, a `refaster-rules` module, and
  `baseline-refaster-testing` for rule tests.

---

## Do NOT backport (project-specific cruft)

- `structuredEvtCarrier` / `MultipleStringLiterals` `"evt"` exception — stock-analysis logging contract.
- `app.scircle.*` package names in NullAway `AnnotatedPackages`, `ImportControl`, `CheckReturnValue:Packages`.
- `VisibilityModifier` JPA annotation exemptions (`jakarta.persistence.*`).
- The Jackson-3 / `javax.*` ban specifics in `IllegalImport` (port a *minimal* generic ban; the
  full list is migration-state-specific).
- The large **commented-out duplicate `TreeWalker` block** at the bottom of stock-analysis's
  `checkstyle.xml` — dead cruft.
- The **NullAway double-configuration**: stock-analysis configures NullAway in *two* convention
  plugins with *divergent* `AnnotatedPackages` (`app.scircle` vs `app.scircle.stockanalysis`) —
  a latent smell. The template's single-location setup is cleaner; **preserve it**.

---

## Verification

After each tier, in `l-java`: run `./gradlew build` on the sample `src/` and confirm the new
checks compile and don't false-fire on the template's own example code. Tier 1 should be green
with at most trivial fixes; if a backported check floods the sample sources, either fix the
sample or move that check to the documented "Off" footer with a reason (don't silently drop it).
