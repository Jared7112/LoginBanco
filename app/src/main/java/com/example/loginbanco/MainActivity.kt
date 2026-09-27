package com.example.loginbanco

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textView = findViewById(R.id.tvFingerprintHint)
        imageView = findViewById(R.id.ivFingerprint)

        val fingerprintContainer = findViewById<View>(R.id.fingerprintContainer)

        fingerprintContainer.setOnClickListener {
            scanButton(it)
        }

        findViewById<MaterialButton>(R.id.btnPinLogin).setOnClickListener {
            navigateToDashboard()
        }

        val helpTextView = findViewById<TextView>(R.id.tvHelp)
        val helpText = getString(R.string.help_footer)
        val spannable = SpannableString(helpText)
        val ayudaIndex = helpText.indexOf("Ayuda")
        if (ayudaIndex != -1) {
            val tealColor = ContextCompat.getColor(this, R.color.nova_teal)
            spannable.setSpan(
                ForegroundColorSpan(tealColor),
                ayudaIndex,
                ayudaIndex + "Ayuda".length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        helpTextView.text = spannable
        helpTextView.setOnClickListener {
            Toast.makeText(this, "Abriendo sección de ayuda...", Toast.LENGTH_SHORT).show()
        }
    }

    fun scanButton(_view: View) {
        textView.text = getString(R.string.scanning_fingerprint)
        textView.setTextColor(ContextCompat.getColor(this, R.color.nova_teal))

        imageView.animate()
            .scaleX(1.15f)
            .scaleY(1.15f)
            .setDuration(300)
            .withEndAction {
                imageView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(300)
                    .withEndAction {
                        checkAndLaunchBiometricPrompt()
                    }
                    .start()
            }
            .start()
    }

    private fun checkAndLaunchBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt()
        } else {
            Toast.makeText(
                this,
                "Simulando autenticación (No hay huellas configuradas)",
                Toast.LENGTH_SHORT
            ).show()
            handleAuthenticationSuccess()
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    textView.text = getString(R.string.fingerprint_error)
                    textView.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    handleAuthenticationSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    textView.text = getString(R.string.fingerprint_failed)
                    textView.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_orange_light))
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("NovaBanco - Biometría")
            .setSubtitle("Autentícate con tu huella digital")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun handleAuthenticationSuccess() {
        textView.text = getString(R.string.fingerprint_success)
        textView.setTextColor(ContextCompat.getColor(this, R.color.nova_teal))

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToDashboard()
        }, 1200)
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
