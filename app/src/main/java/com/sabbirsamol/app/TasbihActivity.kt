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

class TasbihActivity : ComponentActivity() {
    private var count = 0
    private var selected = "সুবহানাল্লাহ"
    private lateinit var countText: TextView
    private lateinit var nameText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }
    private val listPrefs by lazy { getSharedPreferences("tasbih_list", Context.MODE_PRIVATE) }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun items():MutableList<String>{
        val d=linkedSetOf("সুবহানাল্লাহ","আলহামদুলিল্লাহ","আল্লাহু আকবার","আস্তাগফিরুল্লাহ","লা ইলাহা ইল্লাল্লাহ")
        return listPrefs.getStringSet("items",d)?.toMutableList()?:d.toMutableList()
    }
    private fun targetOf(s:String)=listPrefs.getInt("target_$s",0)

    override fun onCreate(b:Bundle?){super.onCreate(b);count=prefs.getInt("count",0);selected=prefs.getString("selected","সুবহানাল্লাহ")?:"সুবহানাল্লাহ";build()}
    override fun onResume(){super.onResume();if(::nameText.isInitialized){selected=prefs.getString("selected",selected)?:selected;count=prefs.getInt("count",count);refresh()}}

    private fun build(){
        val root=FrameLayout(this).apply{
            setBackgroundColor(Color.rgb(7,45,31))
            setOnTouchListener{_,e->if(e.action==MotionEvent.ACTION_UP)increment();true}
        }
        val kaaba=TextView(this).apply{text="🕋";textSize=74f;gravity=Gravity.CENTER}
        root.addView(kaaba,FrameLayout.LayoutParams(-1,dp(115)).apply{topMargin=dp(5)})
        nameText=TextView(this).apply{text=selected;textSize=22f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER}
        root.addView(nameText,FrameLayout.LayoutParams(dp(230),dp(50)).apply{leftMargin=dp(8);topMargin=dp(122)})
        val reset=Button(this).apply{text="↻";textSize=24f;contentDescription="রিসেট";setOnClickListener{resetCount()}}
        root.addView(reset,FrameLayout.LayoutParams(dp(60),dp(48)).apply{gravity=Gravity.TOP or Gravity.END;rightMargin=dp(8);topMargin=dp(120)})
        countText=TextView(this).apply{textSize=92f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER}
        root.addView(countText,FrameLayout.LayoutParams(-1,dp(145)).apply{topMargin=dp(175)})
        val folder=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;background=bg(Color.rgb(15,104,69),24);setPadding(dp(12),0,dp(12),0);isClickable=true}
        folder.addView(TextView(this).apply{text="📁  জিকির লিস্ট";textSize=17f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER},LinearLayout.LayoutParams(0,-1,1f))
        folder.setOnClickListener{startActivity(Intent(this,ZikirManagerActivity::class.java))}
        root.addView(folder,FrameLayout.LayoutParams(-1,dp(62)).apply{gravity=Gravity.BOTTOM;leftMargin=dp(10);rightMargin=dp(10);bottomMargin=dp(10)})
        setContentView(root);refresh()
    }
    private fun increment(){count++;prefs.edit().putInt("count",count).putString("selected",selected).apply();refresh();val t=targetOf(selected);if(count%100==0||(t>0&&count==t))vibrate()}
    private fun refresh(){countText.text=count.toString();nameText.text=selected}
    private fun vibrate(){val v=getSystemService(VIBRATOR_SERVICE) as Vibrator;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(120,VibrationEffect.DEFAULT_AMPLITUDE))else@Suppress("DEPRECATION")v.vibrate(120)}
    private fun resetCount(){AlertDialog.Builder(this).setTitle("রিসেট").setMessage("বর্তমান গণনা শূন্য করবেন?").setNegativeButton("না",null).setPositiveButton("হ্যাঁ"){_,_->count=0;prefs.edit().putInt("count",0).apply();refresh()}.show()}
}
