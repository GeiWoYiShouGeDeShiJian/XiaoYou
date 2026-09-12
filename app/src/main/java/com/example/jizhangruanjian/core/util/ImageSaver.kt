package com.example.jizhangruanjian.core.util
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
object ImageSaver {
    private const val MAX_SIZE = 1024
    fun saveImage(context: Context, uri: Uri, quality: Int = 90): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("无法读取图片")
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > MAX_SIZE || bounds.outHeight / sample > MAX_SIZE) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            ?: throw IllegalStateException("图片解析失败")
        val file = File(context.filesDir, "img_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        bmp.recycle()
        return file.absolutePath
    }
    fun deleteImage(path: String) { runCatching { File(path).delete() } }
    fun loadBitmap(path: String, reqSize: Int = 512): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > reqSize || bounds.outHeight / sample > reqSize) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}