package com.sabbirsamol.app

data class LibraryBook(val id:String,val title:String,val volume:String?=null,val driveUrl:String,val fileName:String)
object LibraryBooks {
 val all=listOf(
  LibraryBook("quran","পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা)",driveUrl="https://drive.google.com/file/d/1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q/view",fileName="quran_full.pdf"),
  LibraryBook("bukhari-1","সহীহ বুখারী শরীফ","১ম খণ্ড","https://drive.google.com/file/d/1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5/view","bukhari_vol_1.pdf"),
  LibraryBook("bukhari-2","সহীহ বুখারী শরীফ","২য় খণ্ড","https://drive.google.com/file/d/1velsDhV5jClX66XH2v9XXnYzvfU1RmKd/view","bukhari_vol_2.pdf"),
  LibraryBook("bukhari-3","সহীহ বুখারী শরীফ","৩য় খণ্ড","https://drive.google.com/file/d/14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR/view","bukhari_vol_3.pdf"),
  LibraryBook("bukhari-4","সহীহ বুখারী শরীফ","৪র্থ খণ্ড","https://drive.google.com/file/d/1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd/view","bukhari_vol_4.pdf"),
  LibraryBook("bukhari-5","সহীহ বুখারী শরীফ","৫ম খণ্ড","https://drive.google.com/file/d/1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX/view","bukhari_vol_5.pdf"),
  LibraryBook("bukhari-6","সহীহ বুখারী শরীফ","৬ষ্ঠ খণ্ড","https://drive.google.com/file/d/1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb/view","bukhari_vol_6.pdf"),
  LibraryBook("bukhari-7","সহীহ বুখারী শরীফ","৭ম খণ্ড","https://drive.google.com/file/d/1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9/view","bukhari_vol_7.pdf"),
  LibraryBook("bukhari-8","সহীহ বুখারী শরীফ","৮ম খণ্ড","https://drive.google.com/file/d/1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA/view","bukhari_vol_8.pdf"),
  LibraryBook("bukhari-9","সহীহ বুখারী শরীফ","৯ম খণ্ড","https://drive.google.com/file/d/1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I/view","bukhari_vol_9.pdf"),
  LibraryBook("bukhari-10","সহীহ বুখারী শরীফ","১০ম খণ্ড","https://drive.google.com/file/d/1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8/view","bukhari_vol_10.pdf")
 )
}