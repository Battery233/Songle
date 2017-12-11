package com.example.great.songle

import android.app.Application

/**
 * Created by great on 2017/12/11.
 * To provide some global variable
 */

class MyApplication : Application() {
    var currentSong = 8
    var mapVersion = 1
    var songNumber = 0
    //var mapplaces = 0

    fun getcurrentSong(): Int {
        return currentSong
    }

    fun getmapVersion(): Int {
        return mapVersion
    }

    fun getsongNumber(): Int {
        return songNumber
    }

    /*fun getmapPlaces(): Int {
        return mapplaces
    }*/

    fun setcurrentSong(i: Int) {
        currentSong = i
    }

    fun setmapVersion(i: Int) {
        mapVersion = i
    }

    fun setsongNumber(i: Int) {
        songNumber = i
    }

    /*fun setmapPlaces(i:Int){
        mapplaces = i
    }*/
}