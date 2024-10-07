package com.rnbio

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import android.util.Log
val KEY_STORE_NAME = "AndroidKeyStore"
val BIOMETRIC_KEY_ALIAS = "MyBiometricKeyAlias"
class BiometricModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val biometricPrompt by lazy {
            val activity = reactContext.currentActivity as FragmentActivity // گرفتن FragmentActivity

        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    
                    promise.resolve("Authentication successful")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    
                    promise.reject("Error", errString.toString())
                }

                override fun onAuthenticationFailed() {
                    
                    promise.reject("Error", "Authentication failed")
                }
            }
        )
    }

    private lateinit var promise: Promise

    override fun getName() = "BiometricModule";

    @ReactMethod fun createBiometricKey(promise: Promise) {
        try {
            

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE_NAME)
            val keyGenParameterSpec =  KeyGenParameterSpec.Builder(
                    BIOMETRIC_KEY_ALIAS, 
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true) 
                    .setUserAuthenticationValidityDurationSeconds(-1) 
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .setKeySize(256)
                    .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            promise.resolve("Biometric key created successfully.")

        } catch (e: Exception) {
            e.printStackTrace()
            promise.reject("Error", "Failed to create biometric key.",e)
        }
    }
@ReactMethod
fun isKeyAvailable(promise: Promise) {
    try {
        
          val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            promise.resolve(true)
    } catch (e: Exception) {
        
        promise.resolve(false)
    }
}

@ReactMethod
fun isBiometricAvailable(promise: Promise) {
    try {
        val biometricManager = BiometricManager.from(reactApplicationContext)
        // Use the newer method for checking both hardware and enrolled biometrics
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric hardware is available and a biometric is enrolled
                promise.resolve(true)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Either hardware is not available or no biometrics are enrolled
                promise.resolve(false)
            }
            else -> {
                // Catch any unexpected cases
                promise.resolve(false)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        promise.reject("Error", "Failed to check biometric availability: ${e.message}", e)
    }
}
    
  
@ReactMethod
fun authenticateBiometric(promise: Promise) {
    this.promise = promise
val activity = reactApplicationContext.currentActivity as FragmentActivity

    // Ensure everything runs on the main thread
activity.runOnUiThread {
        try {
            
            val cipher = getCipher()
            val secretKey = getSecretKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()

            // Trigger the biometric prompt
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))

        } catch (e: Exception) {
            promise.reject("Error", "Failed to authenticate: ${e.message}", e)
        }
    }
}
    

    
 private fun getCipher(): Cipher {
    return Cipher.getInstance(
        "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
    )
}

    
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEY_STORE_NAME)
        keyStore.load(null)
        return keyStore.getKey(BIOMETRIC_KEY_ALIAS, null) as SecretKey
    }
}