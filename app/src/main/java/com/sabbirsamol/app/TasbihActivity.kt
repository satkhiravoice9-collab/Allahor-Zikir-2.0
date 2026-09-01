package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Vibrator
import android.os.Build
import android.os.VibrationEffect
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import androidx.activity.ComponentActivity

/** Tasbih-only screen. Home screen remains untouched. */
class TasbihActivity : ComponentActivity() {
    private var count = 0
    private lateinit var countText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun panel(c:Int,r:Int=22)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);count=prefs.getInt("count",0);buildTasbih()}

    private fun buildTasbih(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(10),dp(10),dp(10));setBackgroundColor(Color.rgb(7,45,31))}
        val kaaba=FrameLayout(this).apply{background=panel(Color.rgb(11,72,48),26);addView(TextView(this@TasbihActivity).apply{text="🕋";textSize=78f;gravity=Gravity.CENTER});addView(TextView(this@TasbihActivity).apply{text="الْكَعْبَةُ الْمُشَرَّفَةُ";textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(238,210,130));setTypeface(null,Typeface.BOLD);layoutParams=FrameLayout.LayoutParams(-1,dp(30),Gravity.BOTTOM).apply{bottomMargin=dp(5)}})}
        root.addView(kaaba,LinearLayout.LayoutParams(-1,dp(135)).apply{bottomMargin=dp(8)})
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        top.addView(TextView(this).apply{text="📿 তাসবিহ";textSize=21f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,dp(45),1f))
        top.addView(Button(this).apply{text="⚙️";textSize=18f;setOnClickListener{openCustomize()}},LinearLayout.LayoutParams(dp(55),dp(45)))
        root.addView(top)
        countText=TextView(this).apply{textSize=76f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD)}
        root.addView(countText,LinearLayout.LayoutParams(-1,dp(120)))
        val tap=FrameLayout(this).apply{background=panel(Color.rgb(15,104,69),30);isClickable=true;setOnTouchListener{_,e->if(e.action==MotionEvent.ACTION_UP){count++;save();if(count%100==0)vibrate();performClick()};true};setOnClickListener{ };addView(TextView(this@TasbihActivity).apply{text="তাসবিহ পড়তে স্ক্রিনে ট্যাপ করুন\n\nপ্রতি ১০০ গণনায় ভাইব্রেশন";textSize=18f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD)})}
        root.addView(tap,LinearLayout.LayoutParams(-1,0,1f).apply{bottomMargin=dp(8)})
        root.addView(TextView(this).apply{text="⚙️ কাস্টমাইজেশন থেকে Target ও Alarm পরিচালনা করুন";textSize=12f;gravity=Gravity.CENTER;setTextColor(Color.rgb(238,210,130))},LinearLayout.LayoutParams(-1,dp(28)))
        setContentView(root);refresh()
    }
    private fun refresh(){countText.text=count.toString()}
    private fun save(){prefs.edit().putInt("count",count).apply();refresh()}
    private fun vibrate(){val v=getSystemService(VIBRATOR_SERVICE) as Vibrator;if(Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(120,VibrationEffect.DEFAULT_AMPLITUDE))else@Suppress("DEPRECATION")v.vibrate(120)}

    private fun openCustomize(){
        val custom= getSharedPreferences("tasbih_customize",Context.MODE_PRIVATE)
        var target=custom.getInt("target",0)
        val alarms=custom.getStringSet("alarms",emptySet())!!.toMutableSet()
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(10),dp(20),dp(5))}
        val targetInput=EditText(this).apply{hint="Target সংখ্যা (0 = Unlimited)";inputType=2;setText(if(target>0)target.toString() else "")}
        box.addView(targetInput)
        val alarmList=TextView(this).apply{textSize=14f;setPadding(0,dp(10),0,dp(10));setTextColor(Color.DKGRAY)}
        fun updateAlarmText(){alarmList.text=if(alarms.isEmpty())"🔔 কোনো Alarm নেই" else alarms.sorted().joinToString("\n"){"🔔 $it"}}
        updateAlarmText();box.addView(alarmList)
        AlertDialog.Builder(this).setTitle("📿 তাসবিহ কাস্টমাইজ").setView(box)
            .setNeutralButton("➕ Alarm") { _,_->
                val input=EditText(this).apply{hint="যেমন: সকাল ৮:০০"}
                AlertDialog.Builder(this).setTitle("Alarm যোগ করুন").setView(input).setNegativeButton("বাতিল",null).setPositiveButton("যোগ") {_,_->val x=input.text.toString().trim();if(x.isNotEmpty())custom.edit().putStringSet("alarms",(alarms+ x).toSet()).apply();openCustomize()}.show()
            }
            .setNegativeButton("🗑 Alarm মুছুন") {_,_->
                if(alarms.isNotEmpty())AlertDialog.Builder(this).setTitle("কোন Alarm মুছবেন?").setItems(alarms.sorted().toTypedArray()){_,which->alarms.remove(alarms.sorted()[which]);custom.edit().putStringSet("alarms",alarms).apply();openCustomize()}.show()
            }
            .setPositiveButton("💾 Save") {_,_->custom.edit().putInt("target",targetInput.text.toString().toIntOrNull()?:0).apply();Toast.makeText(this,"কাস্টমাইজেশন সংরক্ষিত",Toast.LENGTH_SHORT).show()}.show()
    }
}
