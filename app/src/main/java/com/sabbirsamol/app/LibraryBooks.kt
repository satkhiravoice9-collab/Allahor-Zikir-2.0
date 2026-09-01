package com.sabbirsamol.app

data class LibraryBook(
    val id: String,
    val title: String,
    val volume: String? = null,
    val driveUrl: String,
    val isQuran: Boolean = false
)

object LibraryBooks {
    val all: List<LibraryBook> = listOf(
        LibraryBook("quran", "পবিত্র কুরআন শরীফ", driveUrl = "https://drive.google.com/file/d/1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q/view?usp=drivesdk", isQuran = true),

        LibraryBook("bukhari-1", "সহিহ বুখারী শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5/view?usp=drivesdk"),
        LibraryBook("bukhari-2", "সহিহ বুখারী শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1velsDhV5jClX66XH2v9XXnYzvfU1RmKd/view?usp=drivesdk"),
        LibraryBook("bukhari-3", "সহিহ বুখারী শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR/view?usp=drivesdk"),
        LibraryBook("bukhari-4", "সহিহ বুখারী শরীফ", "৪র্থ খণ্ড", "https://drive.google.com/file/d/1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd/view?usp=drivesdk"),
        LibraryBook("bukhari-5", "সহিহ বুখারী শরীফ", "৫ম খণ্ড", "https://drive.google.com/file/d/1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX/view?usp=drivesdk"),
        LibraryBook("bukhari-6", "সহিহ বুখারী শরীফ", "৬ষ্ঠ খণ্ড", "https://drive.google.com/file/d/1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb/view?usp=drivesdk"),
        LibraryBook("bukhari-7", "সহিহ বুখারী শরীফ", "৭ম খণ্ড", "https://drive.google.com/file/d/1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9/view?usp=drivesdk"),
        LibraryBook("bukhari-8", "সহিহ বুখারী শরীফ", "৮ম খণ্ড", "https://drive.google.com/file/d/1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA/view?usp=drivesdk"),
        LibraryBook("bukhari-9", "সহিহ বুখারী শরীফ", "৯ম খণ্ড", "https://drive.google.com/file/d/1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I/view?usp=drivesdk"),
        LibraryBook("bukhari-10", "সহিহ বুখারী শরীফ", "১০ম খণ্ড", "https://drive.google.com/file/d/1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8/view?usp=drivesdk"),

        LibraryBook("muslim-1", "সহিহ মুসলিম শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA/view?usp=drivesdk"),
        LibraryBook("muslim-2", "সহিহ মুসলিম শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG/view?usp=drivesdk"),
        LibraryBook("muslim-3", "সহিহ মুসলিম শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/11Ul0Laj9YGGV37KYnep73RCUELz0yvU8/view?usp=drivesdk"),
        LibraryBook("muslim-4", "সহিহ মুসলিম শরীফ", "৪র্থ খণ্ড", "https://drive.google.com/file/d/16cTetFECwjEPtSHeajv_WTQ11LTATtJp/view?usp=drivesdk"),
        LibraryBook("muslim-5", "সহিহ মুসলিম শরীফ", "৫ম খণ্ড", "https://drive.google.com/file/d/129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z/view?usp=drivesdk"),
        LibraryBook("muslim-6", "সহিহ মুসলিম শরীফ", "৬ষ্ঠ খণ্ড", "https://drive.google.com/file/d/1o4mAG-Qx6KSumsohKgUK2k8S7TAy3PKp/view?usp=drivesdk"),
        LibraryBook("muslim-7", "সহিহ মুসলিম শরীফ", "৭ম খণ্ড", "https://drive.google.com/file/d/121hx1VQv0HCZnztDFrhJrP4XPBuEzuQW/view?usp=drivesdk"),
        LibraryBook("muslim-8", "সহিহ মুসলিম শরীফ", "৮ম খণ্ড", "https://drive.google.com/file/d/1mjTLc_svuuKcUlnQXRu0e3PEAZUzn7_u/view?usp=drivesdk"),

        LibraryBook("abu-dawud-1", "আবু দাউদ শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/15RGRxSsJeKSfITiCU20AOm477jrXf7tX/view?usp=drivesdk"),
        LibraryBook("abu-dawud-2", "আবু দাউদ শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1LLjLCwj-CXLv6RHaTSHpfKK4zPBj6mRn/view?usp=drivesdk"),
        LibraryBook("abu-dawud-3", "আবু দাউদ শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/1KD64uPxeVDliawvEKwOFNaNfH883rR6X/view?usp=drivesdk"),
        LibraryBook("abu-dawud-4", "আবু দাউদ শরীফ", "৪র্থ খণ্ড", "https://drive.google.com/file/d/1iuOGcHAegMxqJ3VZ7HiLNXTbz8a-iooI/view?usp=drivesdk"),

        LibraryBook("tirmidhi-1", "তিরমিজি শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/1tN-N6skALr_G83cXreKmo0mRIqW_siGx/view?usp=drivesdk"),
        LibraryBook("tirmidhi-2", "তিরমিজি শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1FCwMfqmzNHHrbfQr3ho502obdHAaSGr4/view?usp=drivesdk"),
        LibraryBook("tirmidhi-3", "তিরমিজি শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/1Z5FxEDR_dcDFVgWTcx5lxfijVUSAxjcN/view?usp=drivesdk"),
        LibraryBook("tirmidhi-4", "তিরমিজি শরীফ", "৪র্থ খণ্ড", "https://drive.google.com/file/d/1yIiNsyBpYnJV5y8gnWAor23VwKgpD4N6/view?usp=drivesdk"),
        LibraryBook("tirmidhi-5", "তিরমিজি শরীফ", "৫ম খণ্ড", "https://drive.google.com/file/d/18GvNtMs_VK9fb1_oCyHR4nWwz-_6W53h/view?usp=drivesdk"),
        LibraryBook("tirmidhi-6", "তিরমিজি শরীফ", "৬ষ্ঠ খণ্ড", "https://drive.google.com/file/d/1D1dnryvH8iOY69ixBtGPQuF3_kmPRgu_/view?usp=drivesdk"),

        LibraryBook("nasai-1", "নাসাঈ শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/1VH9AkmVv3apCXYPBRu0mI7qmNsU66sZZ/view?usp=drivesdk"),
        LibraryBook("nasai-2", "নাসাঈ শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1t_F71oekP3F1Dc2SDY2I-L5eyDgtPcIR/view?usp=drivesdk"),
        LibraryBook("nasai-3", "নাসাঈ শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/1mjp-ZSvvBsLzkDRXj87oSrH88ifzLIKF/view?usp=drivesdk"),
        LibraryBook("nasai-4", "নাসাঈ শরীফ", "৪র্থ খণ্ড", "https://drive.google.com/file/d/19yY0S3jLumKpxff3n_Kf6qoX8A6Fmwqi/view?usp=drivesdk"),

        LibraryBook("ibn-majah-1", "ইবনে মাজাহ শরীফ", "১ম খণ্ড", "https://drive.google.com/file/d/12nAeV_DjOOCK2WJmrFSSIPyf2fcAyWzd/view?usp=drivesdk"),
        LibraryBook("ibn-majah-2", "ইবনে মাজাহ শরীফ", "২য় খণ্ড", "https://drive.google.com/file/d/1B9ZNJrMmTW1sPXtAX3n2VZj4j4xjG-U6/view?usp=drivesdk"),
        LibraryBook("ibn-majah-3", "ইবনে মাজাহ শরীফ", "৩য় খণ্ড", "https://drive.google.com/file/d/1GVexhALQ3ISCd241Zj7PkgpZG-116Q59/view?usp=drivesdk")
    )

    val quran: LibraryBook = all.first()
    val hadithBooks: List<LibraryBook> = all.drop(1)
}
