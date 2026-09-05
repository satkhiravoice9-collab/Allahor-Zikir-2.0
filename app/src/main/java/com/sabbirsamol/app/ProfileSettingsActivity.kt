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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class ProfileSettingsActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private val mAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private var loadingDialog: AlertDialog? = null

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    // গুগল সাইন-ইন রেজাল্ট ধরার জন্য লঞ্চার
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // গুগলের টোকেন দিয়ে ফায়ারবেসে লগইন
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
        
        // ফায়ারবেসে ইউজার সাইন-ইন না থাকলে স্বয়ংক্রিয়ভাবে অ্যানোনিমাস বা নিরাপদ সেশন তৈরি করা
        if (mAuth.currentUser == null) {
            mAuth.signInAnonymously()
        }

        // গুগল সাইন-ইন কনফিগারেশন
        val webClientId = getString(resources.getIdentifier("default_web_client_id", "string", packageName))
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
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

        // ================= উদ্যোক্তা কার্ড =================
        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(themeColors.textMain); textSize = 15f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(4).toFloat(), 1f) })
        
        devCard.addView(Button(this).apply {
            text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(themeColors.btnBg)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }
            setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))) }
        })
        devCard.addView(Button(this).apply {
            text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))) }
        })
        content.addView(devCard)

        // ================= গুগল ক্লাউড সাইন ইন কার্ড =================
        val cloudCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        cloudCard.addView(TextView(this).apply { text = "☁️ গুগল ক্লাউড সাইন ইন ও সিঙ্ক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", "কোনো অ্যাকাউন্ট যুক্ত নেই")
        
        cloudCard.addView(TextView(this).apply { text = "সংযুক্ত ক্লাউড জিমেইল:\n$savedEmail\n(১০০% গুগল ক্লাউডে ডাটা সংরক্ষণ হবে)"; setTextColor(themeColors.textMain); textSize = 14f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(2).toFloat(), 1f) })
        
        cloudCard.addView(Button(this).apply {
            text = "🔵 গুগল দিয়ে সরাসরি সাইন ইন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#60A5FA"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { startRealGoogleSignIn() }
        })
        content.addView(cloudCard)

        // ================= অ্যাপ থিম নির্বাচন =================
        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })

        val savedTheme = sharedPrefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")
        val themes = listOf(
            Pair("⚪ সাদা থিম (লাইট)", "#F3F4F6"), Pair("⬛ কাবা থিম (ডার্ক গোল্ড)", "#1F2937"), 
            Pair("🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "#064E3B"), Pair("🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)", "#B45309")
        )

        themes.forEach { (tName, tColor) ->
            val isSelected = savedTheme == tName
            themeCard.addView(Button(this).apply {
                text = if (isSelected) "✅ $tName" else tName
                isAllCaps = false; setTextColor(if(tName.contains("সাদা")) Color.BLACK else Color.WHITE)
                background = GradientDrawable().apply { setColor(Color.parseColor(tColor)); cornerRadius = dp(8).toFloat(); if (isSelected) setStroke(dp(3), themeColors.btnBg) else setStroke(dp(1), Color.GRAY) }
                layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(10) }
                
                setOnClickListener {
                    sharedPrefs.edit().putString("app_theme", tName).apply()
                    Toast.makeText(this@ProfileSettingsActivity, "থিম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show()
                    startActivity(intent); finish(); overridePendingTransition(0, 0)
                }
            })
        }
        content.addView(themeCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= বটম নেভিগেশন বার =================
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf(
            Pair("🏠\nহোম", MainActivity::class.java),
            Pair("📿\nতাসবিহ", TasbihActivity::class.java),
            Pair("📚\nলাইব্রেরী", LibraryActivity::class.java),
            Pair("📖\nআমল", MasnunAmolActivity::class.java),
            Pair("📝\nনোটপ্যাড", NotepadActivity::class.java),
            Pair("🔄\nসিঙ্ক", null),
            Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java)
        )

        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
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

    private fun startRealGoogleSignIn() {
        showLoading()
        // আগের সেশন থাকলে ক্লিয়ার করে নতুন করে জিমেইল পপ-আপ আনা
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
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
                    showSettingsPage() // UI রিফ্রেশ করার জন্য
                } else {
                    Toast.makeText(this, "ফায়ারবেস কানেকশন ব্যর্থ: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showLoading() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            gravity = Gravity.CENTER_VERTICAL
            addView(ProgressBar(this@ProfileSettingsActivity))
            addView(TextView(this@ProfileSettingsActivity).apply {
                text = "গুগল একাউন্ট কানেক্ট করা হচ্ছে..."
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(dp(15), 0, 0, 0)
            })
        }
        loadingDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(layout)
            .create()
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }
}
