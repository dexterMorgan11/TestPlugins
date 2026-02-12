package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class BotolatProvider : MainAPI() {
    // إعدادات الموقع
    override var mainUrl = "https://www.btolat.com"
    override var name = "Botolat Goals"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie)

    // 1. جلب قائمة الأهداف من الصفحة الرئيسية للموقع
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // نذهب لصفحة الفيديوهات في موقع بطولات
        val document = app.get("$mainUrl/video").document
        
        // نحدد العناصر التي تحتوي على الفيديوهات (الـ Selectors)
        val items = document.select("div.col-sm-6.col-md-4") 
        
        val homeItems = items.mapNotNull {
            val title = it.selectFirst("h2")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("a")?.attr("href") ?: ""
            val image = it.selectFirst("img")?.attr("src")
            
            newMovieSearchResponse(title, "$mainUrl$link", TvType.Movie) {
                this.posterUrl = image
            }
        }
        return newHomePageResponse(homeItems)
    }

    // 2. الدخول لصفحة الفيديو واستخراج المشغل
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
        
        // موقع بطولات يضع الفيديو غالباً في iframe
        val videoUrl = document.select("iframe").attr("src") ?: ""

        return newMovieLoadResponse(title, url, TvType.Movie, videoUrl) {
            this.posterUrl = poster
        }
    }

    // 3. استخراج الرابط النهائي للتشغيل
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // إذا كان الفيديو من يوتيوب أو سيرفر يدعمه التطبيق، سيتم تشغيله تلقائياً
        loadExtractor(data, data, subtitleCallback, callback)
        return true
    }
}
