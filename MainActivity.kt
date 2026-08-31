package com.sabbirsamol.app

import android.Manifest
import android.app.AlertDialog
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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.*

class MainActivity : ComponentActivity(), LocationListener {

    private lateinit var root: LinearLayout
    private lateinit var locationManager: LocationManager

    private var latitude = 22.7185
    private var longitude = 89.0711

    private var selectedDivision = "খুলনা বিভাগ"
    private var selectedDistrict = "সাতক্ষীরা"
    private var selectedThana = "শ্যামনগর"

    private var isGpsEnabled = false

    private lateinit var locationText: TextView
    private lateinit var prayerText: TextView
    private lateinit var countdownText: TextView

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager

        showHomeScreen()
        updatePrayerTimes()
        startCountdown()
    }

    private fun showHomeScreen() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(20, 25, 20, 25)
            setBackgroundColor(Color.rgb(245, 250, 247))
        }

        val title = TextView(this).apply {
            text = "🕌 Muslim Time"
            textSize = 25f
            setTextColor(Color.rgb(20, 83, 45))
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 15)
        }

        locationText = TextView(this).apply {
            text = "📍 $selectedThana, $selectedDistrict, $selectedDivision"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 5, 0, 12)
        }

        val changeLocation = Button(this).apply {
            text = "📍 লোকেশন পরিবর্তন"
            setOnClickListener {
                showLocationDialog()
            }
        }

        countdownText = TextView(this).apply {
            text = "পরবর্তী ওয়াক্ত হিসাব হচ্ছে..."
            textSize = 18f
            setTextColor(Color.rgb(13, 148, 136))
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        prayerText = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.rgb(30, 30, 30))
            setPadding(15, 15, 15, 15)
        }

        val refresh = Button(this).apply {
            text = "🔄 রিফ্রেশ"
            setOnClickListener {
                updatePrayerTimes()
                Toast.makeText(
                    this@MainActivity,
                    "নামাজের সময় আপডেট হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        root.addView(title)
        root.addView(locationText)
        root.addView(changeLocation)
        root.addView(countdownText)
        root.addView(prayerText)
        root.addView(refresh)

        setContentView(root)
    }

    private fun showLocationDialog() {

        val options = arrayOf(
            "🛰️ GPS Auto Location",
            "🇧🇩 বাংলাদেশ ম্যানুয়াল নির্বাচন"
        )

        AlertDialog.Builder(this)
            .setTitle("লোকেশন নির্বাচন করুন")
            .setItems(options) { _, which ->

                if (which == 0) {
                    enableGps()
                } else {
                    showDivisionDialog()
                }
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }

    private fun showDivisionDialog() {

        val divisions = arrayOf(
            "খুলনা বিভাগ",
            "ঢাকা বিভাগ",
            "চট্টগ্রাম বিভাগ",
            "রাজশাহী বিভাগ",
            "সিলেট বিভাগ",
            "বরিশাল বিভাগ",
            "রংপুর বিভাগ",
            "ময়মনসিংহ বিভাগ"
        )

        AlertDialog.Builder(this)
            .setTitle("বিভাগ নির্বাচন করুন")
            .setItems(divisions) { _, which ->

                selectedDivision = divisions[which]
                showDistrictDialog()
            }
            .show()
    }

    private fun showDistrictDialog() {

        val districts = when (selectedDivision) {

            "খুলনা বিভাগ" -> arrayOf(
                "সাতক্ষীরা",
                "খুলনা",
                "যশোর",
                "বাগেরহাট",
                "ঝিনাইদহ",
                "নড়াইল",
                "কুষ্টিয়া",
                "চুয়াডাঙ্গা",
                "মেহেরপুর",
                "মাগুরা"
            )

            "ঢাকা বিভাগ" -> arrayOf(
                "ঢাকা",
                "গাজীপুর",
                "নারায়ণগঞ্জ",
                "নরসিংদী",
                "মুন্সীগঞ্জ",
                "মানিকগঞ্জ",
                "মাদারীপুর",
                "ফরিদপুর",
                "গোপালগঞ্জ",
                "রাজবাড়ী",
                "শরীয়তপুর"
            )

            else -> arrayOf("সদর")
        }

        AlertDialog.Builder(this)
            .setTitle("$selectedDivision - জেলা নির্বাচন করুন")
            .setItems(districts) { _, which ->

                selectedDistrict = districts[which]
                showThanaDialog()
            }
            .setNegativeButton("পেছনে") { _, _ ->
                showDivisionDialog()
            }
            .show()
    }

    private fun showThanaDialog() {

        val thanas = when (selectedDistrict) {

            "সাতক্ষীরা" -> arrayOf(
                "শ্যামনগর",
                "সাতক্ষীরা সদর",
                "কালিগঞ্জ",
                "আশাশুনি",
                "দেবহাটা",
                "তালা",
                "কলারোয়া"
            )

            "খুলনা" -> arrayOf(
                "খুলনা সদর",
                "ডুমুরিয়া",
                "রূপসা",
                "দাকোপ",
                "পাইকগাছা",
                "বটিয়াঘাটা",
                "ফুলতলা",
                "তেরখাদা",
                "কয়রা"
            )

            "যশোর" -> arrayOf(
                "যশোর সদর",
                "ঝিকরগাছা",
                "শার্শা",
                "অভয়নগর",
                "মনিরামপুর",
                "কেশবপুর"
            )

            "ঢাকা" -> arrayOf(
                "ধানমন্ডি",
                "উত্তরা",
                "মিরপুর",
                "গুলশান",
                "রমনা",
                "মতিঝিল",
                "মোহাম্মদপুর",
                "সাভার",
                "কেরানীগঞ্জ"
            )

            else -> arrayOf("সদর")
        }

        AlertDialog.Builder(this)
            .setTitle("$selectedDistrict - উপজেলা নির্বাচন করুন")
            .setItems(thanas) { _, which ->

                selectedThana = thanas[which]

                locationText.text =
                    "📍 $selectedThana, $selectedDistrict, $selectedDivision"

                updatePrayerTimes()

                Toast.makeText(
                    this,
                    "লোকেশন সেট হয়েছে",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("পেছনে") { _, _ ->
                showDistrictDialog()
            }
            .show()
    }

    private fun enableGps() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )

            return
        }

        isGpsEnabled = true

        try {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                10f,
                this
            )

            Toast.makeText(
                this,
                "GPS চালু হয়েছে",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "GPS চালু করা যায়নি",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onLocationChanged(location: Location) {

        latitude = location.latitude
        longitude = location.longitude

        selectedThana = "GPS Location"

        locationText.text =
            "📍 GPS: %.4f, %.4f".format(
                Locale.US,
                latitude,
                longitude
            )

        updatePrayerTimes()
    }

    private fun updatePrayerTimes() {

        val times = calculatePrayerTimes(
            latitude,
            longitude
        )

        prayerText.text = """
            
            🕌 আজকের নামাজের সময়
            
            🌙 ফজর        ${times.fajr}
            🌅 সূর্যোদয়    ${times.sunrise}
            ☀️ যোহর       ${times.dhuhr}
            🌤️ আসর        ${times.asr}
            🌇 মাগরিব     ${times.maghrib}
            🌙 এশা        ${times.isha}
            
            🚫 নিষিদ্ধ সময়
            
            🌅 সূর্যোদয়: ${times.sunriseForbidden}
            ☀️ মধ্যাহ্ন: ${times.zenithForbidden}
            🌇 সূর্যাস্ত: ${times.sunsetForbidden}
            
        """.trimIndent()
    }

    private fun startCountdown() {

        handler.post(object : Runnable {

            override fun run() {

                updateCountdown()

                handler.postDelayed(
                    this,
                    1000L
                )
            }
        })
    }

    private fun updateCountdown() {

        val times = calculatePrayerTimes(
            latitude,
            longitude
        )

        val now = Calendar.getInstance()

        val prayerList = listOf(
            "ফজর" to times.fajr,
            "যোহর" to times.dhuhr,
            "আসর" to times.asr,
            "মাগরিব" to times.maghrib,
            "এশা" to times.isha
        )

        var nextName = ""
        var nextTime = 0L

        for ((name, time) in prayerList) {

            val millis = timeToMillis(time)

            if (millis > now.timeInMillis) {

                nextName = name
                nextTime = millis
                break
            }
        }

        if (nextName.isEmpty()) {

            nextName = "ফজর"
            nextTime = timeToMillis(times.fajr) +
                    24 * 60 * 60 * 1000
        }

        val diff = nextTime - now.timeInMillis

        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        val seconds = (diff / 1000) % 60

        countdownText.text =
            "⏳ পরবর্তী ওয়াক্ত: $nextName\n" +
                    "%02d:%02d:%02d".format(
                        hours,
                        minutes,
                        seconds
                    )
    }

    private fun timeToMillis(time: String): Long {

        val parts = time.split(":")

        val cal = Calendar.getInstance()

        cal.set(
            Calendar.HOUR_OF_DAY,
            parts[0].toInt()
        )

        cal.set(
            Calendar.MINUTE,
            parts[1].toInt()
        )

        cal.set(
            Calendar.SECOND,
            0
        )

        cal.set(
            Calendar.MILLISECOND,
            0
        )

        return cal.timeInMillis
    }

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String,
        val sunriseForbidden: String,
        val zenithForbidden: String,
        val sunsetForbidden: String
    )

    private fun calculatePrayerTimes(
        lat: Double,
        lon: Double
    ): PrayerTimes {

        val calendar = Calendar.getInstance()

        val day = calendar.get(Calendar.DAY_OF_YEAR)

        val declination =
            23.45 * sin(
                Math.toRadians(
                    360.0 / 365.0 * (284 + day)
                )
            )

        val b =
            Math.toRadians(
                (360.0 / 365.0) * (day - 81)
            )

        val equation =
            9.87 * sin(2 * b) -
                    7.53 * cos(b) -
                    1.5 * sin(b)

        val timezone =
            TimeZoneOffset()

        val solarNoon =
            12.0 +
                    timezone -
                    lon / 15.0 -
                    equation / 60.0

        val sunrise =
            solarTime(
                solarNoon,
                lat,
                declination,
                -0.833
            )

        val sunset =
            solarTime(
                solarNoon,
                lat,
                declination,
                -0.833,
                true
            )

        val fajr =
            solarTime(
                solarNoon,
                lat,
                declination,
                -18.0
            )

        val isha =
            solarTime(
                solarNoon,
                lat,
                declination,
                -18.0,
                true
            )

        val dhuhr =
            solarNoon + 0.05

        val asr =
            solarAsr(
                solarNoon,
                lat,
                declination
            )

        val fajrText = formatTime(fajr)
        val sunriseText = formatTime(sunrise)
        val dhuhrText = formatTime(dhuhr)
        val asrText = formatTime(asr)
        val sunsetText = formatTime(sunset)
        val ishaText = formatTime(isha)

        val zenithStart =
            formatTime(dhuhr - 0.05)

        val zenithEnd =
            formatTime(dhuhr + 0.05)

        return PrayerTimes(
            fajrText,
            sunriseText,
            dhuhrText,
            asrText,
            sunsetText,
            ishaText,
            "$sunriseText - ${formatTime(sunrise + 0.20)}",
            "$zenithStart - $zenithEnd",
            "${formatTime(sunset - 0.20)} - $sunsetText"
        )
    }

    private fun solarTime(
        noon: Double,
        latitude: Double,
        declination: Double,
        angle: Double,
        evening: Boolean = false
    ): Double {

        val latRad =
            Math.toRadians(latitude)

        val decRad =
            Math.toRadians(declination)

        val cosH =
            (
                    sin(Math.toRadians(angle)) -
                            sin(latRad) * sin(decRad)
                    ) /
                    (
                            cos(latRad) * cos(decRad)
                            )

        val hourAngle =
            Math.toDegrees(
                acos(
                    cosH.coerceIn(-1.0, 1.0)
                )
            ) / 15.0

        return if (evening) {
            noon + hourAngle
        } else {
            noon - hourAngle
        }
    }

    private fun solarAsr(
        noon: Double,
        latitude: Double,
        declination: Double
    ): Double {

        val latRad =
            Math.toRadians(latitude)

        val decRad =
            Math.toRadians(declination)

        val shadow =
            2.0

        val angle =
            Math.toDegrees(
                atan(
                    1.0 /
                            (
                                    shadow +
                                            tan(
                                                abs(
                                                    latRad - decRad
                                                )
                                            )
                                    )
                )
            )

        return solarTime(
            noon,
            latitude,
            declination,
            angle,
            true
        )
    }

    private fun formatTime(decimalHour: Double): String {

        var hour =
            floor(decimalHour).toInt()

        var minute =
            ((decimalHour - hour) * 60)
                .roundToInt()

        if (minute >= 60) {
            hour++
            minute -= 60
        }

        hour =
            ((hour % 24) + 24) % 24

        return "%02d:%02d".format(
            hour,
            minute
        )
    }

    private fun TimeZoneOffset(): Double {
        return 6.0
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {}

    override fun onDestroy() {

        super.onDestroy()

        handler.removeCallbacksAndMessages(null)

        if (::locationManager.isInitialized) {

            try {
                locationManager.removeUpdates(this)
            } catch (_: Exception) {
            }
        }
    }
}
