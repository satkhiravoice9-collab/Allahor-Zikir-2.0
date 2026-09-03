package com.sabbirsamol.app

import android.content.Context
import android.graphics.Color

object ThemeManager {
    data class ThemeColors(
        val bgMain: Int,
        val cardBg: Int,
        val cardStroke: Int,
        val textMain: Int,
        val textSub: Int,
        val btnBg: Int
    )

    fun getTheme(context: Context): ThemeColors {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val themeName = prefs.getString("app_theme", "গাঢ় থেম (ডিফল্ট)") ?: "গাঢ় থেম (ডিফল্ট)"

        return if (themeName.contains("সাদা") || themeName.contains("লাইট")) {
            // লাইট থেম
            ThemeColors(
                bgMain = Color.parseColor("#F8FAFC"),
                cardBg = Color.parseColor("#FFFFFF"),
                cardStroke = Color.parseColor("#CBD5E1"),
                textMain = Color.parseColor("#0F172A"),
                textSub = Color.parseColor("#475569"),
                btnBg = Color.parseColor("#047857")
            )
        } else {
            // ডার্ক থেম (ডিফল্ট)
            ThemeColors(
                bgMain = Color.parseColor("#0B0F19"),
                cardBg = Color.parseColor("#111827"),
                cardStroke = Color.parseColor("#374151"),
                textMain = Color.parseColor("#F9FAFB"),
                textSub = Color.parseColor("#9CA3AF"),
                btnBg = Color.parseColor("#10B981")
            )
        }
    }
}
