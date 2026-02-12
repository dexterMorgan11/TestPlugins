package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BotolatProvider : MainAPI() {
    override var mainUrl = "https://www.btolat.com"
    override var name = "Botolat Goals"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        // صفحة الفيديوهات في موقع بطولات
        val document = app.get("$mainUrl/video").document
        // تحديد عناصر الفيديو
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

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
        
        // استخراج رابط الفيديو من الـ Iframe
        val videoUrl = document.select("iframe").attr("src") ?: ""

        return newMovieLoadResponse(title, url, TvType.Movie, videoUrl) {
            this.posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        loadExtractor(data, data, subtitleCallback, callback)
        return true
    }
}
