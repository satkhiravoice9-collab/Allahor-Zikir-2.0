package com.sabbirsamol.app

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var statusText: TextView
    private lateinit var countdownText: TextView
    private val handler = Handler(Looper.getMainLooper())
    data class Prayer(val name:String,val icon:String,val start:Int,val end:Int)
    private val prayers=listOf(Prayer("ফজর","🌅",270,345),Prayer("যোহর","☀️",735,990),Prayer("আসর","🌤️",990,1110),Prayer("মাগরিব","🌇",1110,1200),Prayer("এশা","🌙",1200,1470))
    override fun onCreate(s:Bundle?){super.onCreate(s);window.navigationBarColor=Color.WHITE;buildHome();updateClock();handler.postDelayed(object:Runnable{override fun run(){updateClock();handler.postDelayed(this,1000)}},1000)}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int,r:Int=16)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun row(icon:String,label:String,value:String,color:Int,alarm:Boolean=false)=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(7),dp(2),dp(4),dp(2));background=bg(color,9);addView(TextView(this@MainActivity).apply{text=icon;textSize=17f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(31),dp(38)));addView(TextView(this@MainActivity).apply{text=label;textSize=13f;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(38),1f));addView(TextView(this@MainActivity).apply{text=value;textSize=11f;setTextColor(Color.WHITE);gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(140),dp(38)));if(alarm)addView(Switch(this@MainActivity).apply{scaleX=.65f;scaleY=.65f},LinearLayout.LayoutParams(dp(40),dp(38)))}
    private fun section(title:String,side:Int,inside:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(4),0,0,0);background=bg(side);addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),dp(5),dp(8),dp(6));background=bg(inside,13);addView(TextView(this@MainActivity).apply{text=title;textSize=15f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)})})}
    private fun buildHome(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(246,248,247))};ViewCompat.setOnApplyWindowInsetsListener(root){v,i->v.setPadding(0,0,0,i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom);i}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(7),dp(7),dp(7),dp(7))};val now=Calendar.getInstance()
        content.addView(TextView(this).apply{text="📅 "+SimpleDateFormat("EEEE, dd MMMM yyyy",Locale.ENGLISH).format(now.time);textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(30,70,55));setTypeface(null,android.graphics.Typeface.BOLD)})
        content.addView(TextView(this).apply{text="🌙 হিজরি তারিখ • মাগরিবের পর ইসলামী দিন পরিবর্তন";textSize=12f;gravity=Gravity.CENTER;setTextColor(Color.rgb(82,92,88))})
        content.addView(TextView(this).apply{text="🇧🇩 বাংলা তারিখ";textSize=12f;gravity=Gravity.CENTER;setTextColor(Color.rgb(82,92,88))})
        content.addView(TextView(this).apply{text="📍 GPS লোকেশন";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.rgb(82,92,88))})
        statusText=TextView(this).apply{textSize=19f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)};countdownText=TextView(this).apply{textSize=21f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)}
        content.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=bg(Color.rgb(177,54,54));setPadding(dp(5),dp(6),dp(5),dp(6));addView(statusText);addView(countdownText)},LinearLayout.LayoutParams(-1,dp(80)).apply{bottomMargin=dp(6)})
        val p=section("🕌 নামাজের সময়সূচী",Color.rgb(8,75,54),Color.rgb(30,126,86));prayers.forEach{p.addView(row(it.icon,it.name,"${fmt(it.start)} — ${fmt(it.end)}",Color.rgb(39,145,99),true))};content.addView(p,LinearLayout.LayoutParams(-1,dp(245)).apply{bottomMargin=dp(6)})
        val n=section("🌙 তাহাজ্জুদ • সেহরি • ইফতার",Color.rgb(61,51,103),Color.rgb(91,78,151));listOf(Triple("🌙","তাহাজ্জুদ","০১:০০ — ০৪:২০ AM"),Triple("🌄","সেহরি শেষ","০৪:৩০ AM"),Triple("🌇","ইফতার","০৬:৩০ PM")).forEach{n.addView(row(it.first,it.second,it.third,Color.rgb(111,96,172)))};content.addView(n,LinearLayout.LayoutParams(-1,dp(165)).apply{bottomMargin=dp(6)})
        val i=section("☀️ ইশরাক • চাশত",Color.rgb(137,91,25),Color.rgb(198,135,42));listOf(Triple("🌤️","ইশরাক","সূর্যোদয়ের পর"),Triple("☀️","চাশত","সূর্যোদয়ের পর থেকে যোহরের আগে")).forEach{i.addView(row(it.first,it.second,it.third,Color.rgb(210,151,57)))};content.addView(i,LinearLayout.LayoutParams(-1,dp(125)).apply{bottomMargin=dp(6)})
        val f=section("🚫 নামাজের নিষিদ্ধ সময়",Color.rgb(125,48,43),Color.rgb(181,71,61));listOf(Triple("🌅","সূর্যোদয়","০৫:৪৫ — ০৬:০০ AM"),Triple("☀️","দুপুর","১২:০৫ — ১২:১৫ PM"),Triple("🌇","সূর্যাস্ত","০৬:২০ — ০৬:৩০ PM")).forEach{f.addView(row(it.first,it.second,it.third,Color.rgb(194,85,73)))};content.addView(f,LinearLayout.LayoutParams(-1,dp(165)))
        root.addView(ScrollView(this).apply{addView(content);isFillViewport=true},LinearLayout.LayoutParams(-1,0,1f));val menu=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setBackgroundColor(Color.WHITE)};listOf("🏠\nহোম","📿\nতাসবিহ","📚\nলাইব্রেরি","🤲\nমাসনুন","📝\nনোট","🔄\nরিফ্রেশ","ℹ️\nএবাউট").forEach{label->menu.addView(Button(this).apply{text=label;textSize=9f;isAllCaps=false;minHeight=0;minWidth=0;setPadding(0,0,0,0);setOnClickListener{if(label.contains("রিফ্রেশ"))updateClock()}},LinearLayout.LayoutParams(0,dp(52),1f))};root.addView(menu,LinearLayout.LayoutParams(-1,dp(58)));setContentView(root)
    }
    private fun updateClock(){val c=Calendar.getInstance();val now=c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);val sec=c.get(Calendar.SECOND);val active=prayers.lastOrNull{now>=it.start&&now<it.end};val next=prayers.firstOrNull{it.start>now}?:prayers.first();var total=(next.start-now)*60-sec;if(total<=0)total+=86400;statusText.text=if(active!=null)"🕐 ${active.name} ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}";countdownText.text="%02d:%02d:%02d বাকি".format(total/3600,(total%3600)/60,total%60)}
    private fun fmt(m:Int):String{val h=(m/60)%24;return "%02d:%02d %s".format(if(h%12==0)12 else h%12,m%60,if(h<12)"AM" else "PM")}
}
