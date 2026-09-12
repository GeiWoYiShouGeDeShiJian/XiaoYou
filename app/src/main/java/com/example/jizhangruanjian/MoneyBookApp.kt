package com.example.jizhangruanjian
import android.app.Application
import android.util.Log
import com.example.jizhangruanjian.core.database.AppDatabase
import com.example.jizhangruanjian.core.database.DatabaseSeed
import com.example.jizhangruanjian.core.work.NotificationHelper
import com.example.jizhangruanjian.core.work.RecurringCatchUp
import com.example.jizhangruanjian.core.work.RecurringScheduler
import com.example.jizhangruanjian.core.work.TrashCleanupScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
private const val TAG = "MoneyBookApp"
@HiltAndroidApp
class MoneyBookApp : Application() {
    @Inject lateinit var appDatabase: AppDatabase
    @Inject lateinit var databaseSeed: DatabaseSeed
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        RecurringScheduler.schedule(this) // R7 每日 00:30 周期补跑
        TrashCleanupScheduler.schedule(this) // R3 每日 03:00 回收站清理
        Log.i(TAG, "onCreate")
        // 临时确认加密库可打开（R6-1），正式版移除 + 启动补跑（R7）
        Thread {
            try {
                appDatabase.openHelper.writableDatabase
                databaseSeed.seedIfNeeded()
                runBlocking { RecurringCatchUp(appDatabase).catchUp(System.currentTimeMillis()) }
                Log.i(TAG, "加密数据库已打开，周期补跑完成")
            } catch (e: Exception) {
                Log.e(TAG, "加密数据库打开失败", e)
            }
        }.start()
    }
}
