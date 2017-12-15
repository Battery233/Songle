package com.example.great.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_song_list.*

/**
 * This activity shows the song list. If the song is solved, show the song details, if not, describe as locked
 * Can watch YouTube video bby clicking the button at bottom
 */

class SongListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        //get information about song list
        val intent = intent
        val songNameList = intent.getStringArrayListExtra("songNameList")
        val youTubeList = intent.getStringArrayListExtra("youTubeList")
        val solvedSongList = intent.getIntegerArrayListExtra("solvedSongList")
        val artistList = intent.getStringArrayListExtra("artistList")

        //set list content
        val name = ArrayList<String>()
        var counter = 1
        var solved: Boolean
        while (counter < songNameList.size) {
            var counter2 = 1
            solved = false
            while (counter2 <= solvedSongList[0]) {
                println(">>>solved? $counter2 ${solvedSongList[counter2]}")
                if (solvedSongList[counter2] == counter) {
                    solved = true
                    break
                }
                counter2++
            }
            if (solved) {
                name.add("•$counter: ${songNameList[counter]}\n      Artist: ${artistList[counter]}")
            } else {
                name.add("•$counter:   unsolved")
            }
            counter++
        }
        val arr = name.toArray(arrayOfNulls<String>(name.size))
        val adp: Any = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arr
        )
        list_view_song.adapter = adp as ListAdapter?

        // the video button
        fab_video.setOnClickListener {
            val editSongNumber = EditText(this)
            editSongNumber.inputType = InputType.TYPE_CLASS_NUMBER
            editSongNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
            editSongNumber.gravity = Gravity.CENTER
            editSongNumber.hint = "Input song number here:"
            //To get song number input
            val chooseSongBox = AlertDialog.Builder(this)
            chooseSongBox.setTitle("Watch MV on YouTube:")
            chooseSongBox.setMessage("Only unlocked songs are available")
            chooseSongBox.setView(editSongNumber)
            chooseSongBox.setPositiveButton("youTube!", { _, _ ->
                val text = editSongNumber.text.toString()
                if (text == "") {
                    Toast.makeText(this, "You really need to input something!", Toast.LENGTH_SHORT).show()
                } else {
                    var count = 1
                    var test = false
                    while (count < solvedSongList.size) {
                        if (text.toInt() == solvedSongList[count]) {
                            test = true
                            break
                        }
                        count++
                    }
                    if (test) {
                        val intent = Intent(this, WebActivity::class.java)
                        intent.putExtra("Link", youTubeList[text.toInt()])
                        startActivity(intent)
                        this.finish()
                    } else
                        Toast.makeText(this, "You haven't unlock it yet!", Toast.LENGTH_SHORT).show()
                }
            })
            chooseSongBox.setNegativeButton("Cancel", { _, _ -> })
            chooseSongBox.show()
        }
    }
}