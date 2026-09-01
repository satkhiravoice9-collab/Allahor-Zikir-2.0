package com.sabbirsamol.app

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var statusText: TextView
    private lateinit var countdownText: TextView
    private val handler = Handler(Looper.getMainLooper())
    data class Prayer(val name:String,val start:Int,val end:Int)
    private val prayers=listOf(Prayer("ফজর",270,345),Prayer("যোহর",735,990),Prayer("আসর",990,1110),Prayer("মাগরিব",1110,1200),Prayer("এশা",1200,1470))
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.navigationBarColor=Color.WHITE;buildHome();updateClock();handler.postDelayed(object:Runnable{override fun run(){updateClock();handler.postDelayed(this,1000)}},1000)}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(16).toFloat()}
    private fun row(t:String)=TextView(this).apply{text=t;textSize=14f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setPadding(0,dp(2),0,dp(2))}
    private fun card(title:String,color:Int):LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(12),dp(6),dp(12),dp(6));background=bg(color);addView(TextView(this@MainActivity).apply{text=title;textSize=16f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)})}
    private fun buildHome(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(245,249,247))}
        ViewCompat.setOnApplyWindowInsetsListener(root){v,i->v.setPadding(0,0,0,i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom);i}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),dp(5),dp(8),dp(4))}
        content.addView(TextView(this).apply{text="🕌 Muslim Time";textSize=22f;gravity=Gravity.CENTER;setTextColor(Color.rgb(16,94,55));setTypeface(null,android.graphics.Typeface.BOLD)})
        content.addView(TextView(this).apply{text="📍 শ্যামনগর, সাতক্ষীরা, খুলনা বিভাগ";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.DKGRAY)})
        statusText=TextView(this).apply{textSize=17f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)}
        countdownText=TextView(this).apply{textSize=14f;gravity=Gravity.CENTER;setTextColor(Color.WHITE)}
        content.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(8),dp(5),dp(8),dp(5));background=bg(Color.rgb(28,112,76));addView(statusText);addView(countdownText)},LinearLayout.LayoutParams(-1,dp(58)).apply{bottomMargin=dp(4)})
        val p=card("🕌 নামাজের সময়সূচী",Color.rgb(24,112,78));prayers.forEach{p.addView(row("${it.name}    ${fmt(it.start)} — ${fmt(it.end)}"))};content.addView(p,LinearLayout.LayoutParams(-1,dp(157)).apply{bottomMargin=dp(4)})
        val f=card("🚫 নামাজের নিষিদ্ধ সময়",Color.rgb(176,77,60));f.addView(row("সূর্যোদয়    ০৫:৪৫ — ০৬:০০ AM"));f.addView(row("দুপুর        ১২:০৫ — ১২:১৫ PM"));f.addView(row("সূর্যাস্ত    ০৬:২০ — ০৬:৩০ PM"));content.addView(f,LinearLayout.LayoutParams(-1,dp(103)).apply{bottomMargin=dp(4)})
        val n=card("🌙 তাহাজ্জুদ • সেহরি • ইফতার",Color.rgb(82,76,143));n.addView(row("তাহাজ্জুদ    ০১:০০ — ০৪:২০ AM"));n.addView(row("সেহরি শেষ    ০৪:৩০ AM"));n.addView(row("ইফতার        ০৬:৩০ PM"));content.addView(n,LinearLayout.LayoutParams(-1,dp(103)))
        root.addView(ScrollView(this).apply{addView(content);isFillViewport=true},LinearLayout.LayoutParams(-1,0,1f))
        val menu=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setBackgroundColor(Color.WHITE);setPadding(1,2,1,2)}
        listOf("🏠\nহোম","📿\nতাসবিহ","📚\nলাইব্রেরি","🤲\nমাসনুন","📝\nনোট","🔄\nরিফ্রেশ","ℹ️\nএবাউট").forEach{label->menu.addView(Button(this).apply{text=label;textSize=9f;isAllCaps=false;minHeight=0;minWidth=0;setPadding(0,0,0,0);gravity=Gravity.CENTER;setOnClickListener{Toast.makeText(this@MainActivity,label.replace("\n"," "),Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(0,dp(52),1f))}
        root.addView(menu,LinearLayout.LayoutParams(-1,dp(58)));setContentView(root)
    }
    private fun updateClock(){val c=Calendar.getInstance();val now=c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);val sec=c.get(Calendar.SECOND);val active=prayers.lastOrNull{now>=it.start&&now<it.end};val next=prayers.firstOrNull{it.start>now}?:prayers.first();var total=(next.start-now)*60-sec;if(total<=0)total+=86400;statusText.text=if(active!=null)"🕐 ${active.name} ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}";countdownText.text="পরবর্তী ওয়াক্ত শুরু হতে বাকি %02d:%02d:%02d".format(total/3600,(total%3600)/60,total%60)}
    private fun fmt(m:Int):String{val h=(m/60)%24;return "%02d:%02d %s".format(if(h%12==0)12 else h%12,m%60,if(h<12)"AM" else "PM")}
}
