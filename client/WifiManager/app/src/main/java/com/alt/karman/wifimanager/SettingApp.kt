package com.alt.karman.wifimanager

import android.content.Context
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.adapters.StringSet
import com.afollestad.rxkprefs.rxkPrefs

class SettingApp(context: Context) {

    private val fileName = "setting"
    private var pref: RxkPrefs

    val language: Pref<String>
    val net: Pref<String>
    val wifiOn: Pref<String>
    val hotspot: Pref<Boolean>
    val dbDate: Pref<String>
    val nightMode: Pref<Boolean>
    val dbServer: Pref<StringSet>
    val showStartActivity: Pref<Boolean>

    init {
        pref = rxkPrefs(context, fileName)
        language = pref.string("language", "ru")
        net = pref.string("net", "bsid")
        wifiOn = pref.string("wifiOn", "null")
        hotspot = pref.boolean("hotspot")
        dbDate = pref.string("dbDate", "null")
        nightMode = pref.boolean("nightMode")
        dbServer = pref.stringSet("db")
        showStartActivity = pref.boolean("showStartActivity", true)
    }

    fun reset() {
        language.delete()
        net.delete()
        wifiOn.delete()
        hotspot.delete()
        dbDate.delete()
        nightMode.delete()
        dbServer.delete()
        showStartActivity.delete()
    }

}

fun Pref<Boolean>.antonim() = set(!get())