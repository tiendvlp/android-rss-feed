package com.devlogs.rssfeed.rss

import android.util.Log
import com.devlogs.rssfeed.common.helper.LogTarget
import com.devlogs.rssfeed.common.helper.normalLog
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.lang.IllegalArgumentException

class RssUrlFinder constructor(private val client: OkHttpClient) : LogTarget {

    sealed class Result {
        data class Success (val rssUrl: String) : Result ()
        class RssNotFound : Result ()
        class IllegalUrl : Result()
    }

    suspend fun find (url: String) : Result {
        try {
            Log.d("RssUrlFinder", "Find: " + url)
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = client.newCall(request).await()
                if (!response.isSuccessful) return Result.RssNotFound()
                val htmlBody = response.body!!.string()
                val index = htmlBody.indexOf("application/rss+xml")

                if (index != -1) {
                    Log.d("RssUrlFinder", "Found rss Url")
                    val linkTagIndex = htmlBody.substring(0, index).replace("\\s+","").lastIndexOf("<link")
                    val rssUrlStartIndex = htmlBody.indexOf("href", linkTagIndex)
                    if (rssUrlStartIndex != -1) {
                        var rssUrl = htmlBody.substring(rssUrlStartIndex + 4 + 2, htmlBody.indexOf("\"", rssUrlStartIndex + 6))
                        if (!rssUrl.contains("http")) {
                            rssUrl = "$url/$rssUrl"
                        }
                        normalLog("Found rssUrl: $rssUrl")

                        return Result.Success(rssUrl)
                    }
                }
            Log.d("RssUrlFinder", "Not found rss Url")
            return Result.RssNotFound();
        } catch (ex : Exception) {
            return Result.IllegalUrl()
        }

    }
}