package com.alt.karman.wifimanager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import com.alt.karman.wifimanager.activity.AboutActivity
import com.alt.karman.wifimanager.activity.SettingActivity
import com.alt.karman.wifimanager.activity.AccessPointActivity

class MyToolbar(var context: Context) {

    fun onClickNightModeItem() {
        val setting = SettingApp(context)
        setting.nightMode.set(!setting.nightMode.get())
    }

    fun onClickScanItem(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun onClickHotSpotItem() = context.startActivity(AccessPointActivity::class.java)

    fun onClickSettingItem() {
        if (context.javaClass != SettingActivity::class.java)
            context.startActivity(SettingActivity::class.java)
    }

    fun onClickStatItem() {}//= Toasty.error(context, "Функция недоступна!").show()

    fun onClickAuthorInfoItem() = context.startActivity(AboutActivity::class.java)

}
