package com.example.jizhangruanjian.core.security
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.fragment.app.FragmentActivity
import com.example.jizhangruanjian.core.database.AppSettingDao
import com.example.jizhangruanjian.data.model.AppSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class AppLockManager @Inject constructor(private val appSettingDao: AppSettingDao) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    suspend fun isAppLockEnabled(): Boolean = appSettingDao.get(KEY_ENABLED)?.value == "1"
    suspend fun setAppLockEnabled(enabled: Boolean) {
        scope.launch { if (enabled) appSettingDao.upsert(AppSetting(KEY_ENABLED, "1")) else appSettingDao.delete(KEY_ENABLED) }
    }
    fun isHardwareSupported(context: android.content.Context): Boolean = isSupported(context)
    fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit, onError: () -> Unit) {
        val prompt = BiometricPrompt(activity, androidx.core.content.ContextCompat.getMainExecutor(activity), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { onSuccess() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON && errorCode != BiometricPrompt.ERROR_CANCELED) onError() }
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setAllowedAuthenticators(BIOMETRIC_WEAK).setTitle("应用锁").setSubtitle("请验证指纹/面容以进入").setNegativeButtonText("取消").build())
    }
    companion object {
        const val KEY_ENABLED = "app_lock_enabled"
        fun isSupported(context: android.content.Context): Boolean =
            BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
}