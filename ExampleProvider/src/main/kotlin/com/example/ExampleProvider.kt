package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app

class ExampleProvider : MainAPI() {
    override var mainUrl = "https://www.btolat.com"
    override var name = "Botolat Goals"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("$mainUrl/video").document
        val items = document.select("div.col-sm-6.col-md-4") 
        
        val homeItems = items.mapNotNull {
            val title = it.selectFirst("h3, h2")?.text() ?: return@mapNotNull null
            val link = it.selectFirst("a")?.attr("href") ?: ""
            val image = it.selectFirst("img")?.attr("src")
            
            newMovieSearchResponse(title, "$mainUrl$link", TvType.Movie) {
                this.posterUrl = image
            }
        }
        
        // التعديل هنا: نضع القائمة داخل HomePageList مع عنوان للقسم
        return newHomePageResponse(
            listOf(HomePageList("أحدث الأهداف", homeItems)),
            hasNext = false
        )
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1")?.text() ?: "Botolat Video"
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")
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
        if (data.isBlank()) return false
        loadExtractor(data, data, subtitleCallback, callback)
        return true
    }
}
