package com.example.jizhangruanjian.core.backup
import android.content.Context
import android.database.Cursor
import android.util.Base64
import androidx.sqlite.db.SupportSQLiteStatement
import com.example.jizhangruanjian.core.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import java.security.SecureRandom
// P14 换机迁移：表级明文 dump(JSON) 后 AES-GCM 包裹，摆脱 SQLCipher 设备绑定密钥，可真正跨设备还原
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDatabase: AppDatabase
) {
    companion object {
        private val MAGIC = byteArrayOf(0x4A, 0x5A, 0x42, 0x4B) // JZBK
        private const val ITERATIONS = 200_000
        private const val KEY_BITS = 256
        private const val TAG_BITS = 128
        private val TABLES = listOf(
            "ledger", "account_group", "category", "tag", "tag_group", "member",
            "account", "ledger_account", "budget", "recurring_template", "transactions",
            "transaction_image", "transaction_tag", "app_setting", "record_template",
            "merchant", "merchant_group"
        )
    }
    // 导出：逐表读出全部行 → JSON → AES 包裹
    suspend fun export(password: String): ByteArray {
        val db = appDatabase.openHelper.writableDatabase
        val tablesJson = JSONArray()
        TABLES.forEach { t ->
            val tableJson = JSONObject().put("name", t)
            val rows = JSONArray()
            db.query("SELECT * FROM $t").use { c ->
                val cols = c.columnNames
                tableJson.put("cols", JSONArray(cols.toList()))
                while (c.moveToNext()) {
                    val r = JSONArray()
                    cols.forEachIndexed { i, _ -> r.put(cellToJson(c, i)) }
                    rows.put(r)
                }
            }
            tablesJson.put(tableJson.put("rows", rows))
        }
        return encrypt(password, JSONObject().put("tables", tablesJson).toString().toByteArray(Charsets.UTF_8))
    }
    // 导入：解密 → 清空重建各表（事务内，临时关外键），错密码抛异常
    suspend fun import(bk: ByteArray, password: String): String? {
        val payload = try { decrypt(password, bk) } catch (e: Exception) { return "密码错误或文件无效" }
        return try {
            val root = JSONObject(String(payload, Charsets.UTF_8))
            val db = appDatabase.openHelper.writableDatabase
            db.execSQL("PRAGMA foreign_keys=OFF")
            db.beginTransaction()
            try {
                val all = root.getJSONArray("tables")
                for (i in 0 until all.length()) {
                    val table = all.getJSONObject(i)
                    val name = table.getString("name")
                    db.execSQL("DELETE FROM $name")
                    val colsArr = table.getJSONArray("cols")
                    val cols = (0 until colsArr.length()).map { colsArr.getString(it) }
                    val sql = "INSERT INTO $name (${cols.joinToString(",")}) VALUES (${cols.joinToString(",") { "?" }})"
                    val rows = table.getJSONArray("rows")
                    for (j in 0 until rows.length()) {
                        val stmt = db.compileStatement(sql)
                        val r = rows.getJSONArray(j)
                        for (k in 0 until r.length()) bind(stmt, r.getJSONObject(k), k + 1)
                        stmt.executeInsert()
                        stmt.close()
                    }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
                db.execSQL("PRAGMA foreign_keys=ON")
            }
            null
        } catch (e: Exception) { "导入失败：${e.message}" }
    }
    private fun bind(stmt: SupportSQLiteStatement, cell: JSONObject, idx: Int) {
        when (cell.getString("t")) {
            "n" -> stmt.bindNull(idx)
            "i" -> stmt.bindLong(idx, cell.getLong("v"))
            "r" -> stmt.bindDouble(idx, cell.getDouble("v"))
            "s" -> stmt.bindString(idx, cell.getString("v"))
            else -> stmt.bindBlob(idx, Base64.decode(cell.getString("v"), Base64.NO_WRAP))
        }
    }
    private fun cellToJson(c: Cursor, i: Int): JSONObject {
        val out = JSONObject()
        when (c.getType(i)) {
            Cursor.FIELD_TYPE_NULL -> out.put("t", "n")
            Cursor.FIELD_TYPE_INTEGER -> out.put("t", "i").put("v", c.getLong(i))
            Cursor.FIELD_TYPE_FLOAT -> out.put("t", "r").put("v", c.getDouble(i))
            Cursor.FIELD_TYPE_STRING -> out.put("t", "s").put("v", c.getString(i))
            else -> out.put("t", "b").put("v", Base64.encodeToString(c.getBlob(i), Base64.NO_WRAP))
        }
        return out
    }
    private fun encrypt(password: String, payload: ByteArray): ByteArray {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        val enc = cipher.doFinal(payload)
        val out = ByteArrayOutputStream()
        out.write(MAGIC); out.write(salt); out.write(iv)
        out.write(ByteArray(4).also { it[0] = (enc.size shr 24).toByte(); it[1] = (enc.size shr 16).toByte(); it[2] = (enc.size shr 8).toByte(); it[3] = enc.size.toByte() })
        out.write(enc)
        return out.toByteArray()
    }
    private fun decrypt(password: String, bk: ByteArray): ByteArray {
        val input = ByteArrayInputStream(bk)
        val magic = ByteArray(4).also { input.read(it) }
        if (!magic.contentEquals(MAGIC)) throw IllegalArgumentException("bad magic")
        val salt = ByteArray(16).also { input.read(it) }
        val iv = ByteArray(12).also { input.read(it) }
        val len = ByteArray(4).also { input.read(it) }.let { intFrom(it) }
        val enc = ByteArray(len).also { input.read(it) }
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(enc)
    }
    private fun derive(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)).encoded
    private fun intFrom(b: ByteArray): Int = ((b[0].toInt() and 0xFF) shl 24) or ((b[1].toInt() and 0xFF) shl 16) or ((b[2].toInt() and 0xFF) shl 8) or (b[3].toInt() and 0xFF)
}