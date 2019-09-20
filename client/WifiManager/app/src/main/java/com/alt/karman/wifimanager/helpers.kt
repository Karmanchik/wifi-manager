package com.alt.karman.wifimanager

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.support.v7.app.AlertDialog
import android.widget.ImageView
import android.widget.Toast
import com.alt.karman.wifimanager.activity.NetworkPOJO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

fun setLocale(context: Context, lang: String) {
    context.resources.configuration.locale = Locale(lang)
    context.resources.updateConfiguration(
        context.resources.configuration,
        context.resources.displayMetrics
    )
}

fun Context.startActivity(activity_name: Class<*>) {
    startActivity(Intent(this, activity_name))
}

fun connectWIFI(wifi: WifiManager, ssid: String, password: String) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", ssid)
        wifiConfig.preSharedKey = String.format("\"%s\"", password)
        val netId = wifi.addNetwork(wifiConfig)

        wifi.disconnect()
        wifi.enableNetwork(netId, true)
        wifi.reconnect()
}

const val BASE_URL = "http://vtk.kl.com.ua"

fun getDefaultRetrofit(): Retrofit {
    val gson = GsonBuilder()
        .setLenient()
        .create()

    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

fun getPassword(context: Context, network: ScanResult): String {
    var pass = MyDbWorker.getPasswordNetwork(context, network.SSID)
    if (pass == "") {
        val dbSet = SettingApp(context).dbServer.get()
        var list = mutableListOf<NetworkPOJO>()
        dbSet.forEach {
            list.add(Gson().fromJson(it, NetworkPOJO::class.java))
        }
        list = list.filter { it.ssid == network.SSID }.toMutableList()
        if (list.size > 0)
            pass = list.first().password
    }
    return pass
}

fun generateQR(SSID: String, security: String, password: String): Bitmap {
    val textQR = "WIFI:S:$SSID;T:$security;P:$password;H:false;"

    val multiFormatWriter = MultiFormatWriter()
    return try {
        val bitMatrix = multiFormatWriter.encode(textQR, BarcodeFormat.QR_CODE, 500, 500)
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.createBitmap(bitMatrix)
    } catch (e: Exception) {
        Bitmap.createBitmap(0,0, null)
    }
}