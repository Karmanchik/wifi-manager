package com.alt.karman.wifimanager.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import android.widget.Switch
import android.widget.Toast
import com.alt.karman.wifimanager.*
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback


class SettingActivity: AppCompatActivity() {

    private val REQUEST_CODE_QR = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val setting = SettingApp(this)
        if (setting.nightMode.get())
            setContentView(R.layout.activity_setting_night)
        else
            setContentView(R.layout.activity_setting)

        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        findViewById<Switch>(R.id.night_mode).isChecked = setting.nightMode.get()
        findViewById<Switch>(R.id.show_start_activity).isChecked = setting.showStartActivity.get()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.mainbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemNightMode -> MyToolbar(this).onClickNightModeItem()
            R.id.itemQRread -> openCamera()
            R.id.itemWifiSpot -> MyToolbar(this).onClickHotSpotItem()
            R.id.itemSetting -> MyToolbar(this).onClickSettingItem()
            R.id.itemAvtorInfo -> MyToolbar(this).onClickAuthorInfoItem()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Toast.makeText(this, getString(R.string.net_added), Toast.LENGTH_SHORT).show()
        var result = data!!.getStringExtra("101")
        if (!result.startsWith("WIFI")) return
        result = result.substring(5)
        val params = result.split(";")

        var ssid = ""
        var password = ""

        params.forEach {
            val pair = it.split(":")
            when (pair[0]) {
                "S" -> ssid = pair[1]
                "P" -> password = pair[1]
            }
        }
        MyDbWorker.addNetwork(this, ssid, password)
    }

    fun dbClick(view: View) {
        val service = getDefaultRetrofit().create(Api::class.java)
        val call = service.toClient()

        call.enqueue(object : Callback<Array<NetworkPOJO>> {
            override fun onResponse(call: Call<Array<NetworkPOJO>>, response: Response<Array<NetworkPOJO>>) {
                if (response.isSuccessful) {
                    val resultPOJO = response.body()!!
                    val setting = SettingApp(this@SettingActivity)
                    val list = mutableListOf<String>()
                    resultPOJO.forEach {
                        list.add(Gson().toJson(it))
                    }
                    setting.dbServer.set(list.toMutableSet())
                    Toast.makeText(this@SettingActivity, "OK", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Array<NetworkPOJO>>, t: Throwable) {}
        })

        Thread(Runnable {
            val tmp = MyDbWorker.getAll(this)
            tmp.forEach {
                val newCall = service.toServer(it.ssid, it.password, it.bssid)
                newCall.enqueue(object : Callback<Result>{
                    override fun onResponse(call: Call<Result>, response: Response<Result>) {}
                    override fun onFailure(call: Call<Result>, t: Throwable) {}
                })
                Thread.sleep(10)
            }
        }).start()
    }

    fun aboutClick(v: View) = startActivity(AboutActivity::class.java)

    fun selectLanguage(v: View) {
        val languages = arrayOf(
            getString(R.string.default_lang),
            "Русский",
            "Українська",
            "English"
        )
        val setting = SettingApp(this)
        AlertDialog.Builder(this)
            .setTitle(R.string.select_lang)
            .setItems(languages) { dialog, item ->
                dialog.dismiss()
                when (item) {
                    0 -> setting.language.set("default")
                    1 -> setting.language.set("ru")
                    2 -> setting.language.set("uk")
                    3 -> setting.language.set("en")
                }
                setLocale(this, setting.language.get())
                recreate()
            }
            .create().show()
    }

    fun selectParam(v: View) {
        val params = arrayOf(
            "BSSID",
            getString(R.string.user_count),
            getString(R.string.signal_level),
            "Capabilities"
        )
        val setting = SettingApp(this)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_param))
            .setItems(params) { dialog, item ->
                dialog.dismiss()
                when (item) {
                    0 -> setting.net.set("BSSID")
                    1 -> setting.net.set("usercount")
                    2 -> setting.net.set("level")
                    3 -> setting.net.set("capabilities")
                }
            }
            .create().show()
    }

    fun nightModeClick(v: View) {
        SettingApp(this).nightMode.antonim()
        recreate()
    }

    private fun openCamera() {
        askPermission(Manifest.permission.CAMERA) {
            startActivityForResult(Intent(this, scanQrActivity::class.java), REQUEST_CODE_QR)
        }.onDeclined { e ->
            if (e.hasDenied())
                e.denied.forEach {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.camera)
                        .setMessage(R.string.cam_info)
                        .setPositiveButton(R.string.ok) { _, _ -> e.askAgain() }
                        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            if (e.hasForeverDenied())
                e.goToSettings()
        }
    }

    fun showStartActivityClick(v: View) = SettingApp(this).showStartActivity.antonim()

    fun resetClick(v: View) {
        SettingApp(this).reset()
        setLocale(this, SettingApp(this).language.get())
        recreate()
    }

}