/*
* Copyright (c) 2021 by VeriFone, Inc.
* All Rights Reserved.
* THIS FILE CONTAINS PROPRIETARY AND CONFIDENTIAL INFORMATION
* AND REMAINS THE UNPUBLISHED PROPERTY OF VERIFONE, INC.
*
* Use, disclosure, or reproduction is prohibited
* without prior written approval from VeriFone, Inc.
*/

package com.priv.verifone.psdk.sdiapplication.ui.platform

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build

class WiFiConnector {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    fun connectToWiFi(ssid: String?, password: String?, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid!!)
                .setWpa2Passphrase(password!!)
                .build()
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    // This network should be used by the application
                    connectivityManager.bindProcessToNetwork(network)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    // Handle the case when the specified network is not available
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    // Handle the case when the network connection is lost
                    connectivityManager.bindProcessToNetwork(null) // Unbind from the network
                }
            }
            connectivityManager.requestNetwork(request,
                networkCallback as ConnectivityManager.NetworkCallback
            )
        }
    }

    fun unregisterNetworkCallback(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && networkCallback != null) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback!!)
        }
    }
}
