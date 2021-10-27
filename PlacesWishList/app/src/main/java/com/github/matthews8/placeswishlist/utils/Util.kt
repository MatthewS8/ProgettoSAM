package com.github.matthews8.placeswishlist.utils

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresPermission
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import java.nio.channels.SocketChannel

object myColors {

    val colorArray: ArrayList<Float> = arrayListOf(
        BitmapDescriptorFactory.HUE_GREEN,
        BitmapDescriptorFactory.HUE_YELLOW,
        BitmapDescriptorFactory.HUE_ORANGE,
        BitmapDescriptorFactory.HUE_RED,
        BitmapDescriptorFactory.HUE_MAGENTA,
        BitmapDescriptorFactory.HUE_ROSE,
        BitmapDescriptorFactory.HUE_VIOLET,
        BitmapDescriptorFactory.HUE_BLUE,
        BitmapDescriptorFactory.HUE_AZURE,
        BitmapDescriptorFactory.HUE_CYAN

    )

    fun markerColor(color: Float): Int {
        return when(color) {
            BitmapDescriptorFactory.HUE_YELLOW -> Color.YELLOW
            BitmapDescriptorFactory.HUE_VIOLET -> Color.parseColor("#9035EA")
            BitmapDescriptorFactory.HUE_GREEN -> Color.GREEN
            BitmapDescriptorFactory.HUE_AZURE -> Color.parseColor("#3590EA")
            BitmapDescriptorFactory.HUE_BLUE -> Color.BLUE
            BitmapDescriptorFactory.HUE_CYAN -> Color.CYAN
            BitmapDescriptorFactory.HUE_MAGENTA -> Color.MAGENTA
            BitmapDescriptorFactory.HUE_ORANGE -> Color.parseColor("#EA9035")
            BitmapDescriptorFactory.HUE_ROSE -> Color.parseColor("#EA3590")
            else -> Color.RED
        }
    }

    fun colorMarker(color: Float): String {
        return when(color) {
            BitmapDescriptorFactory.HUE_YELLOW -> "YELLOW"
            BitmapDescriptorFactory.HUE_VIOLET -> "VIOLET"
            BitmapDescriptorFactory.HUE_GREEN -> "GREEN"
            BitmapDescriptorFactory.HUE_AZURE -> "AZURE"
            BitmapDescriptorFactory.HUE_BLUE -> "BLUE"
            BitmapDescriptorFactory.HUE_CYAN -> "CYAN"
            BitmapDescriptorFactory.HUE_MAGENTA -> "MAGENTA"
            BitmapDescriptorFactory.HUE_ORANGE -> "ORANGE"
            BitmapDescriptorFactory.HUE_ROSE -> "ROSE"
            else -> "RED"
        }

    }

}

@RequiresPermission("android.permission.ACCESS_NETWORK_STATE")
fun isOnline(context: Context): Boolean {
    val connManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connManager.activeNetwork ?: return false
    val networkCapabilities = connManager.getNetworkCapabilities(network) ?: return false
    return when {
        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }

}
