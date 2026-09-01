package com.sabbirsamol.app

data class QuranNavigationItem(
    val title: String,
    val page: Int
)

object QuranNavigation {
    /**
     * Known page anchors supplied for this Quran PDF.
     * PDF page numbering is kept as provided by the user.
     */
    val surahs: List<QuranNavigationItem> = listOf(
        QuranNavigationItem("সূরা ফাতিহা", 3)
    )

    val paras: List<QuranNavigationItem> = listOf(
        QuranNavigationItem("২৯তম পারা", 24)
    )
}
