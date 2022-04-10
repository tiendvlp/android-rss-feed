package com.devlogs.rssfeed.common.helper

import android.content.Context
import java.lang.Exception
import java.net.InetAddress
import androidx.core.content.ContextCompat.getSystemService

import android.net.ConnectivityManager
import android.net.NetworkInfo


object InternetChecker {


    fun Context.isOnline(): Boolean {
        val connMgr = getSystemService(ConnectivityManager::class.java)
        val networkInfo: NetworkInfo? = connMgr?.activeNetworkInfo
        return networkInfo?.isConnected == true
    }
    fun Context.isNetworkConnected(context: Context): Boolean {
        val connMgr = getSystemService(ConnectivityManager::class.java)
        val networkInfo: NetworkInfo? = connMgr?.activeNetworkInfo
        var isWifiConn: Boolean = false
        var isMobileConn: Boolean = false
        connMgr?.allNetworks?.forEach { network ->
            connMgr.getNetworkInfo(network).apply {
                    isWifiConn = isWifiConn or (networkInfo?.isConnected == true)
                    isMobileConn = isMobileConn or (networkInfo?.isConnected == true)
                }
            }
        return isWifiConn || isMobileConn
    }
}