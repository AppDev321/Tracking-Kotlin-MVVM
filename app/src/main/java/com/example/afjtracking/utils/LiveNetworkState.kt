package com.example.afjtracking.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow

class LiveNetworkState (val context: Context){

     private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    val isConnected = callbackFlow<Boolean> {

        val callback = object: ConnectivityManager.NetworkCallback(){
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .apply {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                }
            }.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        trySend(MonitorConnectivity.isConnected(connectivityManager))
        connectivityManager.registerNetworkCallback(request,callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }



}








object MonitorConnectivity {
    private val IMPL = if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
        Marshmallow
    }else{
        LowerMarshmallow
    }
    fun isConnected(connectivityManager: ConnectivityManager):Boolean{
        return IMPL.isConnected(connectivityManager)
    }
}


interface ConnectedCompat {
    fun isConnected(connectivityManager: ConnectivityManager): Boolean
}

object Marshmallow : ConnectedCompat {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun isConnected(connectivityManager: ConnectivityManager): Boolean {
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) == true
    }
}
object LowerMarshmallow : ConnectedCompat {
    override fun isConnected(connectivityManager: ConnectivityManager): Boolean {
        return connectivityManager.activeNetworkInfo?.isConnected ?: false
    }
}