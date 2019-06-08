package com.alt.karman.wifimanager.activity

import android.net.wifi.WifiConfiguration
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.alt.karman.wifimanager.R
import com.alt.karman.wifimanager.SettingApp
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.alt.karman.wifimanager.WifiApManager
import java.lang.StringBuilder


class AccessPointActivity : AppCompatActivity() {

    lateinit var manager: WifiApManager

    //views
    lateinit var statusImg: ImageView
    lateinit var statusText: TextView
    lateinit var ssidView: EditText
    lateinit var password: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_spot)

            if (SettingApp(this).style.get() == "night")
                findViewById<ConstraintLayout>(R.id.nt_l_ap).setBackgroundResource(R.color.background_dark)

        statusImg = findViewById(R.id.status_img)
        statusText = findViewById(R.id.stasus_access_point)

        ssidView = findViewById(R.id.ssid_access_point)
        password = findViewById(R.id.password_access_point)

        manager = WifiApManager(this)
        if (manager.isWifiApEnabled)
            statusText.text = getString(R.string.ap_off)
        else
            statusText.text = getString(R.string.ap_on)

        manager.getClientList(false) { clients ->
            val buffer = StringBuilder("")
            buffer.append(getString(R.string.devices)+": \n")
            for (clientScanResult in clients) {
                buffer.append(getString(R.string.ip) + clientScanResult.ipAddr + "\n")
                buffer.append(getString(R.string.device) + clientScanResult.device + "\n")
                buffer.append(getString(R.string.hw_addr) + clientScanResult.hwAddr + "\n")
                buffer.append(getString(R.string.is_reachable) + clientScanResult.isReachable + "\n")
            }
            statusText.text =
                if (manager.isWifiApEnabled)
                    getString(R.string.ap_on)+"\n\n$buffer"
                else
                    getString(R.string.ap_off)
        }
    }

    fun isValide(): Boolean {
        if (ssidView.text.toString().replace(" ", "") == "") {
            ssidView.error = getString(R.string.ssid_empty_error)
            return false
        }
        if (password.text.toString().length < 8) {
            password.error = getString(R.string.pass_len_error)
            return false
        }
        return true
    }

    fun powerMode(view: View) {
        val manager = WifiApManager(this)
        if (manager.isWifiApEnabled) {
            manager.setWifiApEnabled(null, false)
            statusText.text = getString(R.string.ap_off)
            statusImg.setImageResource(R.drawable.wifi_0_open)
        }
        else
            try {
                if (!isValide()) return
                val conf = manager.wifiApConfiguration
                conf.SSID = ssidView.text.toString()
                conf.preSharedKey = String.format("%s", password.text.toString())
                manager.wifiApConfiguration = conf
                manager.setWifiApEnabled(null, true)
                statusText.text = getString(R.string.ap_on)
                statusImg.setImageResource(R.drawable.wifi_4_open)
            } catch (e: Exception) {
                manager.showWritePermissionSettings(true)
                powerMode(view)
            }
    }

    fun share(view: View) {
        val SSID = ssidView.text.toString()
        val securityType = "wpa"
        val password = password.text.toString()
        val textQR = "WIFI:S:$SSID;T:$securityType;P:$password;H:false;"

        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(textQR, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            val builder = AlertDialog.Builder(this)
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            builder.setView(imageView)
            builder.show()
        } catch (e: Exception) {
        }

    }
}