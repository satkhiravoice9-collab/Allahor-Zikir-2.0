package com.sabbirsamol.app

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var locationText: TextView
    private lateinit var prayerText: TextView
    private lateinit var countdownText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("muslim_time", Context.MODE_PRIVATE) }
    private var latitude = 22.7185
    private var longitude = 89.0711
    private var selectedDivision = "খুলনা বিভাগ"
    private var selectedDistrict = "সাতক্ষীরা"
    private var selectedThana = "শ্যামনগর"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        showHomeScreen(); updatePrayerTimes(); startCountdown()
    }

    private fun showHomeScreen() {
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20,25,20,25); setBackgroundColor(Color.rgb(245,250,247)) }
        fun tv(text:String,size:Float)=TextView(this).apply { this.text=text; textSize=size; setTextColor(Color.rgb(20,83,45)); gravity=Gravity.CENTER; setPadding(0,8,0,8) }
        content.addView(tv("🕌 Muslim Time",25f))
        locationText=tv("📍 $selectedThana, $selectedDistrict, $selectedDivision",14f); content.addView(locationText)
        Button(this).apply { text="📍 লোকেশন পরিবর্তন"; setOnClickListener{showLocationDialog()} }.also{content.addView(it)}
        countdownText=tv("পরবর্তী ওয়াক্ত হিসাব হচ্ছে...",18f); content.addView(countdownText)
        prayerText=tv("",17f).apply{setTextColor(Color.DKGRAY);gravity=Gravity.START}; content.addView(prayerText)
        addBottomMenuButton(content,"🏠  হোম"){ Toast.makeText(this,"আপনি হোম স্ক্রিনে আছেন",Toast.LENGTH_SHORT).show() }
        addBottomMenuButton(content,"📿  তাসবিহ"){ showTasbih() }
        addBottomMenuButton(content,"📚  ইসলামিক লাইব্রেরি"){ showLibrary() }
        addBottomMenuButton(content,"🤲  মাসনুন আমল"){ showMasnunAmal() }
        addBottomMenuButton(content,"📝  নোটপ্যাড"){ showNotepad() }
        addBottomMenuButton(content,"🔄  রিফ্রেশ"){ updatePrayerTimes(); Toast.makeText(this,"নামাজের সময় আপডেট হয়েছে",Toast.LENGTH_SHORT).show() }
        addBottomMenuButton(content,"ℹ️  এবাউট"){ showAboutDialog() }
        val scroll=ScrollView(this); scroll.addView(content); setContentView(scroll)
    }

    private fun addBottomMenuButton(parent:LinearLayout,label:String,action:()->Unit){
        parent.addView(Button(this).apply{ text=label; textSize=16f; setTextColor(Color.rgb(20,83,45)); setOnClickListener{action()} },LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,4,0,4)})
    }

    private fun showTasbih(){
        var count=prefs.getInt("tasbih_count",0)
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;padding(20)}
        val number=TextView(this).apply{text=count.toString();textSize=52f;gravity=Gravity.CENTER;setTextColor(Color.rgb(20,83,45))}
        val add=Button(this).apply{text="📿 গুনুন"}
        val reset=Button(this).apply{text="↩️ রিসেট"}
        box.addView(TextView(this).apply{text="তাসবিহ";textSize=22f;gravity=Gravity.CENTER});box.addView(number);box.addView(add);box.addView(reset)
        val d=AlertDialog.Builder(this).setView(box).setNegativeButton("বন্ধ",null).create()
        add.setOnClickListener{count++;number.text=count.toString();prefs.edit().putInt("tasbih_count",count).apply()}
        reset.setOnClickListener{count=0;number.text="0";prefs.edit().putInt("tasbih_count",0).apply()};d.show()
    }

    private fun showLibrary(){
        val books=arrayOf("🕋 কুরআন শরীফ","📚 সহিহ বুখারী","📚 সহিহ মুসলিম","📚 সুনানে আবু দাউদ","📚 জামে তিরমিজি","📚 সুনানে নাসাঈ","📚 সুনানে ইবনে মাজাহ")
        AlertDialog.Builder(this).setTitle("📚 ইসলামিক লাইব্রেরি").setItems(books){_,which->showBookInfo(books[which],which)}.setNegativeButton("বন্ধ",null).show()
    }

    private fun showBookInfo(title:String,index:Int){
        val extra=if(index==0) "\n\n🕋 কুরআন: সূরা ও ৩০ পারার কাঠামো পরবর্তী ধাপে যুক্ত করা যাবে।" else ""
        AlertDialog.Builder(this).setTitle(title).setMessage("এই বইটি লাইব্রেরিতে নির্বাচিত হয়েছে।$extra\n\n⬇️ PDF Download\n💾 Offline Reading\n🔖 Bookmark\n📌 Last Page\n🔍 Search / Zoom\n\nGoogle Drive PDF-এর আসল লিংক এখনো এই রিপোজিটরিতে দেওয়া হয়নি; লিংক ছাড়া ভুয়া ডাউনলোড চালু করা হবে না।")
            .setPositiveButton("ঠিক আছে",null).show()
    }

    private fun showMasnunAmal(){
        val amals=arrayOf("আয়াতুল কুরসি","সকাল-সন্ধ্যার যিকির","ঘুমের আগে আমল","ঘর থেকে বের হওয়ার দোয়া","খাবারের দোয়া","সফরের দোয়া","ইস্তিগফার","দরুদ শরীফ")
        AlertDialog.Builder(this).setTitle("🤲 মাসনুন আমল").setItems(amals){_,i->AlertDialog.Builder(this).setTitle(amals[i]).setMessage(masnunText(amals[i])).setPositiveButton("ঠিক আছে",null).show()}.setNegativeButton("বন্ধ",null).show()
    }

    private fun masnunText(name:String)=when(name){
        "আয়াতুল কুরসি"->"اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ"
        "ইস্তিগফার"->"أَسْتَغْفِرُ اللَّهَ"
        "দরুদ শরীফ"->"اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ"
        else->"মাসনুন আমলের পূর্ণ আরবি পাঠ পরবর্তী কনটেন্ট ডাটায় যুক্ত করা হবে।"
    }

    private fun showNotepad(){
        val edit=EditText(this).apply{setText(prefs.getString("note", ""));hint="আপনার নোট লিখুন...";minLines=8;gravity=Gravity.TOP}
        AlertDialog.Builder(this).setTitle("📝 নোটপ্যাড").setView(edit).setPositiveButton("💾 সেভ"){_,_->prefs.edit().putString("note",edit.text.toString()).apply();Toast.makeText(this,"নোট সেভ হয়েছে",Toast.LENGTH_SHORT).show()}.setNegativeButton("বন্ধ",null).show()
    }

    private fun showAboutDialog(){AlertDialog.Builder(this).setTitle("ℹ️ অ্যাপ সম্পর্কে").setMessage("উদ্যোক্তা ও পরিচালক\nসাব্বির আহমাদ\n\n📞 ০১৭২৫-২২৮৬২২\n\nবিভিন্ন মাসআলা-মাসায়েল জানতে\nFacebook Page: Satkhira Voice").setPositiveButton("ঠিক আছে",null).show()}

    private fun showLocationDialog(){AlertDialog.Builder(this).setTitle("লোকেশন নির্বাচন করুন").setItems(arrayOf("🛰️ GPS Auto Location","🇧🇩 বাংলাদেশ ম্যানুয়াল নির্বাচন")){_,w->if(w==0)enableGps()else showDivisionDialog()}.setNegativeButton("বাতিল",null).show()}
    private fun showDivisionDialog(){val a=arrayOf("খুলনা বিভাগ","ঢাকা বিভাগ","চট্টগ্রাম বিভাগ","রাজশাহী বিভাগ","সিলেট বিভাগ","বরিশাল বিভাগ","রংপুর বিভাগ","ময়মনসিংহ বিভাগ");AlertDialog.Builder(this).setTitle("বিভাগ নির্বাচন করুন").setItems(a){_,i->selectedDivision=a[i];showDistrictDialog()}.show()}
    private fun showDistrictDialog(){val a=when(selectedDivision){"খুলনা বিভাগ"->arrayOf("সাতক্ষীরা","খুলনা","যশোর","বাগেরহাট","ঝিনাইদহ","নড়াইল","কুষ্টিয়া","চুয়াডাঙ্গা","মেহেরপুর","মাগুরা");"ঢাকা বিভাগ"->arrayOf("ঢাকা","গাজীপুর","নারায়ণগঞ্জ","নরসিংদী","মুন্সীগঞ্জ","মানিকগঞ্জ","মাদারীপুর","ফরিদপুর","গোপালগঞ্জ","রাজবাড়ী","শরীয়তপুর");else->arrayOf("সদর")};AlertDialog.Builder(this).setTitle("$selectedDivision - জেলা নির্বাচন করুন").setItems(a){_,i->selectedDistrict=a[i];showThanaDialog()}.setNegativeButton("পেছনে"){_,_->showDivisionDialog()}.show()}
    private fun showThanaDialog(){val a=when(selectedDistrict){"সাতক্ষীরা"->arrayOf("শ্যামনগর","সাতক্ষীরা সদর","কালিগঞ্জ","আশাশুনি","দেবহাটা","তালা","কলারোয়া");"খুলনা"->arrayOf("খুলনা সদর","ডুমুরিয়া","রূপসা","দাকোপ","পাইকগাছা","বটিয়াঘাটা","ফুলতলা","তেরখাদা","কয়রা");"যশোর"->arrayOf("যশোর সদর","ঝিকরগাছা","শার্শা","অভয়নগর","মনিরামপুর","কেশবপুর");"ঢাকা"->arrayOf("ধানমন্ডি","উত্তরা","মিরপুর","গুলশান","রমনা","মতিঝিল","মোহাম্মদপুর","সাভার","কেরানীগঞ্জ");else->arrayOf("সদর")};AlertDialog.Builder(this).setTitle("$selectedDistrict - উপজেলা নির্বাচন করুন").setItems(a){_,i->selectedThana=a[i];locationText.text="📍 $selectedThana, $selectedDistrict, $selectedDivision";updatePrayerTimes();Toast.makeText(this,"লোকেশন সেট হয়েছে",Toast.LENGTH_SHORT).show()}.setNegativeButton("পেছনে"){_,_->showDistrictDialog()}.show()}

    private fun enableGps(){if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),100);return};try{locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000L,10f,this);Toast.makeText(this,"GPS চালু হয়েছে",Toast.LENGTH_SHORT).show()}catch(_:Exception){Toast.makeText(this,"GPS চালু করা যায়নি",Toast.LENGTH_SHORT).show()}}
    override fun onLocationChanged(l:Location){latitude=l.latitude;longitude=l.longitude;selectedThana="GPS Location";locationText.text="📍 GPS: %.4f, %.4f".format(Locale.US,latitude,longitude);updatePrayerTimes()}

    private fun updatePrayerTimes(){val t=calculatePrayerTimes(latitude,longitude);prayerText.text="🕌 আজকের নামাজের সময়\n\n🌙 ফজর        ${t.fajr}\n🌅 সূর্যোদয়    ${t.sunrise}\n☀️ যোহর       ${t.dhuhr}\n🌤️ আসর        ${t.asr}\n🌇 মাগরিব     ${t.maghrib}\n🌙 এশা        ${t.isha}\n\n🚫 নিষিদ্ধ সময়\n🌅 সূর্যোদয়: ${t.sunriseForbidden}\n☀️ মধ্যাহ্ন: ${t.zenithForbidden}\n🌇 সূর্যাস্ত: ${t.sunsetForbidden}"}
    private fun startCountdown(){handler.post(object:Runnable{override fun run(){updateCountdown();handler.postDelayed(this,1000)}})}
    private fun updateCountdown(){val t=calculatePrayerTimes(latitude,longitude);val now=Calendar.getInstance();val list=listOf("ফজর" to t.fajr,"যোহর" to t.dhuhr,"আসর" to t.asr,"মাগরিব" to t.maghrib,"এশা" to t.isha);var n="ফজর";var next=timeToMillis(t.fajr)+86400000;for((name,time)in list){val m=timeToMillis(time);if(m>now.timeInMillis){n=name;next=m;break}};val d=next-now.timeInMillis;countdownText.text="⏳ পরবর্তী ওয়াক্ত: $n\n%02d:%02d:%02d".format(d/3600000,(d/60000)%60,(d/1000)%60)}
    private fun timeToMillis(s:String):Long{val p=s.split(":");return Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,p[0].toInt());set(Calendar.MINUTE,p[1].toInt());set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}.timeInMillis}
    data class PrayerTimes(val fajr:String,val sunrise:String,val dhuhr:String,val asr:String,val maghrib:String,val isha:String,val sunriseForbidden:String,val zenithForbidden:String,val sunsetForbidden:String)
    private fun calculatePrayerTimes(lat:Double,lon:Double):PrayerTimes{val day=Calendar.getInstance().get(Calendar.DAY_OF_YEAR);val dec=23.45*sin(Math.toRadians(360.0/365.0*(284+day)));val b=Math.toRadians(360.0/365.0*(day-81));val eq=9.87*sin(2*b)-7.53*cos(b)-1.5*sin(b);val noon=12.0+6.0-lon/15.0-eq/60.0;val rise=solarTime(noon,lat,dec,-.833);val set=solarTime(noon,lat,dec,-.833,true);val f=solarTime(noon,lat,dec,-18.0);val i=solarTime(noon,lat,dec,-18.0,true);val dhuhr=noon+.05;val asr=solarAsr(noon,lat,dec);val fs=formatTime(f);val rs=formatTime(rise);val ds=formatTime(dhuhr);val ass=formatTime(asr);val ss=formatTime(set);val isv=formatTime(i);return PrayerTimes(fs,rs,ds,ass,ss,isv,"$rs - ${formatTime(rise+.20)}","${formatTime(dhuhr-.05)} - ${formatTime(dhuhr+.05)}","${formatTime(set-.20)} - $ss")}
    private fun solarTime(noon:Double,lat:Double,dec:Double,angle:Double,evening:Boolean=false):Double{val lr=Math.toRadians(lat);val dr=Math.toRadians(dec);val h=Math.toDegrees(acos(((sin(Math.toRadians(angle))-sin(lr)*sin(dr))/(cos(lr)*cos(dr))).coerceIn(-1.0,1.0)))/15.0;return if(evening)noon+h else noon-h}
    private fun solarAsr(noon:Double,lat:Double,dec:Double):Double{val lr=Math.toRadians(lat);val dr=Math.toRadians(dec);val angle=Math.toDegrees(atan(1.0/(2.0+tan(abs(lr-dr)))));return solarTime(noon,lat,dec,angle,true)}
    private fun formatTime(x:Double):String{var h=floor(x).toInt();var m=((x-h)*60).roundToInt();if(m>=60){h++;m-=60};h=((h%24)+24)%24;return "%02d:%02d".format(h,m)}
    override fun onProviderEnabled(p:String){};override fun onProviderDisabled(p:String){};override fun onStatusChanged(p:String?,s:Int,e:Bundle?){}
    override fun onDestroy(){handler.removeCallbacksAndMessages(null);try{locationManager.removeUpdates(this)}catch(_:Exception){};super.onDestroy()}
}
