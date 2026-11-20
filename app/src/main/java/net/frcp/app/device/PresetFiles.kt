package net.frcp.app.device

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ReadFile(val name: String?, val text: String?)

interface PresetFiles {
    suspend fun write(uri: Uri, text: String): Boolean

    suspend fun read(uri: Uri, max: Int): ReadFile
}

class DocumentFiles(private val resolver: ContentResolver) : PresetFiles {
    override suspend fun write(uri: Uri, text: String) = withContext(Dispatchers.IO) {
        runCatching {
            val truncating = runCatching { resolver.openOutputStream(uri, "wt") }.getOrNull()
            val out = truncating ?: resolver.openOutputStream(uri, "w")
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
