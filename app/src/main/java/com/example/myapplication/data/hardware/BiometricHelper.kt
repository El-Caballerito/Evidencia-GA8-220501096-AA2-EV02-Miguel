package com.example.myapplication.data.hardware

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class BiometricHelper(private val activity: FragmentActivity) {

    fun autenticarBiometria(
        metodoSeleccionado: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError("Error biométrico: $errString")
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Verificación fallida. Intente nuevamente.")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verificación Biométrica - Growth")
            .setSubtitle("Autenticación obligatoria por $metodoSeleccionado")
            .setNegativeButtonText("Cancelar")
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
