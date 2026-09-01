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
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

class TasbihActivity : ComponentActivity() {
    private var count=0
    private var selected="সুবহানাল্লাহ"
    private lateinit var countText:TextView
    private lateinit var nameText:TextView
    private lateinit var progressText:TextView
    private val prefs by lazy{getSharedPreferences("tasbih_only",Context.MODE_PRIVATE)}
    private val listPrefs by lazy{getSharedPreferences("tasbih_list",Context.MODE_PRIVATE)}
    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun box(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun target()=listPrefs.getInt("target_$selected",0)

    override fun onCreate(s:Bundle?){super.onCreate(s);selected=prefs.getString("selected","সুবহানাল্লাহ")?:"সুবহানাল্লাহ";count=prefs.getInt("count",0);showMain()}
    override fun onResume(){super.onResume();if(::nameText.isInitialized){selected=prefs.getString("selected",selected)?:selected;count=prefs.getInt("count",count);refresh()}}

    private fun showMain(){
        val root=FrameLayout(this).apply{setBackgroundColor(Color.rgb(7,45,31))}
        // The transparent counter view is behind every visible control; only the controls consume their own taps.
        val counter=View(this).apply{setOnTouchListener{_,e->if(e.action==MotionEvent.ACTION_UP)increment();true}}
        root.addView(counter,FrameLayout.LayoutParams(-1,-1))

        nameText=TextView(this).apply{text=selected;textSize=20f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL}
        root.addView(nameText,FrameLayout.LayoutParams(dp(230),dp(48)).apply{gravity=Gravity.TOP or Gravity.START;leftMargin=dp(10);topMargin=dp(4)})
        val reset=ImageButton(this).apply{setImageResource(android.R.drawable.ic_popup_sync);setColorFilter(Color.WHITE);setBackgroundColor(Color.TRANSPARENT);contentDescription="রিসেট";setOnClickListener{resetCount()}}
        root.addView(reset,FrameLayout.LayoutParams(dp(52),dp(52)).apply{gravity=Gravity.TOP or Gravity.END;rightMargin=dp(8);topMargin=dp(0)})

        val kaaba=TextView(this).apply{text="🕋";textSize=78f;gravity=Gravity.CENTER}
        root.addView(kaaba,FrameLayout.LayoutParams(-1,dp(130)).apply{gravity=Gravity.TOP;topMargin=dp(60)})
        countText=TextView(this).apply{textSize=82f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER}
        root.addView(countText,FrameLayout.LayoutParams(-1,dp(120)).apply{topMargin=dp(190)})
        progressText=TextView(this).apply{textSize=15f;setTextColor(Color.rgb(238,210,130));setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER}
        root.addView(progressText,FrameLayout.LayoutParams(-1,dp(42)).apply{topMargin=dp(305)})

        val folder=TextView(this).apply{text="📁  জিকির লিস্ট";textSize=17f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER;background=box(Color.rgb(145,55,25),22);setOnClickListener{startActivity(Intent(this@TasbihActivity,ZikirManagerActivity::class.java))}}
        root.addView(folder,FrameLayout.LayoutParams(-1,dp(62)).apply{gravity=Gravity.BOTTOM;leftMargin=dp(10);rightMargin=dp(10);bottomMargin=dp(10)})
        setContentView(root);refresh()
    }
    private fun increment(){count++;prefs.edit().putInt("count",count).apply();refresh();val t=target();if(count%100==0||(t>0&&count==t))vibrate()}
    private fun refresh(){if(!::countText.isInitialized)return;countText.text=count.toString();nameText.text=selected;val t=target();progressText.text=if(t>0)"Target: $t    |    বাকি: ${(t-count).coerceAtLeast(0)}" else "Unlimited"}
    private fun vibrate(){val v=getSystemService(VIBRATOR_SERVICE) as Vibrator;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(120,VibrationEffect.DEFAULT_AMPLITUDE))else@Suppress("DEPRECATION")v.vibrate(120)}
    private fun resetCount(){AlertDialog.Builder(this).setTitle("রিসেট").setMessage("বর্তমান গণনা শূন্য করবেন?").setNegativeButton("না",null).setPositiveButton("হ্যাঁ"){_,_->count=0;prefs.edit().putInt("count",0).apply();refresh()}.show()}
}