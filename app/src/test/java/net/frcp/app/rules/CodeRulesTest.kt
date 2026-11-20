package net.frcp.app.rules

import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.io.File

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CodeRulesTest {
    private val root = File("..").canonicalFile
    private val skipped = setOf("build", "temp", "node_modules")

    private fun codeFiles() = root.walkTopDown()
        .onEnter { !it.name.startsWith(".") && it.name !in skipped }
        .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
        .sortedBy { it.path }
        .toList()

    private fun masked(file: File) = SourceMask(file.readText().replace("\r", "")).masked()

    private fun name(file: File) = file.relativeTo(root).invariantSeparatorsPath

    @Test
    fun t351_codeHasNoComments() {
        val commented = codeFiles().flatMap { file ->
            masked(file).split("\n").withIndex()
                .filter { (_, line) -> "//" in line || "/*" in line }
                .map { (i, _) -> "${name(file)}:${i + 1}" }
        }
        assertEquals("Comments in the code (Rule 10)", emptyList<String>(), commented)
    }

    @Test
    fun t352_noFunctionIsLongerThanSixtyLines() {
        val long = codeFiles().flatMap { file ->
            FunctionSpans(masked(file)).all()
                .filter { it.length > MAX_FUNCTION_LINES }
                .map { "${name(file)}:${it.line} ${it.name}, ${it.length} lines" }
        }
        assertEquals("Functions over $MAX_FUNCTION_LINES lines (Rule 11)", emptyList<String>(), long)
    }

    private companion object {
        const val MAX_FUNCTION_LINES = 60
    }
}
