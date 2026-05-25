import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Prints the location of JaCoCo coverage reports after every test run.
 *
 * <p>When the {@code showCoverage} Gradle property is {@code true} (or when
 * {@code --info} logging is active), a detailed instruction/branch/line
 * coverage summary is printed with a visual progress bar.
 *
 * <p>Register this task in a JaCoCo convention plugin and wire it as a
 * {@code finalizedBy} action on {@code jacocoTestReport}.
 */
abstract class PrintCoverageTask @Inject constructor() : DefaultTask() {

    /** Directory containing JaCoCo HTML reports. */
    @get:Internal
    abstract val reportDir: DirectoryProperty

    /**
     * When {@code true}, prints a detailed per-metric coverage summary.
     * Defaults to {@code false}; override via {@code -PshowCoverage=true}.
     */
    @get:Input
    @get:Optional
    abstract val showCoverage: Property<Boolean>

    /** Module name shown in the coverage summary header. */
    @get:Input
    abstract val moduleName: Property<String>

    init {
        group = "verification"
        description = "Prints JaCoCo report location; shows a coverage summary when showCoverage=true."
    }

    @TaskAction
    fun print() {
        val dir = reportDir.get().asFile
        if (!dir.exists()) {
            logger.lifecycle("JaCoCo reports not yet generated (expected at ${dir.absolutePath})")
            return
        }
        logger.lifecycle("JaCoCo reports: ${dir.absolutePath}")

        val showDetailed = showCoverage.getOrElse(false) || logger.isInfoEnabled
        if (showDetailed) {
            printDetailedCoverage(dir)
        }
    }

    private fun printDetailedCoverage(reportDir: File) {
        val xmlReport =
            listOf(
                    File(reportDir.parentFile, "test/jacocoTestReport.xml"),
                    File(reportDir.parentFile, "jacocoTestReport.xml"),
                    File(reportDir, "test/jacocoTestReport.xml"),
                )
                .firstOrNull { it.exists() }

        if (xmlReport == null) {
            logger.lifecycle("Coverage XML not found — run ./gradlew jacocoTestReport first")
            return
        }

        try {
            val factory = DocumentBuilderFactory.newInstance()
            factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false,
            )
            factory.setFeature("http://xml.org/sax/features/validation", false)
            val root = factory.newDocumentBuilder().parse(xmlReport).documentElement
            val counters = root.getElementsByTagName("counter")

            var instrCovered = 0
            var instrMissed = 0
            var branchCovered = 0
            var branchMissed = 0
            var lineCovered = 0
            var lineMissed = 0

            for (i in 0 until counters.length) {
                val c = counters.item(i) as Element
                val covered = c.getAttribute("covered").toInt()
                val missed = c.getAttribute("missed").toInt()
                when (c.getAttribute("type")) {
                    "INSTRUCTION" -> {
                        instrCovered = covered
                        instrMissed = missed
                    }
                    "BRANCH" -> {
                        branchCovered = covered
                        branchMissed = missed
                    }
                    "LINE" -> {
                        lineCovered = covered
                        lineMissed = missed
                    }
                }
            }

            val name = moduleName.get().padEnd(30)
            logger.lifecycle("")
            logger.lifecycle("╔═══════════════════════════════════════════════════════════╗")
            logger.lifecycle("║          Coverage Report: $name║")
            logger.lifecycle("╠═══════════════════════════════════════════════════════════╣")
            printRow("Instruction", instrCovered, instrMissed)
            printRow("Branch", branchCovered, branchMissed)
            printRow("Line", lineCovered, lineMissed)
            logger.lifecycle("╚═══════════════════════════════════════════════════════════╝")
            logger.lifecycle("")
        } catch (e: Exception) {
            logger.lifecycle("Unable to parse coverage report: ${e.message}")
        }
    }

    private fun printRow(label: String, covered: Int, missed: Int) {
        val total = covered + missed
        val pct = if (total > 0) covered * 100.0 / total else 0.0
        val bar = buildBar(pct)
        logger.lifecycle("║ ${label.padEnd(12)} ${String.format("%5.2f%%", pct)} ($covered/$total) $bar ║")
    }

    private fun buildBar(pct: Double): String {
        val filled = (pct / 100.0 * 20).toInt()
        val char = if (pct >= 80.0) "█" else if (pct >= 65.0) "▓" else "░"
        return char.repeat(filled) + "░".repeat(20 - filled)
    }
}
