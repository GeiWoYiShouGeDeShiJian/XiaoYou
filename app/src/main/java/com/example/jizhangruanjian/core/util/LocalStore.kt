package com.example.jizhangruanjian.core.util
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File
// 本地直存：优先写入用户选择的目录（SAF），未选择时写 Download/<子目录>（API29+ 走 MediaStore 免权限）
object LocalStore {
    data class LocalFile(val name: String, val uri: Any, val time: Long, val saf: Boolean = false)
    fun takePersist(context: Context, uri: Uri) = runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
    fun dirName(context: Context, uri: Uri): String = runCatching {
        context.contentResolver.query(uri, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    }.getOrNull() ?: uri.lastPathSegment ?: ""
    fun save(context: Context, sub: String, dirUri: String?, fileName: String, mime: String, bytes: ByteArray): String? = try {
        if (dirUri != null) {
            val tree = Uri.parse(dirUri)
            val doc = DocumentsContract.createDocument(context.contentResolver, DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree)), mime, fileName) ?: return null
            context.contentResolver.openOutputStream(doc)?.use { it.write(bytes) }
            "${dirName(context, tree)}/$fileName"
        } else if (Build.VERSION.SDK_INT >= 29) {
            val values = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); put(MediaStore.MediaColumns.MIME_TYPE, mime); put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/$sub") }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            "Download/$sub/$fileName"
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), sub)
            dir.mkdirs()
            File(dir, fileName).writeBytes(bytes)
            "Download/$sub/$fileName"
        }
    } catch (e: Exception) { null }
    fun list(context: Context, sub: String, dirUri: String?, suffix: String): List<LocalFile> = try {
        if (dirUri != null) {
            val tree = Uri.parse(dirUri)
            val result = mutableListOf<LocalFile>()
            context.contentResolver.query(DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree)), arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_LAST_MODIFIED), null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val name = c.getString(1)
                    if (name.endsWith(suffix)) result.add(LocalFile(name, DocumentsContract.buildDocumentUriUsingTree(tree, c.getString(0)), c.getLong(2), saf = true))
                }
            }
            result.sortedByDescending { it.name }
        } else if (Build.VERSION.SDK_INT >= 29) {
            val result = mutableListOf<LocalFile>()
            context.contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED), "${MediaStore.MediaColumns.RELATIVE_PATH}=? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?", arrayOf("Download/$sub/", "%$suffix"), null)?.use { c ->
                while (c.moveToNext()) {
                    val name = c.getString(1)
                    result.add(LocalFile(name, Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, c.getLong(0).toString()), c.getLong(2) * 1000))
                }
            }
            result.sortedByDescending { it.name }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), sub)
            dir.listFiles { f -> f.name.endsWith(suffix) }?.sortedByDescending { it.name }?.map { LocalFile(it.name, it, it.lastModified()) } ?: emptyList()
        }
    } catch (e: Exception) { emptyList() }
    fun delete(context: Context, file: LocalFile): Boolean = try {
        when {
            file.saf -> DocumentsContract.deleteDocument(context.contentResolver, file.uri as Uri)
            file.uri is Uri -> context.contentResolver.delete(file.uri as Uri, null, null) > 0
            else -> (file.uri as File).delete()
        }
    } catch (e: Exception) { false }
    fun readBytes(context: Context, file: LocalFile): ByteArray? = try {
        when (file.uri) { is Uri -> context.contentResolver.openInputStream(file.uri as Uri)?.use { it.readBytes() }; else -> (file.uri as File).readBytes() }
    } catch (e: Exception) { null }
}
