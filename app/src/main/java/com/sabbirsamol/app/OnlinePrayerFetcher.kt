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

    private val districtOffsets = mapOf(
        "ঢাকা" to 0,
        "ফরিদপুর" to 2,
        "গোপালগঞ্জ" to 2,
        "জামালপুর" to -1,
        "কিশোরগঞ্জ" to -2,
        "মাদারীপুর" to 1,
        "মানিকগঞ্জ" to 1,
        "মুন্সিগঞ্জ" to 0,
        "ময়মনসিংহ" to -1,
        "নারায়ণগঞ্জ" to 0,
        "নরসিংদী" to -1,
        "নেত্রকোণা" to -2,
        "রাজবাড়ী" to 2,
        "শরীয়তপুর" to 1,
        "শেরপুর" to -1,
        "টাঙ্গাইল" to 1,
        "বগুড়া" to 3,
        "জয়পুরহাট" to 3,
        "নওগাঁ" to 4,
        "নাটোর" to 3,
        "নবাবগঞ্জ" to 5,
        "পাবনা" to 2,
        "রাজশাহী" to 5,
        "সিরাজগঞ্জ" to 2,
        "দিনাজপুর" to 6,
        "গাইবান্ধা" to 3,
        "কুড়িগ্রাম" to 2,
        "লালমনিরহাট" to 3,
        "নীলফামারী" to 4,
        "পঞ্চগড়" to 5,
        "রংপুর" to 4,
        "ঠাকুরগাঁও" to 6,
        "বাগেরহাট" to 3,
        "চুয়াডাঙ্গা" to 5,
        "যশোর" to 4,
        "ঝিনাইদহ" to 4,
        "খুলনা" to 3,
        "কুষ্টিয়া" to 4,
        "মাগুরা" to 3,
        "মেহেরপুর" to 5,
        "নড়াইল" to 3,
        "সাতক্ষীরা" to 3,
        "বরগুনা" to 2,
        "বরিশাল" to 2,
        "ভোলা" to 0,
        "ঝালকাঠি" to 2,
        "পটুয়াখালী" to 1,
        "পিরোজপুর" to 2,
        "বান্দরবান" to -6,
        "ব্রাহ্মণবাড়িয়া" to -3,
        "চাঁদপুর" to -2,
        "চট্টগ্রাম" to -5,
        "কুমিল্লা" to -3,
        "কক্সবাজার" to -5,
        "ফেনী" to -4,
        "খাগড়াছড়ি" to -5,
        "লক্ষ্মীপুর" to -2,
        "নোয়াখালী" to -3,
        "রাঙামাটি" to -6,
        "হবিগঞ্জ" to -4,
        "মৌলভীবাজার" to -5,
        "সুনামগঞ্জ" to -4,
        "সিলেট" to -5
    )

    fun fetchTimingsForDistrict(districtName: String): Map<String, String>? {
        return try {
            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(Date())
            val urlStr = "https://api.aladhan.com/v1/timings/$currentDate?latitude=23.8103&longitude=90.4125&method=1"

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

                val offset = districtOffsets[districtName] ?: 3

                fun adjustTime(timeStr: String, minutes: Int): String {
                    val parts = timeStr.trim().split(":")
                    if (parts.size < 2) return timeStr
                    val rawMin = parts[0].toInt() * 60 + parts[1].toInt() + minutes
                    val totalMin = (rawMin % 1440 + 1440) % 1440
                    val h = totalMin / 60
                    val m = totalMin % 60
                    return String.format(Locale.ENGLISH, "%02d:%02d", h, m)
                }

                val map = mutableMapOf<String, String>()
                map["Fajr"] = adjustTime(timings.getString("Fajr"), offset)
                map["Sunrise"] = adjustTime(timings.getString("Sunrise"), offset)
                map["Dhuhr"] = adjustTime(timings.getString("Dhuhr"), offset)
                map["Asr"] = adjustTime(timings.getString("Asr"), offset)
                map["Sunset"] = adjustTime(timings.getString("Sunset"), offset)
                map["Maghrib"] = adjustTime(timings.getString("Maghrib"), offset)
                map["Isha"] = adjustTime(timings.getString("Isha"), offset)

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
