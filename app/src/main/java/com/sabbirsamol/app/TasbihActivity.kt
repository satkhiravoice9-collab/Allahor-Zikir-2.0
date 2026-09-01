package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import androidx.activity.ComponentActivity

/** Tasbih only. Home screen remains untouched. */
class TasbihActivity : ComponentActivity() {
    private var count = 0
    private var selected = "সুবহানাল্লাহ"
    private lateinit var countText: TextView
    private lateinit var nameText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(c: Int, r: Int) = GradientDrawable().apply { setColor(c); cornerRadius = dp(r).toFloat() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("count", 0)
        selected = prefs.getString("selected", "সুবহানাল্লাহ") ?: "সুবহানাল্লাহ"
        build()
    }

    override fun onResume() {
        super.onResume()
        if (::nameText.isInitialized) {
            selected = prefs.getString("selected", selected) ?: selected
            count = prefs.getInt("count", count)
            refresh()
        }
    }

    private fun build() {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(7, 45, 31)) }

        val kaaba = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = bg(Color.rgb(11, 72, 48), 26)
            addView(TextView(this@TasbihActivity).apply { text = "🕋"; textSize = 66f; gravity = Gravity.CENTER })
            addView(TextView(this@TasbihActivity).apply { text = "الْكَعْبَةُ الْمُشَرَّفَةُ"; textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.rgb(238,210,130)); setTypeface(null,Typeface.BOLD) })
        }
        root.addView(kaaba, FrameLayout.LayoutParams(-1, dp(130)).apply { leftMargin=dp(8); rightMargin=dp(8); topMargin=dp(8) })
        kaaba.setOnTouchListener { _, e -> if(e.action==MotionEvent.ACTION_UP) increment(); true }

        nameText = TextView(this).apply {
            text=selected; textSize=22f; setTextColor(Color.WHITE); setTypeface(null,Typeface.BOLD)
            gravity=Gravity.CENTER_VERTICAL; setPadding(dp(12),0,dp(4),0)
        }
        root.addView(nameText, FrameLayout.LayoutParams(dp(230),dp(55)).apply{leftMargin=dp(8);topMargin=dp(143)})

        root.addView(Button(this).apply { text="↻"; textSize=25f; contentDescription="রিসেট"; setOnClickListener{resetCount()} }, FrameLayout.LayoutParams(dp(62),dp(52)).apply{gravity=Gravity.TOP or Gravity.END;rightMargin=dp(8);topMargin=dp(143)})

        countText=TextView(this).apply{textSize=92f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER}
        root.addView(countText,FrameLayout.LayoutParams(-1,dp(150)).apply{topMargin=dp(205)})

        val folder=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;background=bg(Color.rgb(15,104,69),24);setPadding(dp(12),dp(6),dp(12),dp(6))}
        folder.addView(TextView(this).apply{text="📁  জিকির";textSize=18f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(58),1f))
        folder.addView(TextView(this).apply{text="›";textSize=30f;setTextColor(Color.rgb(238,210,130));gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(45),dp(58)))
        folder.setOnClickListener{startActivity(Intent(this,ZikirManagerActivity::class.java))}
        root.addView(folder,FrameLayout.LayoutParams(-1,dp(70)).apply{gravity=Gravity.BOTTOM;leftMargin=dp(8);rightMargin=dp(8);bottomMargin=dp(8)})

        val touchLayer=FrameLayout(this).apply{setBackgroundColor(Color.TRANSPARENT);isClickable=true;setOnTouchListener{_,e->if(e.action==MotionEvent.ACTION_UP)increment();true}}
        root.addView(touchLayer,FrameLayout.LayoutParams(-1,-1).apply{topMargin=dp(195);bottomMargin=dp(78)})

        setContentView(root);refresh()
    }

    private fun increment(){count++;prefs.edit().putInt("count",count).putString("selected",selected).apply();refresh();if(count%100==0)vibrate()}
    private fun refresh(){countText.text=count.toString();nameText.text=selected}
    private fun vibrate(){val v=getSystemService(VIBRATOR_SERVICE) as Vibrator;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE))else@Suppress("DEPRECATION")v.vibrate(100)}
    private fun resetCount(){AlertDialog.Builder(this).setTitle("রিসেট").setMessage("বর্তমান গণনা শূন্য করবেন?").setNegativeButton("না",null).setPositiveButton("হ্যাঁ"){_,_->count=0;prefs.edit().putInt("count",0).apply();refresh()}.show()}
}
