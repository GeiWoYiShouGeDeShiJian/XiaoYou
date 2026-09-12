package com.example.jizhangruanjian.core.database
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
class SqlCipherSupportFactory(passphrase: CharArray) : SupportSQLiteOpenHelper.Factory {
    private val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper {
        return factory.create(configuration)
    }
}
