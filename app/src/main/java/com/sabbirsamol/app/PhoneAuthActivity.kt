package com.sabbirsamol.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : Activity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private var verificationId: String? = null
    private lateinit var phoneInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var sendButton: Button
    private lateinit var verifyButton: Button

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
            setBackgroundColor(Color.parseColor("#F3F4F6"))
        }

        root.addView(TextView(this).apply {
            text = "📱 মোবাইল নম্বর দিয়ে নিরাপদ লগইন"
            textSize = 21f
            setTextColor(Color.parseColor("#047857"))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        })

        root.addView(TextView(this).apply {
            text = "আপনার নম্বরে OTP কোড পাঠানো হবে। যাচাই সফল হলে আপনার আগের ডাটাও একই অ্যাকাউন্টে যুক্ত থাকবে।"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(20))
        })

        phoneInput = EditText(this).apply {
            hint = "মোবাইল নম্বর (01XXXXXXXXX)"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(1), Color.LTGRAY)
                cornerRadius = dp(8).toFloat()
            }
        }
        root.addView(phoneInput, LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(12) })

        sendButton = Button(this).apply {
            text = "OTP পাঠান"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#047857"))
                cornerRadius = dp(8).toFloat()
            }
            setOnClickListener { sendOtp() }
        }
        root.addView(sendButton, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(14) })

        codeInput = EditText(this).apply {
            hint = "৬ সংখ্যার OTP লিখুন"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            textSize = 16f
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                setStroke(dp(1), Color.LTGRAY)
                cornerRadius = dp(8).toFloat()
            }
        }
        root.addView(codeInput, LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(12) })

        verifyButton = Button(this).apply {
            text = "OTP যাচাই করে লগইন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#065F46"))
                cornerRadius = dp(8).toFloat()
            }
            isEnabled = false
            setOnClickListener { verifyOtp() }
        }
        root.addView(verifyButton, LinearLayout.LayoutParams(-1, dp(48)))

        setContentView(root)
    }

    private fun normalizePhone(raw: String): String? {
        val value = raw.trim().replace(" ", "").replace("-", "")
        return when {
            Regex("^01[3-9][0-9]{8}$").matches(value) -> "+88$value"
            Regex("^\\+8801[3-9][0-9]{8}$").matches(value) -> value
            else -> null
        }
    }

    private fun legacyMobile(e164: String): String {
        return if (e164.startsWith("+880")) "0" + e164.removePrefix("+880") else e164
    }

    private fun sendOtp() {
        val phone = normalizePhone(phoneInput.text.toString())
        if (phone == null) {
            phoneInput.error = "সঠিক বাংলাদেশি মোবাইল নম্বর দিন"
            return
        }

        sendButton.isEnabled = false
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithCredential(credential)
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            sendButton.isEnabled = true
            val msg = e.message ?: "অজানা সমস্যা"
            Toast.makeText(this@PhoneAuthActivity, "OTP পাঠানো যায়নি: " + msg, Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            verificationId = id
            sendButton.isEnabled = true
            verifyButton.isEnabled = true
            Toast.makeText(this@PhoneAuthActivity, "OTP পাঠানো হয়েছে।", Toast.LENGTH_SHORT).show()
            codeInput.requestFocus()
        }
    }

    private fun verifyOtp() {
        val id = verificationId
        val code = codeInput.text.toString().trim()
        if (id.isNullOrBlank() || code.length != 6) {
            codeInput.error = "সঠিক ৬ সংখ্যার OTP দিন"
            return
        }
        signInWithCredential(PhoneAuthProvider.getCredential(id, code))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        verifyButton.isEnabled = false
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val mobile = user?.phoneNumber?.let { legacyMobile(it) }
                    ?: normalizePhone(phoneInput.text.toString())?.let { legacyMobile(it) }

                if (user == null || mobile == null) {
                    verifyButton.isEnabled = true
                    Toast.makeText(this, "ব্যবহারকারীর তথ্য পাওয়া যায়নি।", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                getSharedPreferences("AppSettings", MODE_PRIVATE).edit()
                    .putString("user_mobile", mobile)
                    .putString("user_uid", user.uid)
                    .apply()

                AuthManager.migrateLegacyData(this, mobile) { ok, error ->
                    if (ok) {
                        Toast.makeText(this, "লগইন সফল। আপনার ডাটা অ্যাকাউন্টে যুক্ত হয়েছে।", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        verifyButton.isEnabled = true
                        val msg = error ?: "অজানা সমস্যা"
                        Toast.makeText(this, "লগইন হয়েছে, কিন্তু পুরোনো ডাটা যুক্ত করা যায়নি: " + msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .addOnFailureListener {
                verifyButton.isEnabled = true
                val msg = it.message ?: "OTP সঠিক নয়"
                Toast.makeText(this, "লগইন ব্যর্থ: " + msg, Toast.LENGTH_LONG).show()
            }
    }
}
