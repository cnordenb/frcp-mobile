package net.frcp.app.rules

class FunctionSpan(val line: Int, val name: String, val length: Int)

class FunctionSpans(masked: String) {
    private val lines = masked.split("\n")

    fun all(): List<FunctionSpan> = lines.indices.mapNotNull { start ->
        val name = DECLARATION.find(lines[start])?.groupValues?.get(1) ?: return@mapNotNull null
        FunctionSpan(start + 1, name, lastLine(start) - start + 1)
    }

    private fun indent(line: String) = line.length - line.trimStart().length

    private fun lastLine(start: Int): Int {
        var round = 0
        var curly = 0
        var body = false
        for (n in start until lines.size) {
            for (c in lines[n]) {
                when (c) {
                    '(' -> round++
                    ')' -> round--
                    '{' -> if (round == 0) curly++
                    '}' -> if (round == 0) curly--
                }
                if (round == 0 && (c == '{' || c == '=')) body = true
                if (round == 0 && curly == 0 && c == '}') return n
            }
            if (round == 0 && curly == 0 && (!body || endsExpression(start, n))) return n
        }
        return lines.lastIndex
    }

    private fun endsExpression(start: Int, n: Int): Boolean {
        val next = lines.drop(n + 1).firstOrNull { it.isNotBlank() } ?: return true
        return indent(next) <= indent(lines[start])
    }

    private companion object {
        val DECLARATION = Regex("""^\s*(?:@\w+\s+)*(?:[a-z]+\s+)*fun\s+(?:<[^>]+>\s+)?(?:[\w<>?, .]+\.)?(\w+)\s*\(""")
    }
}
