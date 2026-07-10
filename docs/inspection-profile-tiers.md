# IntelliJ Inspection Tiers — minimum / balance / paranoid

**Date:** 2026-06-14
**Source of truth:** JetBrains Inspectopedia (`https://www.jetbrains.com/help/inspectopedia/Java.html`),
fetched 2026-06-14 — **48 categories, ~900 Java inspections**.
**Real-world anchor:** `stock-analysis` `.idea/inspectionProfiles/StockAnalysisCI.xml` (its ERROR set ≈
"minimum") and `StockAnalysisIDE.xml` (its WARNING vs WEAK-WARNING split ≈ balance vs paranoid).
**Status:** rubric + split-rules complete; per-rule table to be appended category-by-category.

## Model

Three tiers, expressed as a **single severity ladder** in one source-of-truth profile (no divergent files):

| Tier | Severity | Meaning | Stage |
|---|---|---|---|
| **minimum** | ERROR | safe & bug-free — *complete* on correctness/security, empty on taste | **CI gate** (Qodana fails on ERROR) |
| **balance** | WARNING | discipline a reviewer enforces (design, naming, coupling, dead code) | IDE default highlight |
| **paranoid** | WEAK WARNING | opinionated / modernization / nitpick | IDE on-demand |

`minimum` ⊂ `balance` ⊂ `paranoid` (cumulative). "minimum" ≠ "few rules" — it is *all* bug/safety
rules and nothing else.

## Generated artifacts (2026-06-14)

- `.idea/inspectionProfiles/LJava.xml` — the severity-laddered profile. `minimum`=ERROR uses
  **verified inspection IDs only** (~48); the expanded `minimum` set whose IDs aren't yet confirmed
  sits in the profile's `VERIFY-THEN-PROMOTE` comment (guessed IDs silently no-op, so they are not
  shipped live). `paranoid`=WEAK WARNING; `balance` inherits IDE defaults.
- `.idea/inspectionProfiles/profiles_settings.xml` — sets `LJava` as the project profile.
- `qodana.yaml` — CI gate fails on ERROR (`severityThresholds: critical/high = 0`); `exclude`s the
  Checkstyle/EP overlaps; includes baseline + coverage notes.
- `.gitignore` — switched `.idea/` → `.idea/*` + `!.idea/inspectionProfiles/` so the profile is
  committable while the rest of `.idea/` stays ignored (verified via `git check-ignore`).

Judgment-call defaults applied: Overly-broad-`catch` → **balance** (Checkstyle `IllegalCatch` owns
the hard ban at minimum); possibly-lossy compound assignment → **balance** (promote to minimum for
financial/numeric domains); `"Cast"` on the Numeric page → **dropped** (mis-grouped).

Status: scaffold — verify the ERROR IDs and the ERROR→severity mapping on the first Qodana run.

## Category → tier rubric

~33 of 48 categories resolve to one tier by category. The other **15 are mixed** and need per-rule
adjudication (split rules below).

### minimum (CI gate)
Probable bugs (107), Data flow (8), Threading issues (52)*, Concurrency annotation issues (6),
Security (16), Resource management (8), Reflective access (4), Compiler issues (4),
Finalization (3), Bitwise operation issues (3)*.
(* small mixed tail — see split rules.)

### balance (IDE default)
Class structure (31), Declaration redundancy (25)†, Inheritance issues (21), Visibility (18),
Abstraction issues (15), Naming conventions (11), Method metrics (11), Class metrics (9),
Encapsulation (8), Imports (8), Packaging issues (7), Logging (6), Modularization issues (5),
JavaBeans issues (5). Test scope: JUnit (1), Test frameworks (5).
(† redundancy: unused→balance, truly-redundant→paranoid.)

### paranoid (IDE on-demand)
Code style issues (80), Verbose or redundant code constructs (45), Javadoc (16)‡,
Java language level issues (7), Java language level migration aids (4), toString() issues (2).
(‡ promote to balance if the project is a *published library*.)

### off (not applicable to this stack)
TestNG (10) — JUnit project. Lombok (5) — enable only if Lombok is used. Properties files (2) —
enable if i18n bundles exist.

## Mixed categories — per-rule split rules

| Category | minimum (ERROR) when… | balance/paranoid when… |
|---|---|---|
| Error handling (28) | finally-block flow, lost exception, null thrown | rethrow / empty-catch / style |
| Control flow issues (50) | runtime-wrong (infinite loop, constant condition, unreachable) | cosmetic (unnecessary-else, duplicate branch, loop style) |
| Code maturity (13) | obsolete/unsafe API banned in new code (obsolete date/time + collections, `OptionalAssignedToNull`) | deprecation-usage nag |
| Numeric issues (28) | real defect (NaN compare, FP equality, `BigDecimal.equals`, int-div-in-FP-context, overflow) | cosmetic arithmetic / octal style |
| Performance (47) | hot-path defect (concat-in-loop, regex compile in loop, boxed-in-loop) | micro-optimisation nits |
| Serialization issues (23) | breaks serialization correctness (non-serializable field in serializable type) | serialVersionUID / ctor nags — **off entirely if no Java serialization** |
| Initialization (11) | abstract-call-in-ctor, this-escape, static-init cycle/forward-ref | non-final / init-order style |
| Memory (8) | static-collection leak, ThreadLocal not removed | capacity nags, inner-class-may-be-static |
| Portability (10) | hardcoded file/line separators, platform-specific calls | rest |
| Internationalization (17) | locale/charset *bug* (default-charset, locale-sensitive case) | broad hardcoded-string nags — **usually off unless localising** |
| Assignment issues (11) | assignment-used-as-condition, nested assignment | assign-to-param, assign-to-for-loop-param |
| Dependency issues (6) | cyclic class/package dependency | "too many dependencies" metrics → balance |
| Cloning issues (7) | clone-contract violation (clone in non-Cloneable, missing super.clone) | clone style — **often off** |
| Bitwise operation issues (3) | pointless / shift-count-out-of-range | — |
| Threading issues (52) | ~24 hard concurrency bugs (locks, wait/notify, volatile, DCL) | ~26 lock-hygiene advisories → balance; 2 → paranoid |

## Per-rule expansion plan

For each of the 15 mixed categories, in this order of value:
Error handling → Control flow → Code maturity → Numeric → Performance → Initialization →
Serialization → Memory → Portability → Internationalization → Assignment → Dependency →
Cloning → Bitwise → Threading-tail.

For each: fetch the current rule list from Inspectopedia, apply the split rule, cross-check against
stock-analysis's existing severity (where present), and append a row to the table below as
`inspection | category | tier | severity | note`. Checkpoint after each category.

## Per-rule classification table

Progress: **all 15 mixed categories classified rule-by-rule.**
Rule names pulled live from Inspectopedia category pages (`Java-<Category>.html`), 2026-06-14.

### Error handling (28) — `Java-Error-handling.html`
- **minimum (9):** 'continue'/'break' inside 'finally'; 'Error' not rethrown; 'finally' block which
  can not complete normally; 'null' thrown; 'return' inside 'finally'; 'ThreadDeath' not rethrown;
  'throw' inside 'catch' which ignores the caught exception; 'throw' inside 'finally'; Throwable
  supplier never returns a value.
- **balance (15):** 'instanceof' on 'catch' parameter; 'throw' caught by containing 'try'; Catch
  block may ignore exception; Caught exception immediately rethrown; Class directly extends
  'Throwable'; Empty 'finally'; Empty 'try'; Exception constructor called without arguments; Nested
  'try'; Non-final field of 'Exception'; Overly broad 'catch' (hard ban lives in Checkstyle
  `IllegalCatch` = minimum); Overly broad 'throws'; Prohibited exception caught / declared / thrown.
- **paranoid (4):** Checked exception class; Unchecked 'Exception' class; Unchecked exception
  declared in 'throws'; Unnecessary call to 'Throwable.initCause()'.

### Code maturity (14) — `Java-Code-maturity.html`
- **minimum (7):** 'Throwable' printed to 'System.out'; Call to 'printStackTrace()'; Call to
  'Thread.dumpStack()'; Null value for Optional type; Use of 'System.out'/'System.err'; Use of
  obsolete collection type; Use of obsolete date-time API.
- **balance (4):** Deprecated API usage; Deprecated member is still used; Usage of API marked for
  removal; Use of 'clone()'/'Cloneable'.
- **paranoid (3):** Commented out code; Method can be extracted; Redundant @ScheduledForRemoval.

### Numeric issues (28) — `Java-Numeric-issues.html`
- **minimum (11):** 'equals()' on 'BigDecimal'; 'BigDecimal' method without rounding mode; Comparison
  of 'short' and 'char'; Comparison to NaN; Division by zero; Floating-point equality; Integer
  division in floating-point context; Negative int hex constant in long context; Numeric overflow;
  Suspicious oddness check; Unpredictable 'BigDecimal' constructor call.
- **balance (10):** 'char' in arithmetic context; 'long' literal ending 'l' (Checkstyle `UpperEll`);
  Confusing floating-point literal; Implicit numeric conversion; Non-reproducible call to 'Math';
  Number constructor call with primitive arg; Octal and decimal in same array; Octal integer;
  Pointless arithmetic expression; Possibly lossy implicit cast in compound assignment.
- **paranoid (7):** Constant call to 'Math'; Numeric literal can use underscores; Overly complex
  arithmetic; Suspicious underscore in literal; Unary plus; Underscores in numeric literal;
  Unnecessary unary minus.
- *Note:* the page also lists "Cast" — appears mis-grouped (not a numeric inspection); verify and
  place under Verbose/redundant. Excluded from the count above.

### Control flow issues (50) — `Java-Control-flow-issues.html`
- **minimum (11):** 'if' with identical branches or common parts; Conditional expression with
  identical branches; Constant conditional expression; Duplicate condition; Enum 'switch' that
  misses case; Fallthrough in 'switch'; Idempotent loop body; Infinite loop statement; Loop
  statement that does not loop; Loop variable not updated inside loop; Pointless 'indexOf()'
  comparison.
- **balance (7):** 'default' not last case; 'for' loop with missing components; 'switch' without
  'default'; Local variable used and declared in different 'switch' branches; Overly complex boolean
  expression; Pointless boolean expression; Unnecessary 'null' check before method call.
- **paranoid (32):** all remaining break/continue/label/switch-metric/ternary/redundant-else/
  double-negation/simplifiable-* style and modernization nudges.

**Gate (minimum) subtotal across those 4 categories: 38 inspections.**

### Performance (47) — `Java-Performance.html`
- **minimum (0):** none — performance ≠ correctness; nothing here gates CI.
- **balance (9):** List.remove() in loop; Boolean constructor call; Arrays.asList() with too few
  args; Dynamic regex → compiled Pattern; Non-regex replaceAll() → replace(); Object instantiation
  in equals()/hashCode(); String concatenation as arg to StringBuilder.append(); String
  concatenation in loop; Random.nextDouble() to get random int.
- **paranoid (38):** all remaining micro-optimisation nudges.

### Internationalization (17) — `Java-Internationalization.html`
- **minimum (4):** 'SimpleDateFormat' without locale; toUpperCase()/toLowerCase() without locale;
  Implicit platform default charset; Unsafe lazy initialization of 'static' field.
- **balance (4):** Date.toString(); Time.toString(); suspicious 'String' method; 'StringTokenizer'.
- **paranoid / off (9):** Hardcoded strings, Magic character, Non-Basic Latin, etc. — **off unless
  the app is localised.**

### Serialization issues (24) — `Java-Serialization-issues.html`
- **Whole category OFF if the app doesn't use `java.io.Serializable`** (JSON/Jackson apps). If it does:
- **minimum (8):** Externalizable without public no-arg ctor; read/writeObject() not private;
  Serializable implicitly stores non-Serializable; Non-serializable field in Serializable class;
  Non-serializable object passed to ObjectOutputStream; Serializable class with unconstructable
  ancestor; Serializable non-static inner with non-Serializable outer; non-serializable bound to
  HttpSession.
- **balance (~10) / paranoid (~6):** @Serial, serialVersionUID, transient-field nags.

### Assignment issues (11) — `Java-Assignment-issues.html`
- **minimum (3):** Assignment used as condition; Nested assignment; Result of '++'/'--' used.
- **balance (8):** assignment to catch/for-loop/lambda/method parameter; assignment to static field
  from instance; 'null' assignment; constructor assigns superclass field; replace-with-operator-assignment.

### Initialization (12) — `Java-Initialization.html`
- **minimum (6):** 'this' escaped in construction; Abstract method called during construction;
  Instance field used before initialization; Static field used before initialization; Non-final
  static field used during class init; Unsafe lazy init of 'static' field.
- **balance (6):** Double brace initialization; Instance/Static field may not be initialized;
  Overridable / Overridden method called during construction; Type parameter extends 'final' class.

### Memory (9) — `Java-Memory.html`
- **minimum (1):** Call to 'System.gc()' / 'Runtime.gc()'.
- **balance (2):** Inner class may be 'static'; Static collection.
- **paranoid (5) / off (1 Lombok):** StringBuilder field, zero-length array, return-of-inner-class…
- *(the hard static-field leaks — PublicStaticCollection/ArrayField — live in Security = minimum.)*

### Portability (11) — `Java-Portability.html`
- **minimum (3):** Call to 'System.exit()'; Hardcoded file separator; Hardcoded line separator.
- **balance (4):** Runtime.exec(); ProcessBuilder; 'sun.*' classes; concrete JDBC driver class.
- **paranoid (4):** System.getenv(); Native method; AWT peer class; wrapper-may-be-primitive.

### Dependency issues (6) — `Java-Dependency-issues.html`
- **minimum (2):** Cyclic class dependency; Cyclic package dependency.
- **balance (4):** the four "too many (transitive) dependencies / dependents" coupling metrics.

### Cloning issues (7) — `Java-Cloning-issues.html`
- **Off unless the codebase uses `clone()`.** Otherwise all 7 → **balance** (clone-contract
  hygiene); minimum 0.

### Bitwise operation issues (3) — `Java-Bitwise-operation-issues.html`
- **minimum (2):** Incompatible bitwise mask operation; Shift operation by inappropriate constant.
- **balance (1):** Pointless bitwise expression.

### Threading issues (52) — `Java-Threading-issues.html`
*Correction: genuinely ~half-and-half, not "mostly minimum" — the fetch settled it.*
- **minimum (24):** AtomicFieldUpdater not 'static final'; await() not in loop; await() without
  signal(); notify/notifyAll on Condition; notify/notifyAll without state change; notify() without
  wait(); signal() without await(); ThreadLocal not 'static final'; wait() on Condition; wait() not
  in loop; wait/notify not in synchronized context; wait() without notify(); access to static field
  locked on instance; System.runFinalizersOnExit(); Thread.stop/suspend/resume; double-checked
  locking; inconsistent AtomicFieldUpdater; lock acquired but not safely unlocked; non-atomic op on
  volatile field; static initializer references subclass; synchronization on a Lock object;
  synchronization on a non-final field; synchronization on literal-initialized object; unsynchronized
  method overrides synchronized method.
- **balance (26):** wait/await without timeout; sleep while synchronized; Thread.start() in
  construction; notify-instead-of-notifyAll; signal-instead-of-signalAll; sync on this / static /
  getClass / local-var; nested / empty synchronized; extends Thread; volatile array field; busy
  wait; native method while locked; field in sync+unsync contexts; ThreadLocal.set(null); etc.
- **paranoid (2):** 'synchronized' method; single-sync-block-can-be-a-synchronized-method.

**Gate (minimum) total across all 15 mixed categories: ≈ 83** (the 59 + Threading 24; Serialization's
+8 only if Java serialization is used). The full CI gate is this plus the **whole-category minimum**
groups — Probable bugs (107), Data flow (8), Security (16), Concurrency annotation (6), Resource
management (8), Reflective access (4), Compiler (4), Finalization (3) ≈ 156 — so the gate ceiling is
**~240 inspections before deduping** against Error Prone / Checkstyle, which already own a large
share at ERROR. **The Qodana `minimum` profile must `exclude` those overlaps** so a finding isn't
reported by two engines; net IntelliJ-only gate is materially smaller.

## Sources
- https://www.jetbrains.com/help/inspectopedia/Java.html
- https://www.jetbrains.com/help/inspectopedia/Java-Error-handling.html
- https://www.jetbrains.com/help/inspectopedia/Java-Code-maturity.html
- https://www.jetbrains.com/help/inspectopedia/Java-Numeric-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Control-flow-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Performance.html
- https://www.jetbrains.com/help/inspectopedia/Java-Internationalization.html
- https://www.jetbrains.com/help/inspectopedia/Java-Serialization-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Assignment-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Initialization.html
- https://www.jetbrains.com/help/inspectopedia/Java-Memory.html
- https://www.jetbrains.com/help/inspectopedia/Java-Portability.html
- https://www.jetbrains.com/help/inspectopedia/Java-Dependency-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Cloning-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Bitwise-operation-issues.html
- https://www.jetbrains.com/help/inspectopedia/Java-Threading-issues.html
