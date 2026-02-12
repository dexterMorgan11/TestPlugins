package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BotolatProvider : MainAPI() {
    override var mainUrl = "https://www.btolat.com"
    override var name = "Botolat Goals" // الاسم اللي هيظهر في التطبيق
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/video").document
        // سحب الفيديوهات من موقع بطولات
        val items = document.select("div.videoItem, div.col-sm-6.col-md-4") 
        val home = items.mapNotNull {
            val title = it.selectFirst("h2, h3")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("a")?.attr("href") ?: ""
            val poster = it.selectFirst("img")?.attr("src")

            newMovieSearchResponse(title, "$mainUrl$link", TvType.Movie) {
                this.posterUrl = poster
            }
        }
        return newHomePageResponse(home)
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: ""
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
        
        // جلب رابط الفيديو من الـ Iframe
        val videoUrl = document.select("iframe").attr("src") ?: ""

        return newMovieLoadResponse(title, url, TvType.Movie, videoUrl) {
            this.posterUrl = poster
        }
    }
}
