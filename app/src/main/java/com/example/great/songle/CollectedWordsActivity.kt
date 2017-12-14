package com.example.great.songle

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import kotlinx.android.synthetic.main.activity_collected_words.*
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import kotlin.collections.ArrayList


class CollectedWordsActivity : AppCompatActivity() {
    data class WordsGot(val order: Int, val word: String, val line: Int, val column: Int, val type: String)

    private val tag = "CollectedWordsActivity"
    private var collectedWordsIndex = ArrayList<Int>()
    private var totalWords = 0
    private var wordsList = ArrayList<WordsGot>()
    private var listMade = true

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        println(">>>>>[$tag]mOnNavigationItemSelectedListener")
        //Get the info list of markers
        if (listMade) {
            val application = this.application as MyApplication
            val currentSong = application.getcurrentSong()
            val mapVersion = application.getmapVersion()
            val kmlLocation: String? = if (currentSong in 1..9)
                "MapV${mapVersion}Song0$currentSong.kml"
            else
                "MapV${mapVersion}Song$currentSong.kml"
            val fileIn = this.openFileInput(kmlLocation)
            println(">>>>>[$tag]KML stream input: $fileIn")
            val mapMarkers = KmlParser().parse(fileIn)

            var i = 1
            while (i < collectedWordsIndex.size) {
                val line = (mapMarkers!![collectedWordsIndex[i]].name.split(":")[0]).toInt()
                val column = (mapMarkers[collectedWordsIndex[i]].name.split(":")[1]).toInt()
                val wordString = readLyricFile(currentSong, line, column)
                wordsList.add(WordsGot(i, wordString!!, line, column, mapMarkers[collectedWordsIndex[i]].description))
                println(">>>>>[$tag] Word list we made ${wordsList[i - 1]}")
                i++
            }
            listMade = false
            println(">>>>>[$tag] Word list size ${wordsList.size}")
        }

        when (item.itemId) {
            R.id.navigation_home -> {
                val name = ArrayList<String>()
                var i = 0
                name.add("***In this list, words are shown\n     at the order of time you collected it:")
                while (i < totalWords) {
                    name.add("    ${wordsList[i].word} \n      The No. ${wordsList[i].order} word you collected, ${wordsList[i].type}\n      in line ${wordsList[i].line}, column ${wordsList[i].column}")
                    i++
                }
                val arr = name.toArray(arrayOfNulls<String>(name.size))
                val adp: Any = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        arr
                )
                list_view.adapter = adp as ListAdapter?
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_achievements -> {
                val name = ArrayList<String>()
                if (wordsList[0].type == "•unclassified") {
                    name.add("In this list, all the words are unclassified:")
                    var i = 0
                    while (i < totalWords) {
                        name.add("    ${wordsList[i].word}\n      The No. ${wordsList[i].order} word you collected\n      in line ${wordsList[i].line}, column ${wordsList[i].column}")
                        i++
                    }
                } else {
                    name.add("***In this list, words are grouped by the word type:")
                    name.add("•Very interesting words:")
                    var i = 0
                    while (i < totalWords) {
                        if (wordsList[i].type == "veryinteresting") {
                            name.add("    ${wordsList[i].word}\n      The No. ${wordsList[i].order} word you collected\n     in line ${wordsList[i].line}, column ${wordsList[i].column}")
                        }
                        i++
                    }
                    name.add("•Interesting words:")
                    i = 0
                    while (i < totalWords) {
                        if (wordsList[i].type == "interesting") {
                            name.add("    ${wordsList[i].word}\n      The No. ${wordsList[i].order} word you collected\n      in line ${wordsList[i].line}, column ${wordsList[i].column}")
                        }
                        i++
                    }
                    name.add("•Not boring words:")
                    i = 0
                    while (i < totalWords) {
                        if (wordsList[i].type == "notboring") {
                            name.add("    ${wordsList[i].word}\n      The No. ${wordsList[i].order} word you collected\n      in line ${wordsList[i].line}, column ${wordsList[i].column}")
                        }
                        i++
                    }
                    name.add("•Boring words:")
                    i = 0
                    while (i < totalWords) {
                        if (wordsList[i].type == "boring") {
                            name.add("    ${wordsList[i].word}\n      collected time order:${wordsList[i].order}\n      in line ${wordsList[i].line}, column ${wordsList[i].column}")
                        }
                        i++
                    }
                }
                val arr = name.toArray(arrayOfNulls<String>(name.size))
                val adp: Any = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        arr
                )
                list_view.adapter = adp as ListAdapter?
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                println(">>>>>[$tag]mOnNavigationItemSelectedListener->2")
                val name = ArrayList<String>()
                var counter1 = 0
                var counter2: Int
                var minLine = 0
                var minColumn = 0
                var index = 0
                name.add("***In this list, words are listed by the order in song:")
                while (counter1 < totalWords) {
                    counter2 = 0
                    var maxLine = 99999
                    var maxColumn = 99999
                    while (counter2 < totalWords) {
                        index = if (wordsList[counter2].line in (minLine + 1)..(maxLine - 1)) {
                            counter2
                        } else if (wordsList[counter2].line == minLine) {
                            if (maxLine == minLine && wordsList[counter2].column > minColumn && wordsList[counter2].column < maxColumn) {
                                counter2
                            } else if (maxLine != minLine && wordsList[counter2].column > minColumn) {
                                counter2
                            } else {
                                counter2++
                                continue
                            }
                        } else if (wordsList[counter2].line == maxLine) {
                            if (maxLine == minLine && wordsList[counter2].column > minColumn && wordsList[counter2].column < maxColumn) {
                                counter2
                            } else if (maxLine != minLine && wordsList[counter2].column < maxColumn) {
                                counter2
                            } else {
                                counter2++
                                continue
                            }
                        } else {
                            counter2++
                            continue
                        }
                        println(">>>>>[$tag] in the while $counter1 $counter2 $maxLine $minLine ${wordsList[counter2].line} ${wordsList[counter2].column}")
                        maxLine = wordsList[counter2].line
                        maxColumn = wordsList[counter2].column
                        counter2++
                    }
                    minLine = wordsList[index].line
                    minColumn = wordsList[index].column
                    counter1++
                    name.add("    ${wordsList[index].word}\n      Word in line ${wordsList[index].line}, column ${wordsList[index].column}, ${wordsList[index].type}\n      The No. ${wordsList[index].order} word you collected")
                }
                val arr = name.toArray(arrayOfNulls<String>(name.size))
                val adp: Any = ArrayAdapter<String>(
                        this,
                        android.R.layout.simple_list_item_1,
                        arr
                )
                list_view.adapter = adp as ListAdapter?
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collected_words)

        //Get the list of word index
        val intent = intent
        collectedWordsIndex = intent.getIntegerArrayListExtra("collectedWords")
        println(">>>>>[$tag] total ${collectedWordsIndex[0]}, for ${collectedWordsIndex.size - 1} words")
        totalWords = collectedWordsIndex[0]
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onStart() {
        super.onStart()
        //Select the default type to show words
        val view: View = findViewById(R.id.navigation_home)
        view.performClick()
    }

    private fun readLyricFile(currentSong: Int, line: Int, column: Int): String? {
        val address = if (currentSong in 1..9)
            "Lyric0$currentSong.txt"
        else
            "Lyric$currentSong.txt"
        var text: String? = ""
        try {
            val fileIn: FileInputStream? = this.openFileInput(address)
            val reader = BufferedReader(InputStreamReader(fileIn))
            var i = 0
            while (i < line) {
                text = reader.readLine()
                i++
            }
        } catch (e: Exception) {
            println(">>>>> Failed to read specific word in file")
        }
        return text!!.split(" ")[column - 1]
    }
}
