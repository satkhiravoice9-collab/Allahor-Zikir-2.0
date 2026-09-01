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
    data class Prayer(val name:String,val icon:String,val start:Int,val end:Int)
    private val prayers=listOf(Prayer("ফজর","🌅",270,345),Prayer("যোহর","☀️",735,990),Prayer("আসর","🌤️",990,1110),Prayer("মাগরিব","🌇",1110,1200),Prayer("এশা","🌙",1200,1470))

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.navigationBarColor=Color.WHITE;buildHome();updateClock();handler.postDelayed(object:Runnable{override fun run(){updateClock();handler.postDelayed(this,1000)}},1000)}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int,r:Int=16)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}

    private fun section(title:String,side:Int,inside:Int):LinearLayout=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL;setPadding(dp(4),0,0,0);background=bg(side,16)
        addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(7),dp(10),dp(7));background=bg(inside,14);addView(TextView(this@MainActivity).apply{text=title;textSize=16f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)})})
    }
    private fun infoRow(icon:String,label:String,value:String,color:Int):LinearLayout=LinearLayout(this).apply{
        orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(3),dp(8),dp(3));background=bg(color,10)
        addView(TextView(this@MainActivity).apply{text=icon;textSize=18f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(34),-2))
        addView(TextView(this@MainActivity).apply{text=label;textSize=14f;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)},LinearLayout.LayoutParams(0,-2,1f))
        addView(TextView(this@MainActivity).apply{text=value;textSize=13f;setTextColor(Color.WHITE);gravity=Gravity.CENTER})
    }
    private fun prayerRow(p:Prayer)=LinearLayout(this).apply{
        orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(2),dp(4),dp(2))
        addView(TextView(this@MainActivity).apply{text=p.icon;textSize=19f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(34),dp(36)))
        addView(TextView(this@MainActivity).apply{text=p.name;textSize=14f;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,dp(36),1f))
        addView(TextView(this@MainActivity).apply{text="${fmt(p.start)} — ${fmt(p.end)}";textSize=12f;setTextColor(Color.WHITE);gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(125),dp(36)))
        addView(Switch(this@MainActivity).apply{isChecked=false;scaleX=.75f;scaleY=.75f;contentDescription="${p.name} অ্যালার্ম";setOnCheckedChangeListener{_,on->Toast.makeText(this@MainActivity,if(on)"${p.name} অ্যালার্ম চালু" else "${p.name} অ্যালার্ম বন্ধ",Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(dp(48),dp(36)))
    }

    private fun buildHome(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(246,248,247))}
        ViewCompat.setOnApplyWindowInsetsListener(root){v,i->v.setPadding(0,0,0,i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom);i}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),dp(5),dp(8),dp(4))}
        content.addView(TextView(this).apply{text="🕌 Muslim Time";textSize=22f;gravity=Gravity.CENTER;setTextColor(Color.rgb(20,92,64));setTypeface(null,android.graphics.Typeface.BOLD)})
        content.addView(TextView(this).apply{text="📍 শ্যামনগর, সাতক্ষীরা, খুলনা বিভাগ";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.rgb(82,92,88))})
        statusText=TextView(this).apply{textSize=17f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)}
        countdownText=TextView(this).apply{textSize=14f;gravity=Gravity.CENTER;setTextColor(Color.rgb(232,248,240))}
        content.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(8),dp(5),dp(8),dp(5));background=bg(Color.rgb(25,105,72));addView(statusText);addView(countdownText)},LinearLayout.LayoutParams(-1,dp(58)).apply{bottomMargin=dp(5)})

        val p=section("🕌  নামাজের সময়সূচী",Color.rgb(11,82,58),Color.rgb(33,133,91));prayers.forEach{p.addView(prayerRow(it))};content.addView(p,LinearLayout.LayoutParams(-1,dp(204)).apply{bottomMargin=dp(5)})
        val f=section("🚫  নামাজের নিষিদ্ধ সময়",Color.rgb(132,55,48),Color.rgb(190,82,68));f.addView(infoRow("🌅","সূর্যোদয়","০৫:৪৫ — ০৬:০০ AM",Color.rgb(205,98,81)));f.addView(infoRow("☀️","দুপুর","১২:০৫ — ১২:১৫ PM",Color.rgb(205,98,81)));f.addView(infoRow("🌇","সূর্যাস্ত","০৬:২০ — ০৬:৩০ PM",Color.rgb(205,98,81)));content.addView(f,LinearLayout.LayoutParams(-1,dp(122)).apply{bottomMargin=dp(5)})
        val n=section("🌙  তাহাজ্জুদ • সেহরি • ইফতার",Color.rgb(66,56,112),Color.rgb(101,88,164));n.addView(infoRow("🌙","তাহাজ্জুদ","০১:০০ — ০৪:২০ AM",Color.rgb(113,99,179)));n.addView(infoRow("🌄","সেহরি শেষ","০৪:৩০ AM",Color.rgb(113,99,179)));n.addView(infoRow("🌇","ইফতার","০৬:৩০ PM",Color.rgb(113,99,179)));content.addView(n,LinearLayout.LayoutParams(-1,dp(122)))
        root.addView(ScrollView(this).apply{addView(content);isFillViewport=true},LinearLayout.LayoutParams(-1,0,1f))
        val menu=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setBackgroundColor(Color.WHITE);setPadding(1,2,1,2)}
        listOf("🏠\nহোম","📿\nতাসবিহ","📚\nলাইব্রেরি","🤲\nমাসনুন","📝\nনোট","🔄\nরিফ্রেশ","ℹ️\nএবাউট").forEach{label->menu.addView(Button(this).apply{text=label;textSize=9f;isAllCaps=false;minHeight=0;minWidth=0;setPadding(0,0,0,0);gravity=Gravity.CENTER;setOnClickListener{Toast.makeText(this@MainActivity,label.replace("\n"," "),Toast.LENGTH_SHORT).show()}},LinearLayout.LayoutParams(0,dp(52),1f))}
        root.addView(menu,LinearLayout.LayoutParams(-1,dp(58)));setContentView(root)
    }
    private fun updateClock(){val c=Calendar.getInstance();val now=c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);val sec=c.get(Calendar.SECOND);val active=prayers.lastOrNull{now>=it.start&&now<it.end};val next=prayers.firstOrNull{it.start>now}?:prayers.first();var total=(next.start-now)*60-sec;if(total<=0)total+=86400;statusText.text=if(active!=null)"🕐 ${active.name} ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}";countdownText.text="পরবর্তী ওয়াক্ত শুরু হতে বাকি %02d:%02d:%02d".format(total/3600,(total%3600)/60,total%60)}
    private fun fmt(m:Int):String{val h=(m/60)%24;return "%02d:%02d %s".format(if(h%12==0)12 else h%12,m%60,if(h<12)"AM" else "PM")}
}
