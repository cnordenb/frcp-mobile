package net.frcp.app.rules

class SourceMask(private val text: String) {
    private val out = StringBuilder()
    private var i = 0

    fun masked(): String {
        code(false)
        return out.toString()
    }

    private fun code(inTemplate: Boolean) {
        var depth = 0
        while (i < text.length) {
            val c = text[i]
            when {
                text.startsWith(RAW_QUOTE, i) -> string(RAW_QUOTE)
                c == '"' -> string(QUOTE)
                c == '\'' -> char()
                c == '}' && inTemplate && depth == 0 -> return
                else -> {
                    if (c == '{') depth++
                    if (c == '}') depth--
                    keep()
                }
            }
        }
    }

    private fun keep() {
        out.append(text[i++])
    }

    private fun hide() {
        if (i >= text.length) return
        out.append(if (text[i] == '\n') '\n' else ' ')
        i++
    }

    private fun string(quote: String) {
        repeat(quote.length) { keep() }
        while (i < text.length && !text.startsWith(quote, i)) {
            when {
                quote == QUOTE && text[i] == '\\' -> repeat(2) { hide() }
                text.startsWith(TEMPLATE, i) -> {
                    repeat(2) { hide() }
                    code(true)
                    hide()
                }
                else -> hide()
            }
        }
        repeat(quote.length) { if (i < text.length) keep() }
    }

    private fun char() {
        keep()
        while (i < text.length && text[i] != '\'') {
            if (text[i] == '\\') hide()
            hide()
        }
        if (i < text.length) keep()
    }

    private companion object {
        const val QUOTE = "\""
        const val RAW_QUOTE = "\"\"\""
        const val TEMPLATE = "\${"
    }
}
