package com.alt.karman.wifimanager.activity

import android.Manifest
import android.content.*
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.alt.karman.wifimanager.*
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.isupatches.wisefy.WiseFy
import com.journeyapps.barcodescanner.BarcodeEncoder


class ListActivity : AppCompatActivity() {

    val REQUEST_CODE_QR = 101
    lateinit var wifi: WifiManager
    private lateinit var scanReceiver: ScanReceiver
    lateinit var wise: WiseFy
    lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        wifi = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wise = WiseFy.Brains(this).getSmarts()
        listView = findViewById(R.id.list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        layoutGenerate()
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

    override fun onStart() {
        super.onStart()
        if (!SettingApp(this).nightMode.get()) return
            findViewById<Toolbar>(R.id.toolbar).setBackgroundResource(R.color.background_dark)
            findViewById<ConstraintLayout>(R.id.nt_ol).setBackgroundResource(R.color.background_dark)
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

    private fun layoutGenerate() {
        geoLocationOn()
        scanReceiver = ScanReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(scanReceiver, intentFilter)
        wifi.startScan()
    }

    private fun geoLocationOn() {
        val locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!enabled)
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private inner class ScanReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            listView.adapter = NetworkAdapter(
                this@ListActivity,
                R.layout.network_item,
                wifi.scanResults.toTypedArray(),
                wifi,
                wise
            )
            wifi.startScan()
        }
    }
}
