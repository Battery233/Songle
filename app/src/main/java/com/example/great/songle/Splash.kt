package com.example.great.songle

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import kotlinx.android.synthetic.main.activity_splash.*


class Splash : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        
        val packagemanager = packageManager
        try {
            val pm = packagemanager.getPackageInfo("com.example.great.songle", 0)           //Show version number at bottom
            versionNumber.text = "Version:"+pm.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@Splash,MainActivity::class.java)               //Delay 3seconds at splash
            startActivity(intent)
            this@Splash.finish()
        },3000)

    }
}
