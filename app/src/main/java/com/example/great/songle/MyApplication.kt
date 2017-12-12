package com.example.great.songle

import android.app.Application

/**
 * Created by great on 2017/12/11.
 * To provide some global variable
 */

class MyApplication : Application() {
    private var currentSong = 0
    private var mapVersion = 1
    private var songNumber = 0
    private var accuracy = 20.0
    private var user = "User"


    fun getcurrentSong(): Int {
        return currentSong
    }

    fun getmapVersion(): Int {
        return mapVersion
    }

    fun getsongNumber(): Int {
        return songNumber
    }

    fun getUser():String{
        return user
    }

    fun getaccuracy(): Double {
        return accuracy
    }

    fun setcurrentSong(i: Int) {
        currentSong = i
    }

    fun setmapVersion(i: Int) {
        mapVersion = i
    }

    fun setsongNumber(i: Int) {
        songNumber = i
    }

    fun setaccuracy(i: Double) {
        accuracy = i
    }

    fun setUser(i:String){
        user = i
    }
}