package com.pomplarg.spe95.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET

fun hasConnectivity(context: Context?): Boolean {
    val cm = context?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasCapability(NET_CAPABILITY_INTERNET) == true
}