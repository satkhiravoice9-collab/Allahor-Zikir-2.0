package com.sabbirsamol.app

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var statusText: TextView
    private lateinit var countdownText: TextView
    private lateinit var hijriText: TextView
    private lateinit var banglaText: TextView
    private lateinit var englishText: TextView
    private lateinit var locationText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val locationRequestCode = 501
    data class Prayer(val name:String,val icon:String,val start:Int,val end:Int)
    private val prayers=listOf(Prayer("ফজর","🌅",270,345),Prayer("যোহর","☀️",735,990),Prayer("আসর","🌤️",990,1110),Prayer("মাগরিব","🌇",1110,1200),Prayer("এশা","🌙",1200,1470))
    override fun onCreate(s:Bundle?){super.onCreate(s);window.navigationBarColor=Color.rgb(112,47,69);buildHome();updateClock();updateDates();setupLocation();handler.postDelayed(object:Runnable{override fun run(){updateClock();updateDates();handler.postDelayed(this,1000)}},1000)}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int,r:Int=16)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun row(icon:String,label:String,value:String,color:Int,alarm:Boolean=false)=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(7),dp(3),dp(4),dp(3));background=bg(color,9);addView(TextView(this@MainActivity).apply{text=icon;textSize=17f;gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(31),dp(40)));addView(TextView(this@MainActivity).apply{text=label;textSize=13f;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)},LinearLayout.LayoutParams(0,dp(40),1f));addView(TextView(this@MainActivity).apply{text=value;textSize=11f;setTextColor(Color.WHITE);gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(140),dp(40)));if(alarm)addView(Switch(this@MainActivity).apply{scaleX=.65f;scaleY=.65f},LinearLayout.LayoutParams(dp(40),dp(40)))}
    private fun section(title:String,side:Int,inside:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(4),0,0,0);background=bg(side);addView(LinearLayout(this@MainActivity).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),dp(5),dp(8),dp(6));background=bg(inside,13);addView(TextView(this@MainActivity).apply{text=title;textSize=15f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)})})}
    private fun buildHome(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(255,248,250))};ViewCompat.setOnApplyWindowInsetsListener(root){v,i->v.setPadding(0,0,0,i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom);i}
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(7),dp(7),dp(7),dp(7))}
        hijriText=TextView(this).apply{textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(124,61,81));setTypeface(null,android.graphics.Typeface.BOLD)};banglaText=TextView(this).apply{textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(105,76,86))};englishText=TextView(this).apply{textSize=13f;gravity=Gravity.CENTER;setTextColor(Color.rgb(83,48,61));setTypeface(null,android.graphics.Typeface.BOLD)};locationText=TextView(this).apply{text="📍 লোকেশন খোঁজা হচ্ছে...";textSize=11f;gravity=Gravity.CENTER;setTextColor(Color.rgb(124,61,81))};content.addView(hijriText);content.addView(banglaText);content.addView(englishText);content.addView(locationText)
        statusText=TextView(this).apply{textSize=19f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)};countdownText=TextView(this).apply{textSize=22f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);setTypeface(null,android.graphics.Typeface.BOLD)};content.addView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=bg(Color.rgb(166,48,77));setPadding(dp(5),dp(7),dp(5),dp(7));addView(statusText);addView(countdownText)},LinearLayout.LayoutParams(-1,dp(84)).apply{bottomMargin=dp(7)})
        val p=section("🕌 নামাজের সময়সূচী",Color.rgb(112,47,69),Color.rgb(159,71,96));prayers.forEach{p.addView(row(it.icon,it.name,"${fmt(it.start)} — ${fmt(it.end)}",Color.rgb(190,104,128),true))};content.addView(p,LinearLayout.LayoutParams(-1,dp(255)).apply{bottomMargin=dp(7)})
        val n=section("🌙 তাহাজ্জুদ • সেহরি • ইফতার",Color.rgb(92,55,100),Color.rgb(133,76,139));listOf(Triple("🌙","তাহাজ্জুদ","০১:০০ — ০৪:২০ AM"),Triple("🌄","সেহরি শেষ","০৪:৩০ AM"),Triple("🌇","ইফতার","০৬:৩০ PM")).forEach{n.addView(row(it.first,it.second,it.third,Color.rgb(181,104,144)))};content.addView(n,LinearLayout.LayoutParams(-1,dp(171)).apply{bottomMargin=dp(7)})
        val i=section("☀️ ইশরাক • চাশত",Color.rgb(133,82,49),Color.rgb(185,119,73));listOf(Triple("🌤️","ইশরাক","সূর্যোদয়ের পর"),Triple("☀️","চাশত","সূর্যোদয়ের পর থেকে যোহরের আগে")).forEach{i.addView(row(it.first,it.second,it.third,Color.rgb(205,132,118)))};content.addView(i,LinearLayout.LayoutParams(-1,dp(130)).apply{bottomMargin=dp(7)})
        val f=section("🚫 নামাজের নিষিদ্ধ সময়",Color.rgb(125,48,61),Color.rgb(173,68,82));listOf(Triple("🌅","সূর্যোদয়","০৫:৪৫ — ০৬:০০ AM"),Triple("☀️","দুপুর","১২:০৫ — ১২:১৫ PM"),Triple("🌇","সূর্যাস্ত","০৬:২০ — ০৬:৩০ PM")).forEach{f.addView(row(it.first,it.second,it.third,Color.rgb(196,91,108)))};content.addView(f,LinearLayout.LayoutParams(-1,dp(171)))
        root.addView(ScrollView(this).apply{addView(content);isFillViewport=true},LinearLayout.LayoutParams(-1,0,1f));val menu=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setBackgroundColor(Color.rgb(112,47,69));setPadding(dp(2),dp(2),dp(2),dp(2))};listOf("🏠\nহোম","📿\nতাসবিহ","📚\nলাইব্রেরি","🤲\nমাসনুন","📝\nনোট","🔄\nরিফ্রেশ","ℹ️\nএবাউট").forEach{label->menu.addView(Button(this).apply{text=label;textSize=9f;isAllCaps=false;minHeight=0;minWidth=0;setPadding(0,0,0,0);setTextColor(Color.WHITE);background=bg(Color.rgb(159,71,96),8);setOnClickListener{if(label.contains("রিফ্রেশ")){updateClock();updateDates();setupLocation()}}},LinearLayout.LayoutParams(0,dp(52),1f).apply{setMargins(dp(1),0,dp(1),0)})};root.addView(menu,LinearLayout.LayoutParams(-1,dp(58)));setContentView(root)
    }
    private fun setupLocation(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),locationRequestCode);return};val lm=getSystemService(LOCATION_SERVICE) as LocationManager;val listener=object:LocationListener{override fun onLocationChanged(location:Location){locationText.text="📍 ${if(location.provider==LocationManager.GPS_PROVIDER)"GPS" else "Network"} লোকেশন • ${String.format(Locale.US,"%.4f, %.4f",location.latitude,location.longitude)}"}};try{listOf(LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER).forEach{provider->if(lm.isProviderEnabled(provider))lm.requestLocationUpdates(provider,30000L,50f,listener,Looper.getMainLooper())};locationText.text="📍 GPS / Network লোকেশন সক্রিয়"}catch(_:Exception){locationText.text="📍 লোকেশন পাওয়া যায়নি"}}
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==locationRequestCode&&grantResults.any{it==PackageManager.PERMISSION_GRANTED})setupLocation()}
    private fun updateDates(){val c=Calendar.getInstance();englishText.text="📅 "+SimpleDateFormat("EEEE, dd MMMM yyyy",Locale.ENGLISH).format(c.time);banglaText.text="🇧🇩 বাংলা তারিখ: "+banglaDate(c);hijriText.text="🌙 হিজরি তারিখ: "+hijriDate(c)}
    private fun banglaDate(c:Calendar)="বাংলা তারিখ";
    private fun hijriDate(c:Calendar):String=try{val d=java.time.LocalDate.of(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));val h=java.time.chrono.HijrahDate.from(d);val names=arrayOf("মুহাররম","সফর","রবিউল আউয়াল","রবিউস সানি","জমাদিউল আউয়াল","জমাদিউস সানি","রজব","শাবান","রমজান","শাওয়াল","জিলকদ","জিলহজ");"${bn(h.get(java.time.temporal.ChronoField.DAY_OF_MONTH))} ${names[h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)-1]} ${bn(h.get(java.time.temporal.ChronoField.YEAR))} হিজরি"}catch(_:Exception){"হিজরি তারিখ পাওয়া যায়নি"}
    private fun bn(n:Int)=n.toString().map{"০১২৩৪৫৬৭৮৯"["0123456789".indexOf(it)]}.joinToString("")
    private fun updateClock(){val c=Calendar.getInstance();val now=c.get(Calendar.HOUR_OF_DAY)*60+c.get(Calendar.MINUTE);val sec=c.get(Calendar.SECOND);val active=prayers.lastOrNull{now>=it.start&&now<it.end};val next=prayers.firstOrNull{it.start>now}?:prayers.first();var total=(next.start-now)*60-sec;if(total<=0)total+=86400;statusText.text=if(active!=null)"🕐 ${active.name} ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}";countdownText.text="%02d:%02d:%02d বাকি".format(total/3600,(total%3600)/60,total%60)}
    private fun fmt(m:Int):String{val h=(m/60)%24;return "%02d:%02d %s".format(if(h%12==0)12 else h%12,m%60,if(h<12)"AM" else "PM")}
}
