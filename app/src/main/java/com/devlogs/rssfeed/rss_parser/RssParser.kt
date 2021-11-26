package com.devlogs.rssfeed.rss_parser

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import ru.gildor.coroutines.okhttp.await
import java.lang.Exception

class RssParser(val client: OkHttpClient) {
    sealed class Result {
        data class Success (val rssObject: RSSObject) : Result ()
        class InvalidFormat : Result ()
        class GeneralError : Result()
    }

    private val API_URL = "https://api.rss2json.com/v1/api.json?rss_url=";

    suspend fun parse (rssUrl: String) : Result {
        val request = Request.Builder()
            .url(API_URL + rssUrl)
            .get()
            .build()

        val response = client.newCall(request).await()

        if (!response.isSuccessful) return Result.GeneralError()

        val json = response.body!!.string()

        try {
            val rssObject = Gson().fromJson(json, RSSObject::class.java)
            return Result.Success(rssObject)
        } catch (ex: JsonSyntaxException) {
            return Result.InvalidFormat()
        }
    }
}
