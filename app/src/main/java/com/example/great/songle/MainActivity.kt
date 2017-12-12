package com.example.great.songle

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Math.abs
import java.lang.System.currentTimeMillis
import android.text.InputFilter
import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val tag = "MainActivity"
    private var currentSong = 1
    private var mapVersion = 5
    private var songNumber = 0
    private var currentUser = ""
    private var xmlFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //test permission access
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            val requestCode = 666
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
            println(">>>>> [$tag]onRequestPermissionsResult: $requestCode PERMISSION_REQUESTED")
        }

        //get global variable
        val application = this.application as MyApplication
        currentSong = application.getcurrentSong()
        mapVersion = application.getmapVersion()
        songNumber = application.getsongNumber()
        currentUser = application.getUser()

        //Check local file if internet is not available
        if (songNumber == 0) {
            try {
                this.openFileInput("songList.xml")
                xmlFlag = true
            } catch (e: Exception) {
                Toast.makeText(this, "No song list available", Toast.LENGTH_SHORT).show()
            }
        }
        if (xmlFlag) {
            val songList = XmlParser().parse(this.openFileInput("songList.xml"))
            application.setsongNumber(songList.size)
            songNumber = songList.size
            var counter = 0
            while (counter < songList.size) {
                println(">>>>>[$tag]songList" + counter + ":" + songList[counter])                //print the list after parser
                counter++
            }
        }

        //the start game button
        fab.setOnClickListener {
            if (currentSong != 0) {
                val kmlLocation: String? = if (currentSong in 1..9)
                    "MapV${mapVersion}Song0$currentSong.kml"
                else
                    "MapV${mapVersion}Song$currentSong.kml"
                try {
                    this.openFileInput(kmlLocation)
                    val intent = Intent(this, MapsActivity::class.java)                      //goto map activity
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Load KML failed. Restart the game when Internet is available!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "← Choose a song first!", Toast.LENGTH_SHORT).show()
            }
        }

        //the songlist button for choose a song or a random song
        fab2.setOnClickListener { view ->
            Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
            //add input edittext
            val editSongNumber = EditText(this)
            editSongNumber.inputType = InputType.TYPE_CLASS_NUMBER
            editSongNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
            editSongNumber.gravity = Gravity.CENTER

            //To get song number input
            val chooseSongBox = AlertDialog.Builder(this)
            chooseSongBox.setTitle("Choose a song to start:")
            chooseSongBox.setMessage("We have songs from 1 to $songNumber now")
            chooseSongBox.setView(editSongNumber)
            chooseSongBox.setPositiveButton("Random", { dialog, which ->
                val random: Int = ((abs(Random().nextInt()) + currentTimeMillis()) % 30 + 1).toInt()
                Snackbar.make(view, "Your lucky number is :$random", Snackbar.LENGTH_LONG)
                        .setAction("OK!") {}.show()
                currentSong = random
                textView3.text = "Song selection: $currentSong"
                application.setcurrentSong(currentSong)
            })
            chooseSongBox.setNegativeButton("Select", { dialog, which ->
                val text = editSongNumber.text.toString()
                if (text == "") {
                    Toast.makeText(this, "You really need to input something!", Toast.LENGTH_SHORT).show()
                } else {
                    val input = text.toInt()
                    if (input == 0 || input > songNumber) {
                        Snackbar.make(view, "This is not a valid number", Snackbar.LENGTH_LONG)
                                .setAction("OK!") {}.show()
                    } else {
                        Snackbar.make(view, "Song $input selected! Good luck.", Snackbar.LENGTH_LONG)
                                .setAction("OK!") {}.show()
                        currentSong = input
                        textView3.text = "Song selection: $currentSong"
                        application.setcurrentSong(currentSong)
                    }
                }
            })
            chooseSongBox.show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        //change fonts
        val typeface = Typeface.createFromAsset(assets, "fonts/comicbd.ttf")
        textView2.typeface = typeface
        textView3.typeface = typeface
        textView4.typeface = typeface
        textView5.typeface = typeface
        textView6.typeface = typeface
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> textView2.text = "Good morning!"
            in 12..17 -> textView2.text = "Good afternoon!"
            else -> textView2.text = "Good evening!"
        }
        textView6.text = application.getUser()
        if (currentSong == 0) {
            textView3.text = "↓Select a Song to start!"
        } else {
            textView3.text = "Song selection: $currentSong"
        }
        textView4.text = "There are $songNumber Songs in the list"
        println(">>>>> [$tag]OnCreate")
    }

    // Runtime requests
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            666 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Nice! Your location is now available!", Toast.LENGTH_LONG).show()
                    println(">>>>> [$tag]onRequestPermissionsResult:$requestCode PERMISSION_GRANTED")
                } else {
                    Toast.makeText(this, "Location access denied", Toast.LENGTH_LONG).show()
                    println(">>>>> [$tag]onRequestPermissionsResult:$requestCode PERMISSION_NOT_GRANTED")
                }
            }
        }
        println(">>>>> [$tag]onRequestPermissionsResult")
    }

    //Test whether need to close drawer first
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
        println(">>>>> [$tag]onBackPressed")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        println(">>>>> [$tag]onCreateOptionsMenu")
        return true
    }

    //when the logout button on toolbar is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        println(">>>>> [$tag]onOptionsItemSelected")
        return when (item.itemId) {
            R.id.action_logout -> {
                val logoutBox = AlertDialog.Builder(this)
                logoutBox.setTitle("Logout,$currentUser?")
                logoutBox.setNegativeButton("Cancel", null)
                logoutBox.setPositiveButton("Logout!",{ dialog, which ->
                    saveFile("","currentUser.txt")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    this.finish()
                })
                logoutBox.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
        /*R.id.nav_camera -> {
            // Handle the camera action
        }*/
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        println(">>>>> [$tag]onNavigationItemSelected")
        return true
    }

    private fun saveFile(data: String, filename: String){
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
