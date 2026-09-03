package com.sabbirsamol.app

import android.content.Context
import android.graphics.Color

object ThemeManager {
    class ThemeColors(
        val bgMain: Int, 
        val cardBg: Int, 
        val cardStroke: Int,
        val textAccent: Int, 
        val btnBg: Int, 
        val textMain: Int, 
        val textSub: Int
    )

    fun getTheme(context: Context): ThemeColors {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val themeName = prefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)") ?: "মদিনা থিম (এমরেল্ড গ্রিন)"

        return when {
            themeName.contains("সাদা") -> ThemeColors(
                Color.parseColor("#F3F4F6"), Color.WHITE, Color.parseColor("#E5E7EB"),
                Color.parseColor("#059669"), Color.parseColor("#FBBF24"), Color.BLACK, Color.DKGRAY
            )
            themeName.contains("কাবা") -> ThemeColors(
                Color.parseColor("#111827"), Color.parseColor("#1F2937"), Color.parseColor("#374151"),
                Color.parseColor("#FBBF24"), Color.parseColor("#F59E0B"), Color.WHITE, Color.LTGRAY
            )
            themeName.contains("সুবহে") -> ThemeColors(
                Color.parseColor("#451A03"), Color.parseColor("#78350F"), Color.parseColor("#92400E"),
                Color.parseColor("#FDE047"), Color.parseColor("#FACC15"), Color.WHITE, Color.parseColor("#FEF3C7")
            )
            else -> ThemeColors( // মদিনা থিম (ডিফল্ট)
                Color.parseColor("#091C14"), Color.parseColor("#114D3C"), Color.parseColor("#1B785B"),
                Color.parseColor("#FBBF24"), Color.parseColor("#FACC15"), Color.WHITE, Color.parseColor("#D1D5DB")
            )
        }
    }
}
