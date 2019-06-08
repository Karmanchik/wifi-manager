package com.alt.karman.wifimanager

import com.alt.karman.wifimanager.activity.NetworkPOJO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET("misha.php?action=load")
    fun toServer(@Query("ssid") ssid: String, @Query("password") password: String,
                 @Query("bssid") bssid: String): Call<Result>

    @GET("misha.php?action=download")
    fun toClient(): Call<Array<NetworkPOJO>>

}