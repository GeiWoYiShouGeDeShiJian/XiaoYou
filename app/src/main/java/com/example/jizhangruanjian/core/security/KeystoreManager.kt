package com.example.jizhangruanjian.core.security
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class KeystoreManager @Inject constructor() {
    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "jizhang_sqlcipher_master"
        private const val GCM_TAG_BITS = 128
        private val FIXED_IV = ByteArray(12)
    }
    private fun getOrCreateMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(false)
                .build()
        )
        return gen.generateKey()
    }
    // R6-1 密钥不落盘：每次启动从 Keystore 主密钥派生稳定口令（同密钥+固定IV+固定种子=同口令）
    fun deriveSqlCipherPassphrase(): CharArray {
        val key = getOrCreateMasterKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, FIXED_IV))
        val cipherText = cipher.doFinal("jizhang_sqlcipher_seed_v1".toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipherText, Base64.NO_WRAP).toCharArray()
    }
}
