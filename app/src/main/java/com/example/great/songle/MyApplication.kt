package com.example.great.songle

import android.app.Application

/**
 * Created by great on 2017/12/11.
 * To provide some global variable by setting
 * <application
android:name=".MyApplication"
android:allowBackup="true"
android:icon="@mipmap/ic_launcher"
android:label="@string/app_name"
android:roundIcon="@mipmap/ic_launcher_round"
android:supportsRtl="true"
android:theme="@style/AppTheme">
 */

class MyApplication : Application() {
    private var currentSong = 0
    private var mapVersion = 5
    private var songNumber = 0
    private var accuracy = 15.0
    private var user = "User"
    private var veryInteresting = true
    private var interesting = false
    private var notBoring = false
    private var boring = false


    fun getcurrentSong(): Int {
        return currentSong
    }

    fun getmapVersion(): Int {
        return mapVersion
    }

    fun getsongNumber(): Int {
        return songNumber
    }

    fun getUser(): String {
        return user
    }

    fun getaccuracy(): Double {
        return accuracy
    }

    fun getVeryInteresting(): Boolean {
        return veryInteresting
    }

    fun getInteresting(): Boolean {
        return interesting
    }

    fun getNotBoring(): Boolean {
        return notBoring
    }

    fun getBoring(): Boolean {
        return boring
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

    fun setUser(i: String) {
        user = i
    }

    fun setVeryInteresting(i: Boolean) {
        veryInteresting = i
    }

    fun setInteresting(i: Boolean) {
        interesting = i
    }

    fun setNotBoring(i: Boolean) {
        notBoring = i
    }

    fun setBoring(i: Boolean) {
        boring = i
    }
}

