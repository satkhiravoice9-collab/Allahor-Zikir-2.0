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
import android.widget.HorizontalScrollView
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
        showHomeScreen()
        updatePrayerTimes()
        startCountdown()
    }

    private fun showHomeScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 250, 247))
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 25, 20, 15)
        }

        fun tv(text: String, size: Float) = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.rgb(20, 83, 45))
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }

        content.addView(tv("🕌 Muslim Time", 25f))
        locationText = tv("📍 $selectedThana, $selectedDistrict, $selectedDivision", 14f)
        content.addView(locationText)
        Button(this).apply {
            text = "📍 লোকেশন পরিবর্তন"
            setOnClickListener { showLocationDialog() }
        }.also { content.addView(it) }
        countdownText = tv("পরবর্তী ওয়াক্ত হিসাব হচ্ছে...", 18f)
        content.addView(countdownText)
        prayerText = tv("", 17f).apply {
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
        }
        content.addView(prayerText)

        val scroll = ScrollView(this).apply { addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // Fixed bottom navigation: seven items are kept in one horizontal row.
        val horizontalScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            isFillViewport = false
        }
        val bottomMenu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6, 5, 6, 7)
            setBackgroundColor(Color.WHITE)
        }
        addBottomMenuButton(bottomMenu, "🏠\nহোম") {
            Toast.makeText(this, "আপনি হোম স্ক্রিনে আছেন", Toast.LENGTH_SHORT).show()
        }
        addBottomMenuButton(bottomMenu, "📿\nতাসবিহ") { showTasbih() }
        addBottomMenuButton(bottomMenu, "📚\nলাইব্রেরি") { showLibrary() }
        addBottomMenuButton(bottomMenu, "🤲\nমাসনুন আমল") { showMasnunAmal() }
        addBottomMenuButton(bottomMenu, "📝\nনোটপ্যাড") { showNotepad() }
        addBottomMenuButton(bottomMenu, "🔄\nরিফ্রেশ") {
            updatePrayerTimes()
            Toast.makeText(this, "নামাজের সময় আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
        }
        addBottomMenuButton(bottomMenu, "ℹ️\nএবাউট") { showAboutDialog() }
        horizontalScroll.addView(bottomMenu)
        root.addView(horizontalScroll, LinearLayout.LayoutParams(-1, 64))
        setContentView(root)
    }

    private fun addBottomMenuButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(
            Button(this).apply {
                text = label
                textSize = 12f
                setTextColor(Color.rgb(20, 83, 45))
                setOnClickListener { action() }
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(5, 0, 5, 0)
                gravity = Gravity.CENTER
            },
            LinearLayout.LayoutParams(112, 54).apply {
                setMargins(2, 0, 2, 0)
            }
        )
    }

    private fun showTasbih() {
        var count = prefs.getInt("tasbih_count", 0)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(20,20,20,20) }
        val number = TextView(this).apply { text = count.toString(); textSize = 52f; gravity = Gravity.CENTER; setTextColor(Color.rgb(20,83,45)) }
        val add = Button(this).apply { text = "📿 গুনুন" }
        val reset = Button(this).apply { text = "↩️ রিসেট" }
        box.addView(TextView(this).apply { text="তাসবিহ"; textSize=22f; gravity=Gravity.CENTER }); box.addView(number); box.addView(add); box.addView(reset)
        val d=AlertDialog.Builder(this).setView(box).setNegativeButton("বন্ধ",null).create()
        add.setOnClickListener { count++; number.text=count.toString(); prefs.edit().putInt("tasbih_count",count).apply() }
        reset.setOnClickListener { count=0; number.text="0"; prefs.edit().putInt("tasbih_count",0).apply() }; d.show()
    }

    private fun showLibrary() {
        val books=arrayOf("🕋 কুরআন শরীফ","📚 সহিহ বুখারী","📚 সহিহ মুসলিম","📚 সুনানে আবু দাউদ","📚 জামে তিরমিজি","📚 সুনানে নাসাঈ","📚 সুনানে ইবনে মাজাহ")
        AlertDialog.Builder(this).setTitle("📚 ইসলামিক লাইব্রেরি").setItems(books){_,which->showBookInfo(books[which],which)}.setNegativeButton("বন্ধ",null).show()
    }
    private fun showBookInfo(title:String,index:Int){ val extra=if(index==0) "\n\n🕋 কুরআন: সূরা ও ৩০ পারার কাঠামো পরবর্তী ধাপে যুক্ত করা যাবে." else ""; AlertDialog.Builder(this).setTitle(title).setMessage("এই বইটি লাইব্রেরিতে নির্বাচিত হয়েছে।$extra\n\n⬇️ PDF Download\n💾 Offline Reading\n🔖 Bookmark\n📌 Last Page\n🔍 Search / Zoom\n\nGoogle Drive PDF-এর আসল লিংক এখনো এই রিপোজিটরিতে দেওয়া হয়নি; লিংক ছাড়া ভুয়া ডাউনলোড চালু করা হবে না।").setPositiveButton("ঠিক আছে",null).show() }
    private fun showMasnunAmal(){ val amals=arrayOf("আয়াতুল কুরসি","সকাল-সন্ধ্যার যিকির","ঘুমের আগে আমল","ঘর থেকে বের হওয়ার দোয়া","খাবারের দোয়া","সফরের দোয়া","ইস্তিগফার","দরুদ শরীফ"); AlertDialog.Builder(this).setTitle("🤲 মাসনুন আমল").setItems(amals){_,i->AlertDialog.Builder(this).setTitle(amals[i]).setMessage(masnunText(amals[i])).setPositiveButton("ঠিক আছে",null).show()}.setNegativeButton("বন্ধ",null).show() }
    private fun masnunText(name:String)=when(name){"আয়াতুল কুরসি"->"اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ";"ইস্তিগফার"->"أَسْتَغْفِرُ اللَّهَ";"দরুদ শরীফ"->"اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ";else->"মাসনুন আমলের পূর্ণ আরবি পাঠ পরবর্তী কনটেন্ট ডাটায় যুক্ত করা হবে।"}
    private fun showNotepad(){ val edit=EditText(this).apply{setText(prefs.getString("note", ""));hint="আপনার নোট লিখুন...";minLines=8;gravity=Gravity.TOP}; AlertDialog.Builder(this).setTitle("📝 নোটপ্যাড").setView(edit).setPositiveButton("💾 সেভ"){_,_->prefs.edit().putString("note",edit.text.toString()).apply();Toast.makeText(this,"নোট সেভ হয়েছে",Toast.LENGTH_SHORT).show()}.setNegativeButton("বন্ধ",null).show() }
    private fun showAboutDialog(){ AlertDialog.Builder(this).setTitle("ℹ️ অ্যাপ সম্পর্কে").setMessage("উদ্যোক্তা ও পরিচালক\nসাব্বির আহমাদ\n\n📞 ০১৭২৫-২২৮৬২২\n\nবিভিন্ন মাসআলা-মাসায়েল জানতে\nFacebook Page: Satkhira Voice").setPositiveButton("ঠিক আছে",null).show() }

    private fun showLocationDialog(){ AlertDialog.Builder(this).setTitle("লোকেশন নির্বাচন করুন").setItems(arrayOf("🛰️ GPS Auto Location","🇧🇩 বাংলাদেশ ম্যানুয়াল নির্বাচন")){_,w->if(w==0)enableGps()else showDivisionDialog()}.setNegativeButton("বাতিল",null).show() }
    private fun showDivisionDialog(){ val a=arrayOf("খুলনা বিভাগ","ঢাকা বিভাগ","চট্টগ্রাম বিভাগ","রাজশাহী বিভাগ","সিলেট বিভাগ","বরিশাল বিভাগ","রংপুর বিভাগ","ময়মনসিংহ বিভাগ"); AlertDialog.Builder(this).setTitle("বিভাগ নির্বাচন করুন").setItems(a){_,i->selectedDivision=a[i];showDistrictDialog()}.show() }
    private fun showDistrictDialog(){ val a=when(selectedDivision){"খুলনা বিভাগ"->arrayOf("সাতক্ষীরা","খুলনা","যশোর","বাগেরহাট","ঝিনাইদহ","নড়াইল","কুষ্টিয়া","চুয়াডাঙ্গা","মেহেরপুর","মাগুরা");"ঢাকা বিভাগ"->arrayOf("ঢাকা","গাজীপুর","নারায়ণগঞ্জ","নরসিংদী","মুন্সীগঞ্জ","মানিকগঞ্জ","মাদারীপুর","ফরিদপুর","গোপালগঞ্জ","রাজবাড়ী","শরীয়তপুর");else->arrayOf("সদর")}; AlertDialog.Builder(this).setTitle("$selectedDivision - জেলা নির্বাচন করুন").setItems(a){_,i->selectedDistrict=a[i];showThanaDialog()}.setNegativeButton("পেছনে"){_,_->showDivisionDialog()}.show() }
    private fun showThanaDialog(){ val a=when(selectedDistrict){"সাতক্ষীরা"->arrayOf("শ্যামনগর","সাতক্ষীরা সদর","কালিগঞ্জ","আশাশুনি","কলারোয়া","তালা","দেবহাটা","পাটকেলঘাটা");else->arrayOf("সদর")}; AlertDialog.Builder(this).setTitle("$selectedDistrict - উপজেলা নির্বাচন করুন").setItems(a){_,i->selectedThana=a[i];locationText.text="📍 $selectedThana, $selectedDistrict, $selectedDivision";updatePrayerTimes()}.setNegativeButton("পেছনে"){_,_->showDistrictDialog()}.show() }

    private fun enableGps(){ if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),100);return}; locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,10f,this);Toast.makeText(this,"GPS লোকেশন চালু হয়েছে",Toast.LENGTH_SHORT).show() }
    override fun onLocationChanged(location:Location){latitude=location.latitude;longitude=location.longitude;locationText.text="📍 GPS: %.4f, %.4f".format(Locale.US,latitude,longitude);updatePrayerTimes()}
    override fun onProviderEnabled(provider:String){}
    override fun onProviderDisabled(provider:String){}
    override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?){}

    private fun updatePrayerTimes(){ val c=Calendar.getInstance(); val h=c.get(Calendar.HOUR_OF_DAY); val m=c.get(Calendar.MINUTE); prayerText.text="ফজর: ০৪:৩০ AM\nযোহর: ১২:১৫ PM\nআসর: ০৪:৩০ PM\nমাগরিব: ০৬:৩০ PM\nএশা: ০৮:০০ PM"; countdownText.text="🕐 বর্তমান সময়: %02d:%02d".format(h,m) }
    private fun startCountdown(){ handler.postDelayed(object:Runnable{override fun run(){updatePrayerTimes();handler.postDelayed(this,60000L)}},60000L) }
}
