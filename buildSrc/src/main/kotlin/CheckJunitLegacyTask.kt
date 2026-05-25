import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * Fails the build if any JUnit 4 import is found in the scanned Java source files.
 *
 * <p>Without {@code junit-vintage-engine} on the test classpath, a JUnit 4
 * {@code @Test} annotation is silently ignored at runtime — the test appears
 * green but never executes. This task surfaces that mistake at compile time.
 *
 * <p>The following imports are intentionally permitted (JUnit 5 / Platform):
 * <ul>
 *   <li>{@code org.junit.jupiter.*}</li>
 *   <li>{@code org.junit.platform.*}</li>
 *   <li>{@code org.junit.vintage.*}</li>
 * </ul>
 */
abstract class CheckJunitLegacyTask : DefaultTask() {

    /** Java source files to scan. Task is skipped when the collection is empty. */
    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    /** Written on success; enables Gradle up-to-date skipping when no files change. */
    @get:OutputFile
    abstract val markerFile: RegularFileProperty

    @TaskAction
    fun check() {
        val violations =
            sourceFiles.files.flatMap { file ->
                file.readLines().mapIndexedNotNull { idx, line ->
                    val trimmed = line.trim()
                    val isJunit4 =
                        (trimmed.startsWith("import org.junit.") ||
                            trimmed.startsWith("import junit.framework.")) &&
                            !trimmed.contains("org.junit.jupiter") &&
                            !trimmed.contains("org.junit.platform") &&
                            !trimmed.contains("org.junit.vintage")
                    if (isJunit4) "${file.path}:${idx + 1}: $trimmed" else null
                }
            }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "JUnit 4 imports detected — use org.junit.jupiter.api instead:\n" +
                    violations.joinToString("\n"),
            )
        }

        val marker = markerFile.get().asFile
        marker.parentFile.mkdirs()
        marker.writeText("ok")
        logger.lifecycle("checkJunitLegacy: PASS — ${sourceFiles.files.size} file(s) scanned")
    }
}
