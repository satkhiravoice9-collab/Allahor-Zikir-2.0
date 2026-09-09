package com.sabbirsamol.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection

object OnlinePrayerFetcher {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private val districtCoordinates = mapOf(
        "ঢাকা" to Pair(23.8103, 90.4125),
        "ফরিদপুর" to Pair(23.6070, 89.8423),
        "গোপালগঞ্জ" to Pair(23.0051, 89.8265),
        "জামালপুর" to Pair(24.9375, 89.9377),
        "কিশোরগঞ্জ" to Pair(24.4449, 90.7766),
        "মাদারীপুর" to Pair(23.1641, 90.2013),
        "মানিকগঞ্জ" to Pair(23.8644, 90.0047),
        "মুন্সিগঞ্জ" to Pair(23.5422, 90.5305),
        "ময়মনসিংহ" to Pair(24.7471, 90.4203),
        "নারায়ণগঞ্জ" to Pair(23.6336, 90.4964),
        "নরসিংদী" to Pair(23.9322, 90.7151),
        "নেত্রকোণা" to Pair(24.8709, 90.7276),
        "রাজবাড়ী" to Pair(23.7574, 89.6445),
        "শরীয়তপুর" to Pair(23.2423, 90.3432),
        "শেরপুর" to Pair(25.0204, 90.0153),
        "টাঙ্গাইল" to Pair(24.2513, 89.9167),
        "বগুড়া" to Pair(24.8465, 89.3777),
        "জয়পুরহাট" to Pair(25.0968, 89.0227),
        "নওগাঁ" to Pair(24.7936, 88.9318),
        "নাটোর" to Pair(24.4205, 89.0003),
        "নবাবগঞ্জ" to Pair(24.5952, 88.2773),
        "পাবনা" to Pair(24.0032, 89.2473),
        "রাজশাহী" to Pair(24.3636, 88.6241),
        "সিরাজগঞ্জ" to Pair(24.4533, 89.7008),
        "দিনাজপুর" to Pair(25.6217, 88.6355),
        "গাইবান্ধা" to Pair(25.3287, 89.5428),
        "কুড়িগ্রাম" to Pair(25.8054, 89.6362),
        "লালমনিরহাট" to Pair(25.9178, 89.4478),
        "নীলফামারী" to Pair(25.9317, 88.8560),
        "পঞ্চগড়" to Pair(26.3411, 88.5542),
        "রংপুর" to Pair(25.7439, 89.2752),
        "ঠাকুরগাঁও" to Pair(26.0330, 88.4616),
        "বাগেরহাট" to Pair(22.6515, 89.7859),
        "চুয়াডাঙ্গা" to Pair(23.6401, 88.8418),
        "যশোর" to Pair(23.1664, 89.2081),
        "ঝিনাইদহ" to Pair(23.5450, 89.1726),
        "খুলনা" to Pair(22.8456, 89.5403),
        "কুষ্টিয়া" to Pair(23.9012, 89.1204),
        "মাগুরা" to Pair(23.4873, 89.4198),
        "মেহেরপুর" to Pair(23.7622, 88.6318),
        "নড়াইল" to Pair(23.1725, 89.5126),
        "সাতক্ষীরা" to Pair(22.7185, 89.0705),
        "বরগুনা" to Pair(22.1557, 90.1259),
        "বরিশাল" to Pair(22.7010, 90.3535),
        "ভোলা" to Pair(22.6823, 90.6482),
        "ঝালকাঠি" to Pair(22.6406, 90.1987),
        "পটুয়াখালী" to Pair(22.3596, 90.3298),
        "পিরোজপুর" to Pair(22.5841, 89.9720),
        "বান্দরবান" to Pair(22.1953, 92.2184),
        "ব্রাহ্মণবাড়িয়া" to Pair(23.9571, 91.1118),
        "চাঁদপুর" to Pair(23.2321, 90.6633),
        "চট্টগ্রাম" to Pair(22.3569, 91.7832),
        "কুমিল্লা" to Pair(23.4682, 91.1788),
        "কক্সবাজার" to Pair(21.4272, 92.0058),
        "ফেনী" to Pair(23.0185, 91.3975),
        "খাগড়াছড়ি" to Pair(23.1192, 91.9847),
        "লক্ষ্মীপুর" to Pair(22.9447, 90.8412),
        "নোয়াখালী" to Pair(22.8695, 91.0994),
        "রাঙামাটি" to Pair(22.6533, 92.1751),
        "হবিগঞ্জ" to Pair(24.3745, 91.4137),
        "মৌলভীবাজার" to Pair(24.4829, 91.7774),
        "সুনামগঞ্জ" to Pair(25.0658, 91.3950),
        "সিলেট" to Pair(24.8949, 91.8687)
    )

    fun fetchTimingsForDistrict(districtName: String): Map<String, String>? {
        return try {
            val coord = districtCoordinates[districtName] ?: districtCoordinates["সাতক্ষীরা"]!!
            val lat = coord.first
            val lng = coord.second

            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
            // method=1 (University of Islamic Sciences, Karachi) এবং school=1 (Hanafi Asr calculation)
            val urlStr = "https://api.aladhan.com/v1/timings/$currentDate?latitude=$lat&longitude=$lng&method=1&school=1"

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val data = json.getJSONObject("data")
                val timings = data.getJSONObject("timings")

                val map = mutableMapOf<String, String>()
                map["Fajr"] = timings.getString("Fajr")
                map["Sunrise"] = timings.getString("Sunrise")
                map["Dhuhr"] = timings.getString("Dhuhr")
                map["Asr"] = timings.getString("Asr")
                map["Sunset"] = timings.getString("Sunset")
                map["Maghrib"] = timings.getString("Maghrib")
                map["Isha"] = timings.getString("Isha")

                connection.disconnect()
                return map
            }
            connection.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
