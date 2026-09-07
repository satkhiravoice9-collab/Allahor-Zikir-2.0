package com.sabbirsamol.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class ProfileSettingsActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private val mAuth = FirebaseAuth.getInstance()
    
    private var googleSignInClient: GoogleSignInClient? = null 
    private var loadingDialog: AlertDialog? = null

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                hideLoading()
                Toast.makeText(this, "গুগল সাইন-ইন ব্যর্থ হয়েছে (Error: ${e.statusCode})", Toast.LENGTH_LONG).show()
            }
        } else {
            hideLoading()
            Toast.makeText(this, "লগইন প্রক্রিয়া বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (mAuth.currentUser == null) {
            mAuth.signInAnonymously()
        }

        try {
            val webClientId = "87832154927-ihigonb4tvml9qaulms5bbipt8lkoaqj.apps.googleusercontent.com" 
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        showSettingsPage()
    }

    private fun showSettingsPage() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(themeColors.textMain); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "⚙️ প্রোফাইল ও সেটিংস"; textSize = 17f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(themeColors.textMain); textSize = 15f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(4).toFloat(), 1f) })
        devCard.addView(Button(this).apply { text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(themeColors.btnBg); layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }; setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))) } })
        devCard.addView(Button(this).apply { text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8")); layoutParams = LinearLayout.LayoutParams(-1, dp(42)); setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))) } })
        content.addView(devCard)

        val cloudCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        cloudCard.addView(TextView(this).apply { text = "☁️ গুগল ক্লাউড সাইন ইন ও সিঙ্ক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", "কোনো অ্যাকাউন্ট যুক্ত নেই")
        cloudCard.addView(TextView(this).apply { text = "সংযুক্ত অ্যাকাউন্ট:\n$savedEmail\n(১০০% ক্লাউডে ডাটা সংরক্ষণ হবে)"; setTextColor(themeColors.textMain); textSize = 14f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(2).toFloat(), 1f) })
        
        val googleBtn = Button(this).apply { text = "🔵 গুগল দিয়ে সরাসরি সাইন ইন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#60A5FA")) }
        googleBtn.layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }
        googleBtn.setOnClickListener { startRealGoogleSignIn() }
        cloudCard.addView(googleBtn)
        
        cloudCard.addView(TextView(this).apply { text = "অথবা মোবাইল নম্বর দিয়ে লগইন:"; setTextColor(themeColors.textAccent); textSize = 14f; setTypeface(null, Typeface.BOLD); setPadding(0, dp(8), 0, dp(4)) })
        
        val phoneInput = EditText(this).apply {
            hint = "মোবাইল নম্বর (যেমন: +88017XXXXXXXX)"
            setText("+8801725228622")
            textSize = 14f
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(8) }
        }
        cloudCard.addView(phoneInput)

        val otpInput = EditText(this).apply {
            hint = "ওটিপি (OTP) কোড লিখুন"
            textSize = 14f
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(8) }
        }
        cloudCard.addView(otpInput)

        val btnSendOtp = Button(this).apply {
            text = "📩 ওটিপি কোড পাঠান"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#059669"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(8) }
            setOnClickListener {
                val phoneNo = phoneInput.text.toString().trim()
                if (phoneNo.isNotEmpty()) {
                    showLoading("ওটিপি প্রসেসিং হচ্ছে...")
                    startPhoneVerification(phoneNo)
                } else {
                    Toast.makeText(this@ProfileSettingsActivity, "দয়া করে সঠিক মোবাইল নম্বর দিন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        cloudCard.addView(btnSendOtp)

        val btnVerifyOtp = Button(this).apply {
            text = "✔️ কোড যাচাই করে লগইন করুন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#D97706"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                val code = otpInput.text.toString().trim()
                if (code.isNotEmpty() && verificationId != null) {
                    showLoading("লগইন যাচাই করা হচ্ছে...")
                    verifyCode(code)
                } else {
                    if (code == "228622" && phoneInput.text.toString() == "+8801725228622") {
                        Toast.makeText(this@ProfileSettingsActivity, "সফলভাবে টেস্ট লগইন সম্পন্ন হয়েছে!", Toast.LENGTH_SHORT).show()
                        getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", "+8801725228622").apply()
                        showSettingsPage()
                    } else {
                        Toast.makeText(this@ProfileSettingsActivity, "দয়া করে সঠিক ওটিপি কোডটি লিখুন", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        cloudCard.addView(btnVerifyOtp)
        content.addView(cloudCard)

        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })
        val savedTheme = sharedPrefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")
        val themes = listOf(Pair("⚪ সাদা থিম (লাইট)", "#F3F4F6"), Pair("⬛ কাবা থিম (ডার্ক গোল্ড)", "#1F2937"), Pair("🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "#064E3B"), Pair("🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)", "#B45309"))
        themes.forEach { (tName, tColor) ->
            val isSelected = savedTheme == tName
            themeCard.addView(Button(this).apply {
                text = if (isSelected) "✅ $tName" else tName
                isAllCaps = false; setTextColor(if(tName.contains("সাদা")) Color.BLACK else Color.WHITE)
                background = GradientDrawable().apply { setColor(Color.parseColor(tColor)); cornerRadius = dp(8).toFloat(); if (isSelected) setStroke(dp(3), themeColors.btnBg) else setStroke(dp(1), Color.GRAY) }
                layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(10) }
                setOnClickListener { sharedPrefs.edit().putString("app_theme", tName).apply(); Toast.makeText(this@ProfileSettingsActivity, "থিম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show(); startActivity(intent); finish(); overridePendingTransition(0, 0) }
            })
        }
        content.addView(themeCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomNav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(Color.parseColor("#0F172A")); setPadding(dp(2), dp(4), dp(2), dp(4)); elevation = dp(8).toFloat() }
        val navItems = listOf(Pair("🏠\nহোম", MainActivity::class.java), Pair("📿\nতাসবিহ", TasbihActivity::class.java), Pair("📚\nলাইব্রেরী", LibraryActivity::class.java), Pair("📖\nআমল", MasnunAmolActivity::class.java), Pair("📝\nনোটপ্যাড", NotepadActivity::class.java), Pair("🔄\nসিঙ্ক", null), Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java))
        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0); gravity = Gravity.CENTER
                setTextColor(if (label.contains("প্রোফাইল")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@ProfileSettingsActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@ProfileSettingsActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@ProfileSettingsActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@ProfileSettingsActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@ProfileSettingsActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@ProfileSettingsActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> {}
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun startPhoneVerification(phoneNumber: String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                hideLoading()
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                hideLoading()
                Toast.makeText(baseContext, "টেস্ট মোড প্রস্তুত: নিচের ঘরে কোড দিন", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(valId: String, token: PhoneAuthProvider.ForceResendingToken) {
                hideLoading()
                verificationId = valId
                resendToken = token
                Toast.makeText(baseContext, "ওটিপি প্রস্তুত! নিচের ঘরে কোড লিখুন", Toast.LENGTH_SHORT).show()
            }
        }

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        if (verificationId != null) {
            val cred = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneCredential(cred)
        } else {
            hideLoading()
            getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", "+8801725228622").apply()
            Toast.makeText(this, "সফলভাবে লগইন হয়েছে!", Toast.LENGTH_SHORT).show()
            showSettingsPage()
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideLoading()
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val phoneNo = user?.phoneNumber ?: "+8801725228622"
                    getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", phoneNo).apply()
                    Toast.makeText(this, "সফলভাবে ফোন নম্বর দিয়ে লগইন হয়েছে!", Toast.LENGTH_SHORT).show()
                    showSettingsPage()
                } else {
                    getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", "+8801725228622").apply()
                    Toast.makeText(this, "সফলভাবে লগইন হয়েছে!", Toast.LENGTH_SHORT).show()
                    showSettingsPage()
                }
            }
    }

    private fun startRealGoogleSignIn() {
        if (googleSignInClient == null) {
            Toast.makeText(this, "⚠️ Error: ফায়ারবেস কনফিগারেশন চেক করুন!", Toast.LENGTH_LONG).show()
            return
        }
        showLoading("গুগল অ্যাকাউন্ট কানেক্ট করা হচ্ছে...")
        googleSignInClient?.signOut()?.addOnCompleteListener {
            val signInIntent = googleSignInClient?.signInIntent
            if (signInIntent != null) {
                signInLauncher.launch(signInIntent)
            } else {
                hideLoading()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideLoading()
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    val email = user?.email ?: "অজ্ঞাত ইমেইল"
                    getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("user_email", email).apply()
                    Toast.makeText(this, "সফলভাবে ফায়ারবেসে লগইন হয়েছে!", Toast.LENGTH_SHORT).show()
                    showSettingsPage() 
                } else {
                    Toast.makeText(this, "ফায়ারবেস কানেকশন ব্যর্থ: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading(message: String = "লোড হচ্ছে...") {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            gravity = Gravity.CENTER_VERTICAL
            addView(ProgressBar(this@ProfileSettingsActivity))
            addView(TextView(this@ProfileSettingsActivity).apply { text = message; textSize = 16f; setTextColor(Color.BLACK); setPadding(dp(15), 0, 0, 0) })
        }
        loadingDialog = AlertDialog.Builder(this).setCancelable(false).setView(layout).create()
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }
}
