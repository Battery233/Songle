package com.example.great.songle

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_playinfo2.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * The activity for show some play stats
 */

class PlayInfo2 : AppCompatActivity() {
    private var currentUser = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playinfo2)

        val application = this.application as MyApplication
        currentUser = application.getUser()

        val hint = BufferedReader(InputStreamReader(this.openFileInput("hint_$currentUser.txt"))).readLine().toInt()
        val guessTimes = BufferedReader(InputStreamReader(this.openFileInput("guess_times_$currentUser.txt"))).readLine().toInt()
        val guessCorrect = BufferedReader(InputStreamReader(this.openFileInput("guess_correct_times_$currentUser.txt"))).readLine().toInt().toDouble()

        val typeface = Typeface.createFromAsset(assets, "fonts/comicbd.ttf")
        user_name2.typeface = typeface
        info_textView2.typeface = typeface
        textView12.typeface = typeface
        textView13.typeface = typeface
        textView14.typeface = typeface
        textView15.typeface = typeface

        user_name2.text = currentUser
        textView12.text = "Guess correct ${guessCorrect.toInt()} times"
        textView13.text = "Total guess $guessTimes times"
        if (guessTimes == 0)
            textView14.text = "Correctness: ???"
        else
            textView14.text = "Correctness: ${(guessCorrect / guessTimes * 100).toInt()} %"
        textView15.text = "Hint pressed $hint times"
    }
}
