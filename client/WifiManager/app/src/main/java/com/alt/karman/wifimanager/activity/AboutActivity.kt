package com.alt.karman.wifimanager.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.ViewGroup
import com.alt.karman.wifimanager.R
import com.alt.karman.wifimanager.SettingApp
import com.vansuita.materialabout.builder.AboutBuilder

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        var cover = R.mipmap.profile_cover
        if (SettingApp(this).nightMode.get()) {
            findViewById<ConstraintLayout>(R.id.aboutLayout)
                    .setBackgroundResource(R.color.background_dark)
            cover = R.drawable.cover
        }

        val view = AboutBuilder.with(this)
                .setPhoto(R.drawable.ava)
                .setCover(cover)
                .setName(R.string.fullname)
                .setSubTitle(R.string.profession)
                .setBrief(R.string.status)
                .setAppIcon(R.drawable.icon)
                .setAppName(R.string.app_name)
                .addEmailLink(R.string.mail)
                .addWebsiteLink(R.string.vk_addr)
                .addFiveStarsAction()
                .addGitHubLink("deusmengnus")
                .setVersionNameAsAppSubTitle()
                .addShareAction(R.string.app_name)
                .setWrapScrollView(true)
                .setLinksAnimated(true)
                .setShowAsCard(true)
                .build()

        addContentView(
                view, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        )
    }
}
