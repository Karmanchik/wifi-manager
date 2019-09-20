package com.alt.karman.wifimanager.activity

import android.Manifest
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.alt.karman.wifimanager.*
import com.github.florent37.runtimepermission.kotlin.askPermission

class StartActivity: AppCompatActivity() {

    lateinit var prodText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        MyDbWorker.initDB(this)
        setLocale(this, SettingApp(this).language.get())
        prodText = findViewById(R.id.mainText)

        val animProd = AnimationUtils.loadAnimation(this, R.anim.productanim)
        animProd.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                askPerm()
            }
            override fun onAnimationRepeat(animation: Animation) {}
        })
        if (SettingApp(this).showStartActivity.get())
            askPerm()
        else
            prodText.startAnimation(animProd)
    }

    override fun onStart() {
        super.onStart()
        if (SettingApp(this).nightMode.get())
            findViewById<ConstraintLayout>(R.id.nt_l_StartActivity).setBackgroundResource(R.color.background_dark)
    }

    override fun onRestart() {
        super.onRestart()
        finish()
    }

    private fun askPerm() {
        askPermission(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WAKE_LOCK) {
            startActivity(ListActivity::class.java)
        }.onDeclined { e ->
            if (e.hasDenied())
                e.denied.forEach {
                    e.askAgain()
                }
            if (e.hasForeverDenied())
                e.goToSettings()
        }
    }
}