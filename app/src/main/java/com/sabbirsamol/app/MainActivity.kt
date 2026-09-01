package com.sabbirsamol.app

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var locationText: TextView
    private lateinit var countdownText: TextView
    private lateinit var statusText: TextView
    private lateinit var prayerCard: LinearLayout
    private lateinit var forbiddenCard: LinearLayout
    private lateinit var nightCard: LinearLayout
    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { getSharedPreferences("muslim_time", Context.MODE_PRIVATE) }
    private var latitude = 22.7185
    private var longitude = 89.0711
    private var selectedDivision = "খুলনা বিভাগ"
    private var selectedDistrict = "সাতক্ষীরা"
    private var selectedThana = "শ্যামনগর"

    data class Prayer(val name: String, val start: Int, val end: Int)
    private val prayers = listOf(
        Prayer("ফজর", 4 * 60 + 30, 5 * 60 + 45),
        Prayer("যোহর", 12 * 60 + 15, 16 * 60 + 30),
        Prayer("আসর", 16 * 60 + 30, 18 * 60 + 30),
        Prayer("মাগরিব", 18 * 60 + 30, 20 * 60),
        Prayer("এশা", 20 * 60, 28 * 60 + 30)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        showHomeScreen()
        updateHome()
        handler.postDelayed(object : Runnable { override fun run() { updateHome(); handler.postDelayed(this, 1000) } }, 1000)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(color: Int, radius: Int = 18): GradientDrawable = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }

    private fun card(title: String, color: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(9), dp(14), dp(9))
        background = bg(color)
        addView(TextView(this@MainActivity).apply { text = title; textSize = 17f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) })
    }

    private fun row(label: String, value: String): TextView = TextView(this).apply {
        text = "$label    $value"
        textSize = 15f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        setPadding(0, dp(3), 0, dp(3))
    }

    private fun showHomeScreen() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = bg(Color.rgb(244, 248, 246), 0) }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets -> view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom); insets }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(7), dp(10), dp(5)) }

        content.addView(TextView(this).apply { text = "🕌 Muslim Time"; textSize = 23f; gravity = Gravity.CENTER; setTextColor(Color.rgb(16, 94, 55)); setTypeface(null, android.graphics.Typeface.BOLD) })
        locationText = TextView(this).apply { text = "📍 $selectedThana, $selectedDistrict, $selectedDivision"; textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.DKGRAY) }
        content.addView(locationText)
        Button(this).apply { text = "📍 লোকেশন পরিবর্তন"; textSize = 12f; minHeight = 0; setPadding(0, 0, 0, 0); setOnClickListener { showLocationDialog() } }.also { content.addView(it, LinearLayout.LayoutParams(-1, dp(34))) }

        val nowBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(8), dp(6), dp(8), dp(6)); background = bg(Color.rgb(28, 112, 76), 18) }
        statusText = TextView(this).apply { textSize = 18f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) }
        countdownText = TextView(this).apply { textSize = 15f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setPadding(0, dp(2), 0, 0) }
        nowBox.addView(statusText); nowBox.addView(countdownText); content.addView(nowBox, LinearLayout.LayoutParams(-1, dp(62)).apply { bottomMargin = dp(5) })

        prayerCard = card("🕌 নামাজের সময়সূচী", Color.rgb(24, 112, 78));
        prayers.forEach { prayerCard.addView(row(prayer.name, "${fmt(prayer.start)} — ${fmt(prayer.end)}")) }
        content.addView(prayerCard, LinearLayout.LayoutParams(-1, dp(174)).apply { bottomMargin = dp(5) })

        forbiddenCard = card("🚫 নামাজের নিষিদ্ধ সময়", Color.rgb(176, 77, 60))
        forbiddenCard.addView(row("সূর্যোদয়", "০৫:৪৫ — ০৬:০০ AM")); forbiddenCard.addView(row("দুপুর", "১২:০৫ — ১২:১৫ PM")); forbiddenCard.addView(row("সূর্যাস্ত", "০৬:২০ — ০৬:৩০ PM"))
        content.addView(forbiddenCard, LinearLayout.LayoutParams(-1, dp(116)).apply { bottomMargin = dp(5) })

        nightCard = card("🌙 তাহাজ্জুদ • সেহরি • ইফতার", Color.rgb(82, 76, 143))
        nightCard.addView(row("তাহাজ্জুদ", "০১:০০ — ০৪:২০ AM")); nightCard.addView(row("সেহরি শেষ", "০৪:৩০ AM")); nightCard.addView(row("ইফতার", "০৬:৩০ PM"))
        content.addView(nightCard, LinearLayout.LayoutParams(-1, dp(116)))

        val scroll = ScrollView(this).apply { addView(content); isFillViewport = true }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val menu = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(1), dp(2), dp(1), dp(2)); setBackgroundColor(Color.WHITE) }
        addBottomMenuButton(menu, "🏠\nহোম") { Toast.makeText(this, "আপনি হোম স্ক্রিনে আছেন", Toast.LENGTH_SHORT).show() }
        addBottomMenuButton(menu, "📿\nতাসবিহ") { showTasbih() }; addBottomMenuButton(menu, "📚\nলাইব্রেরি") { showLibrary() }; addBottomMenuButton(menu, "🤲\nমাসনুন") { showMasnunAmal() }; addBottomMenuButton(menu, "📝\nনোট") { showNotepad() }; addBottomMenuButton(menu, "🔄\nরিফ্রেশ") { updateHome() }; addBottomMenuButton(menu, "ℹ️\nএবাউট") { showAboutDialog() }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(60)))
        setContentView(root)
    }

    private fun addBottomMenuButton(parent: LinearLayout, label: String, action: () -> Unit) {
        parent.addView(Button(this).apply { text = label; textSize = 9f; setTextColor(Color.rgb(20,83,45)); isAllCaps = false; minHeight = 0; minWidth = 0; includeFontPadding = false; setPadding(0,0,0,0); gravity = Gravity.CENTER; maxLines = 2; setOnClickListener { action() } }, LinearLayout.LayoutParams(0, dp(54), 1f).apply { setMargins(1,0,1,0) })
    }

    private fun updateHome() {
        val cal = Calendar.getInstance(); val minute = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE); val second = cal.get(Calendar.SECOND)
        val next = prayers.firstOrNull { it.start > minute } ?: prayers.first()
        var diff = next.start - minute; if (diff <= 0) diff += 24 * 60
        val totalSeconds = diff * 60 - second; val hh = totalSeconds / 3600; val mm = (totalSeconds % 3600) / 60; val ss = totalSeconds % 60
        val active = prayers.lastOrNull { minute >= it.start && minute < it.end }?.name
        statusText.text = if (active != null) "🕐 $active ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}"
        countdownText.text = "পরবর্তী ওয়াক্ত শুরু হতে বাকি  %02d:%02d:%02d".format(hh, mm, ss)
    }

    private fun fmt(minute: Int): String { val h = (minute / 60) % 24; val m = minute % 60; val ap = if (h < 12) "AM" else "PM"; val hh = if (h % 12 == 0) 12 else h % 12; return "%02d:%02d %s".format(hh,m,ap) }

    private fun showTasbih() { var count=prefs.getInt("tasbih_count",0); val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(20,20,20,20)}; val n=TextView(this).apply{text=count.toString();textSize=52f;gravity=Gravity.CENTER}; val add=Button(this).apply{text="📿 গুনুন"}; val reset=Button(this).apply{text="↩️ রিসেট"}; box.addView(TextView(this).apply{text="তাসবিহ";textSize=22f;gravity=Gravity.CENTER});box.addView(n);box.addView(add);box.addView(reset); val d=AlertDialog.Builder(this).setView(box).setNegativeButton("বন্ধ",null).create();add.setOnClickListener{count++;n.text=count.toString();prefs.edit().putInt("tasbih_count",count).apply()};reset.setOnClickListener{count=0;n.text="0";prefs.edit().putInt("tasbih_count",0).apply()};d.show() }
    private fun showLibrary() { val b=arrayOf("🕋 কুরআন শরীফ","📚 সহিহ বুখারী","📚 সহিহ মুসলিম","📚 সুনানে আবু দাউদ","📚 জামে তিরমিজি","📚 সুনানে নাসাঈ","📚 সুনানে ইবনে মাজাহ");AlertDialog.Builder(this).setTitle("📚 ইসলামিক লাইব্রেরি").setItems(b){_,i->showBookInfo(b[i],i)}.setNegativeButton("বন্ধ",null).show() }
    private fun showBookInfo(title:String,index:Int){AlertDialog.Builder(this).setTitle(title).setMessage("এই বইটি লাইব্রেরিতে নির্বাচিত হয়েছে।\n\n⬇️ PDF Download\n💾 Offline Reading\n🔖 Bookmark\n📌 Last Page\n🔍 Search / Zoom\n\nআসল PDF লিংক যুক্ত না হওয়া পর্যন্ত ভুয়া ডাউনলোড চালু করা হবে না।").setPositiveButton("ঠিক আছে",null).show()}
    private fun showMasnunAmal(){val a=arrayOf("আয়াতুল কুরসি","সকাল-সন্ধ্যার যিকির","ঘুমের আগে আমল","ঘর থেকে বের হওয়ার দোয়া","খাবারের দোয়া","সফরের দোয়া","ইস্তিগফার","দরুদ শরীফ");AlertDialog.Builder(this).setTitle("🤲 মাসনুন আমল").setItems(a){_,i->AlertDialog.Builder(this).setTitle(a[i]).setMessage(masnunText(a[i])).setPositiveButton("ঠিক আছে",null).show()}.setNegativeButton("বন্ধ",null).show()}
    private fun masnunText(n:String)=when(n){"আয়াতুল কুরসি"->"اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ";"ইস্তিগফার"->"أَسْتَغْفِرُ اللَّهَ";"দরুদ শরীফ"->"اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ";else->"মাসনুন আমলের পূর্ণ আরবি পাঠ পরবর্তী কনটেন্ট ডাটায় যুক্ত করা হবে।"}
    private fun showNotepad(){val e=EditText(this).apply{setText(prefs.getString("note",""));hint="আপনার নোট লিখুন...";minLines=8;gravity=Gravity.TOP};AlertDialog.Builder(this).setTitle("📝 নোটপ্যাড").setView(e).setPositiveButton("💾 সেভ"){_,_->prefs.edit().putString("note",e.text.toString()).apply();Toast.makeText(this,"নোট সেভ হয়েছে",Toast.LENGTH_SHORT).show()}.setNegativeButton("বন্ধ",null).show()}
    private fun showAboutDialog(){AlertDialog.Builder(this).setTitle("ℹ️ অ্যাপ সম্পর্কে").setMessage("উদ্যোক্তা ও পরিচালক\nসাব্বির আহমাদ\n\n📞 ০১৭২৫-২২৮৬২২\n\nবিভিন্ন মাসআলা-মাসায়েল জানতে\nFacebook Page: Satkhira Voice").setPositiveButton("ঠিক আছে",null).show()}
    private fun showLocationDialog(){AlertDialog.Builder(this).setTitle("লোকেশন নির্বাচন করুন").setItems(arrayOf("🛰️ GPS Auto Location","🇧🇩 বাংলাদেশ ম্যানুয়াল নির্বাচন")){_,w->if(w==0)enableGps()else showDivisionDialog()}.setNegativeButton("বাতিল",null).show()}
    private fun showDivisionDialog(){val a=arrayOf("খুলনা বিভাগ","ঢাকা বিভাগ","চট্টগ্রাম বিভাগ","রাজশাহী বিভাগ","সিলেট বিভাগ","বরিশাল বিভাগ","রংপুর বিভাগ","ময়মনসিংহ বিভাগ");AlertDialog.Builder(this).setTitle("বিভাগ নির্বাচন করুন").setItems(a){_,i->selectedDivision=a[i];showDistrictDialog()}.show()}
    private fun showDistrictDialog(){val a=when(selectedDivision){"খুলনা বিভাগ"->arrayOf("সাতক্ষীরা","খুলনা","যশোর","বাগেরহাট","ঝিনাইদহ","নড়াইল","কুষ্টিয়া","চুয়াডাঙ্গা","মেহেরপুর","মাগুরা");"ঢাকা বিভাগ"->arrayOf("ঢাকা","গাজীপুর","নারায়ণগঞ্জ","নরসিংদী","মুন্সীগঞ্জ","মানিকগঞ্জ","মাদারীপুর","ফরিদপুর","গোপালগঞ্জ","রাজবাড়ী","শরীয়তপুর");else->arrayOf("সদর")};AlertDialog.Builder(this).setTitle("$selectedDivision - জেলা নির্বাচন করুন").setItems(a){_,i->selectedDistrict=a[i];showThanaDialog()}.setNegativeButton("পেছনে"){_,_->showDivisionDialog()}.show()}
    private fun showThanaDialog(){val a=when(selectedDistrict){"সাতক্ষীরা"->arrayOf("শ্যামনগর","সাতক্ষীরা সদর","কালিগঞ্জ","আশাশুনি","কলারোয়া","তালা","দেবহাটা","পাটকেলঘাটা");else->arrayOf("সদর")};AlertDialog.Builder(this).setTitle("$selectedDistrict - উপজেলা নির্বাচন করুন").setItems(a){_,i->selectedThana=a[i];locationText.text="📍 $selectedThana, $selectedDistrict, $selectedDivision";updateHome()}.setNegativeButton("পেছনে"){_,_->showDistrictDialog()}.show()}
    private fun enableGps(){if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),100);return};locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,10f,this);Toast.makeText(this,"GPS লোকেশন চালু হয়েছে",Toast.LENGTH_SHORT).show()}
    override fun onLocationChanged(location:Location){latitude=location.latitude;longitude=location.longitude;locationText.text="📍 GPS: %.4f, %.4f".format(Locale.US,latitude,longitude);updateHome()}
    override fun onProviderEnabled(provider:String){}; override fun onProviderDisabled(provider:String){}; override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?){}
}
