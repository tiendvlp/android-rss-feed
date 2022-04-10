package com.devlogs.rssfeed.encrypt

import android.annotation.SuppressLint
import java.util.*

object UrlEncrypt {
    @SuppressLint("NewApi")
    fun encode (url: String) : String {
        var minimizeUrl = url

        // www.url.com/ => www.url.com
        if (minimizeUrl.last().equals('/')) {
            minimizeUrl = minimizeUrl.substring(0, url.length-1)
        }

        var result = Base64.getEncoder().encodeToString(minimizeUrl.toByteArray())
        result = result.replace("/", "|")
        return result
    }

    @SuppressLint("NewApi")
    fun decode (code: String) : String {
       val target = code.replace("|", "/")
       val bytes = Base64.getDecoder().decode(target)
       return String(bytes)
    }
}