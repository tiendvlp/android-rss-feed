package com.devlogs.rssfeed.rss

import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await

class RssUrlFinder constructor(private val client: OkHttpClient) {

    sealed class Result {
        data class Success (val rssUrl: String) : Result ()
        class RssNotFound : Result ()
    }

    suspend fun find (url: String) : Result {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = client.newCall(request).await()
            if (!response.isSuccessful) return Result.RssNotFound()
            val htmlBody = response.body!!.string()
            val index = htmlBody.indexOf("application/rss+xml")

            if (index != -1) {
                val linkTagIndex = htmlBody.substring(0, index).replace("\\s+","").lastIndexOf("<link")
                val rssUrlStartIndex = htmlBody.indexOf("href", linkTagIndex)
                if (rssUrlStartIndex != -1) {
                    val rssUrl = htmlBody.substring(rssUrlStartIndex + 4 + 2, htmlBody.indexOf("\"", rssUrlStartIndex + 6))
                    return Result.Success(rssUrl)
                }
            }
            return Result.RssNotFound();
    }
}