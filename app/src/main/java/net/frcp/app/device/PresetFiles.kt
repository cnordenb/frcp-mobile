package net.frcp.app

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** A file read for a preset: its name, if it has one, and its text, null when it can't be read or is too long. */
class ReadFile(val name: String?, val text: String?)

/** The preset files the user picks to save to and load from. */
interface PresetFiles {
    /** Writes [text] to the file at [uri], and says whether it could. */
    suspend fun write(uri: Uri, text: String): Boolean

    /** Reads the file at [uri], up to [max] bytes of it. */
    suspend fun read(uri: Uri, max: Int): ReadFile
}

/** [PresetFiles] through Android's document providers, away from the screen's thread. */
class DocumentFiles(private val resolver: ContentResolver) : PresetFiles {
    override suspend fun write(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        runCatching {
            val out = runCatching { resolver.openOutputStream(uri, "wt") }.getOrNull() ?: resolver.openOutputStream(uri, "w")
            checkNotNull(out).use { it.write(text.toByteArray()) }
        }.isSuccess
    }

    override suspend fun read(uri: Uri, max: Int) = withContext(Dispatchers.IO) {
        val name = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()
        ReadFile(name, runCatching { resolver.openInputStream(uri)?.use { readText(it, max) } }.getOrNull())
    }

    /** The text of a file up to max bytes long, or null for a longer one. */
    private fun readText(input: InputStream, max: Int): String? {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        while (true) {
            val n = input.read(chunk)
            if (n < 0) break
            out.write(chunk, 0, n)
            if (out.size() > max) return null
        }
        return out.toByteArray().toString(Charsets.UTF_8)
    }
}
