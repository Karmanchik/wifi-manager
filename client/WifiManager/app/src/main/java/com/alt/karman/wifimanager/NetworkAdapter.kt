package com.alt.karman.wifimanager

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.isupatches.wisefy.WiseFy
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder


class NetworkAdapter(context: Context, var layout: Int, var networks: Array<ScanResult>, val wm: WifiManager, val wifi: WiseFy):
        ArrayAdapter<ScanResult>(context, layout, networks) {

    private fun shareQR(SSID: String) {
        val securityType = "wpa"
        val password = MyDbWorker.getPasswordNetwork(context, SSID)
        val textQR = "WIFI:S:$SSID;T:$securityType;P:$password;H:false;"

        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(textQR, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)

            val builder = AlertDialog.Builder(context)
            val imageView = ImageView(context)
            imageView.setImageBitmap(bitmap)
            builder.setView(imageView)
            builder.show()
        } catch (e: Exception) {}
    }

    private fun connectWithDialog(network: ScanResult) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        val userInput = dialogView.findViewById<EditText>(R.id.passField)
        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { _, _ ->
                    try {
                        wifi.connectToNetwork(network.SSID, 1600)
                        connectWIFI(wm, network.SSID, userInput.text.toString())
                        MyDbWorker.addNetwork(context, network.SSID, userInput.text.toString())
                    } catch (e: Exception) { }
                }
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
                ?: (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                        .inflate(layout, parent, false)

        val network = networks[position]

        view.setOnClickListener {

            var actions = arrayOf(
                context.getString(R.string.connect),
                context.getString(R.string.share),
                context.getString(R.string.show_pass),
                context.getString(R.string.with_pass)
            )
            if (wifi.isDeviceConnectedToSSID(network.SSID))
                actions += context.getString(R.string.deconnect)
            AlertDialog.Builder(context)
                .setTitle(R.string.action)
                .setItems(actions) { dialog, item ->
                    dialog.dismiss()
                    when (item) {
                        0 -> connectWithDialog(network)
                        1 -> shareQR(network.SSID)
                        2 -> Toast.makeText(context, getPassword(context, network), Toast.LENGTH_LONG).show()
                        4 -> wifi.disconnectFromCurrentNetwork()
                        3 -> {
                            connectWIFI(wm, network.SSID, getPassword(context, network))
                        }
                    }
                }
                .create().show()
        }


        val powerView = view.findViewById<ImageView>(R.id.power_status_ImageView)
        if (network.capabilities == "[ESS]") when {
            network.level > -50 -> powerView.setImageResource(R.drawable.wifi_4_open)
            network.level > -83 -> powerView.setImageResource(R.drawable.wifi_3_open)
            network.level > -87 -> powerView.setImageResource(R.drawable.wifi_2_open)
            else -> powerView.setImageResource(R.drawable.wifi_1_open)
        }
        else when {
            network.level > -50 -> powerView.setImageResource(R.drawable.wifi_4_lock)
            network.level > -83 -> powerView.setImageResource(R.drawable.wifi_3_lock)
            network.level > -87 -> powerView.setImageResource(R.drawable.wifi_2_lock)
            else -> powerView.setImageResource(R.drawable.wifi_1_lock)
        }

        view.findViewById<TextView>(R.id.ssid_network_TextView).text = network.SSID

        val bonusParam = SettingApp(context).net.get()
        view.findViewById<TextView>(R.id.param_network_TextView).text = when (bonusParam) {
            "BSSID" -> network.BSSID
            "capabilities" -> network.capabilities
            "level" -> context.getString(R.string.lvl)+network.level
            else -> context.getString(R.string.in_work)
        }

        view.findViewById<ImageView>(R.id.isConnect_ImageView).setImageResource(
                if (wifi.isDeviceConnectedToSSID(network.SSID)) R.drawable.done
                else R.drawable.empty
        )
        view.tag = "${network.SSID}-${network.BSSID}"
        return view
    }
}
