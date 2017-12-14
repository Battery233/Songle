package com.example.great.songle

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity() {
    private var link = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient(){}
        link = intent.getStringExtra("Link")
        web_view.loadUrl(link)
    }
}