package com.devlogs.rssfeed.rss_parser

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import java.net.SocketTimeoutException

class RssParser(private val client: OkHttpClient) {
    sealed class Result {
        data class Success(val rssObject: RSSObject) : Result()
        class InvalidFormat : Result()
        class GeneralError : Result()
        data class ConnectionError(val errMessage: String) : Result()
    }

    private val API_URL = "https://api.rss2json.com/v1/api.json?rss_url=";

    suspend fun parse(rssUrl: String): Result {
        try {
            val apiUrl = """
                    $API_URL$rssUrl&api_key=crqz0qfs3k4uuqh1ywrh1bk5v6ojrhrclgggesov
            """.trimIndent()
            val request = Request.Builder()
                .url(apiUrl)
                .get()
                .build()
            val response = client.newCall(request).await()
            Log.d("Parseee", "$apiUrl reponse: ${response.code}")
            if (!response.isSuccessful) {
                Log.d("Parseee", "error: ${response.code}")
                return Result.GeneralError()
            }
            val json = response.body!!.string()

            val rssObject = Gson().fromJson(json, RSSObject::class.java)
            return Result.Success(rssObject)
        } catch (ex: JsonSyntaxException) {
            Log.d("Parseee", "error: Invalid format: ${ex.message}")
            return Result.InvalidFormat()
        } catch (ex: SocketTimeoutException) {
            return Result.ConnectionError("Connection timeout")
        }
    }
}
