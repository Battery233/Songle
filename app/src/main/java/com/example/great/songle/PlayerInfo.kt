package com.example.great.songle

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_player_info.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * The activity for show some play stats
 */

class PlayerInfo : AppCompatActivity() {
    private var currentUser = ""
    private var songSolved = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_info)

        val application = this.application as MyApplication
        currentUser = application.getUser()
        val time = BufferedReader(InputStreamReader(this.openFileInput("Login_times_$currentUser.txt"))).readLine().toInt()
        val youtube = BufferedReader(InputStreamReader(this.openFileInput("youtube_$currentUser.txt"))).readLine().toInt()
        val map = BufferedReader(InputStreamReader(this.openFileInput("map_opened_$currentUser.txt"))).readLine().toInt()

        val intent = intent
        songSolved = intent.getIntExtra("solvedSong", 0)

        val typeface = Typeface.createFromAsset(assets, "fonts/comicbd.ttf")
        user_name.typeface = typeface
        info_textView.typeface = typeface
        textView8.typeface = typeface
        textView9.typeface = typeface
        textView10.typeface = typeface
        textView11.typeface = typeface

        user_name.text = currentUser
        textView8.text = "Total song solved: $songSolved"
        textView9.text = "Total times logged in: $time"
        textView10.text = "YouTube watched: $youtube"
        textView11.text = "Map opened $map times"
    }
}
