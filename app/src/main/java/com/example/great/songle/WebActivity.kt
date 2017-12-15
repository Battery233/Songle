package com.example.great.songle

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_web.*
import java.io.*

/**
 * The activity to watch MV on YouTube
 */

class WebActivity : AppCompatActivity() {
    private var link = ""
    private var counter = 0
    private val tag = "youTube"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        //To record the YouTube view times for the specific user
        val application = this.application as MyApplication
        val currentUser = application.getUser()
        counter = BufferedReader(InputStreamReader(this.openFileInput("youtube_$currentUser.txt"))).readLine().toInt()
        counter++
        saveFile(counter.toString(), "youtube_$currentUser.txt")

        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {}
        link = intent.getStringExtra("Link")
        web_view.loadUrl(link)
    }

    private fun saveFile(data: String, filename: String) {
        val out: FileOutputStream?
        var writer: BufferedWriter? = null
        try {
            out = this.openFileOutput(filename, Context.MODE_PRIVATE)
            writer = BufferedWriter(OutputStreamWriter(out))
            writer.write(data)
            println(">>>>> [$tag]File writeData")
        } catch (e: IOException) {
            println(">>>>> [$tag]File writeDataError")
            e.printStackTrace()
        } finally {
            try {
                if (writer != null) {
                    writer.close()
                    println(">>>>> [$tag]File writeClose")
                }
            } catch (e: IOException) {
                println(">>>>> [$tag]File writeCloseError")
                e.printStackTrace()
            }
        }
    }
}