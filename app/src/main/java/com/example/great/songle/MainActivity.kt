@file:Suppress("DEPRECATION")

package com.example.great.songle

import android.annotation.SuppressLint
import android.content.ClipboardManager
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
import com.example.great.songle.R.id.drawer_layout
import kotlinx.android.synthetic.main.switch_item_classify.*
import kotlinx.android.synthetic.main.switch_item1.*
import kotlinx.android.synthetic.main.switch_item2.*
import kotlinx.android.synthetic.main.switch_item3.*
import kotlinx.android.synthetic.main.switch_item4.*
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val tag = "MainActivity"
    private var currentSong = 1
    private var mapVersion = 5
    private var songNumber = 0
    private var currentUser = ""
    private var accuracyGps = 15.0
    private var songNameList = ArrayList<String>()
    private var youTubeList = ArrayList<String>()
    private var solvedSongList = ArrayList<Int>()

    @SuppressLint("SetTextI18n")
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
        accuracyGps = application.getaccuracy()
        songNameList.add("songNameList")
        youTubeList.add("youTubeList")
        solvedSongList.add(0)
        //Check local file if internet is not available
        if (songNumber == 0) {
            try {
                this.openFileInput("songList.xml")
            } catch (e: Exception) {
                Toast.makeText(this, "No Local song list available", Toast.LENGTH_SHORT).show()
            }
        }
        try {
            val songList = XmlParser().parse(this.openFileInput("songList.xml"))
            application.setsongNumber(songList.size)
            songNumber = songList.size
            var counter = 0
            while (counter < songList.size) {
                songNameList.add(songList[counter].Title)
                youTubeList.add(songList[counter].Link)
                println(">>>>>[$tag]songList" + counter + ":" + songList[counter] + songNameList[counter])                //print the list after parser
                counter++
            }
        } catch (e: Exception) {
        }

        //the listener on game icon, click and show time
        mainSongleIcon.setOnClickListener {
            val calendar = Calendar.getInstance()
            val min = calendar.get(Calendar.MINUTE)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val date = calendar.get(Calendar.DATE)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            Toast.makeText(this, "It's $hour:$min, $date/$month/$year now!", Toast.LENGTH_LONG).show()
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
                    var isFileExist = false
                    try {
                        this.openFileInput("solved_song_list_$currentUser.txt")
                        isFileExist = true
                    } catch (e: Exception) {
                    }
                    if (isFileExist) {
                        val reader = BufferedReader(InputStreamReader(this.openFileInput("solved_song_list_$currentUser.txt"))).readLine()
                        solvedSongList[0] = reader.split(" ")[0].toInt()
                        var i = 1
                        while (i <= solvedSongList[0]) {
                            solvedSongList.add(reader.split(" ")[i].toInt())
                            i++
                        }
                    }
                    var ifSolved = false
                    var counter = 1
                    while (counter <= solvedSongList[0]) {
                        if (currentSong == solvedSongList[counter]) {
                            ifSolved = true
                            break
                        }
                        counter++
                    }
                    val intent = Intent(this, MapsActivity::class.java)                      //goto map activity
                    intent.putExtra("currentSongTitle", songNameList[currentSong])
                    intent.putExtra("youTubeLink", youTubeList[currentSong])
                    intent.putExtra("ifSolved", ifSolved)
                    if (!ifSolved) {
                        startActivity(intent)
                    } else {
                        val viewWords = AlertDialog.Builder(this)
                        viewWords.setTitle("You have finished this song before!")
                        viewWords.setMessage("Do it again?")
                        viewWords.setPositiveButton("OK!", { _, _ ->
                            startActivity(intent)
                        })
                        viewWords.setNegativeButton("Cancel!", { _, _ ->})
                        viewWords.show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Load KML failed. Restart the game when Internet is available!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "← Choose a song first!", Toast.LENGTH_SHORT).show()
            }
        }

        //the songList button for choose a song or a random song
        fab2.setOnClickListener { view ->
            Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
            //add input editText
            if (songNumber != 0) {
                val editSongNumber = EditText(this)
                editSongNumber.inputType = InputType.TYPE_CLASS_NUMBER
                editSongNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
                editSongNumber.gravity = Gravity.CENTER

                //To get song number input
                val chooseSongBox = AlertDialog.Builder(this)
                chooseSongBox.setTitle("Choose a song to start:")
                chooseSongBox.setMessage("We have songs from 1 to $songNumber now")
                chooseSongBox.setView(editSongNumber)
                chooseSongBox.setPositiveButton("Random", { _, _ ->
                    val random: Int = ((abs(Random().nextInt()) + currentTimeMillis()) % songNumber + 1).toInt()
                    Snackbar.make(view, "Your lucky number is :$random", Snackbar.LENGTH_LONG)
                            .setAction("OK!") {}.show()
                    currentSong = random
                    textView3.text = "Song selection: $currentSong"
                    application.setcurrentSong(currentSong)
                })
                chooseSongBox.setNegativeButton("Select", { _, _ ->
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
            } else {
                val chooseSongBox = AlertDialog.Builder(this)
                chooseSongBox.setTitle("No Internet access!")
                chooseSongBox.setMessage("You need to download files when first start the game.\nRestart the game when internet is available!")
                chooseSongBox.setPositiveButton("Quit!", { _, _ ->
                    this.finish()
                })
                chooseSongBox.show()
            }
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(0).isChecked = true                             //start with the game page selected in the drawer

        //change fonts
        val typeface = Typeface.createFromAsset(assets, "fonts/comicbd.ttf")
        textView2.typeface = typeface
        textView3.typeface = typeface
        textView4.typeface = typeface
        textView5.typeface = typeface
        textView6.typeface = typeface
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> textView2.text = getString(R.string.morning)
            in 12..17 -> textView2.text = getString(R.string.afternoon)
            else -> textView2.text = getString(R.string.evening)
        }
        currentUser = application.getUser()
        textView6.text = currentUser
        if (currentSong == 0) {
            textView3.text = getString(R.string.SelectSong)
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
        wordSwitch.isChecked = true
        wordSwitch1.isChecked = true
        val application = this.application as MyApplication
        //set listen on drawer switches and save the settings
        wordSwitch.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked) {
                mapVersion = 5
                application.setmapVersion(5)
                if (!wordSwitch1.isChecked && !wordSwitch2.isChecked && !wordSwitch3.isChecked && !wordSwitch4.isChecked) {
                    wordSwitch1.isChecked = true
                }
            } else {
                mapVersion = 1
                application.setmapVersion(1)
                //make other switches false
                wordSwitch1.isChecked = false
                wordSwitch2.isChecked = false
                wordSwitch3.isChecked = false
                wordSwitch4.isChecked = false
            }
        })

        wordSwitch1.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked) {
                mapVersion = 5
                application.setmapVersion(5)
                application.setVeryInteresting(true)
                wordSwitch.isChecked = true
            } else {
                application.setVeryInteresting(false)
                if (!wordSwitch2.isChecked && !wordSwitch3.isChecked && !wordSwitch4.isChecked) {        //if all the classify is canceled, set to unclassified mood
                    mapVersion = 1
                    application.setmapVersion(1)
                    wordSwitch.isChecked = false
                }
            }
        })
        wordSwitch2.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked) {
                mapVersion = 5
                application.setmapVersion(5)
                application.setInteresting(true)
                wordSwitch.isChecked = true
            } else {
                application.setInteresting(false)
                if (!wordSwitch1.isChecked && !wordSwitch3.isChecked && !wordSwitch4.isChecked) {
                    mapVersion = 1
                    application.setmapVersion(1)
                    wordSwitch.isChecked = false
                }
            }
        })
        wordSwitch3.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked) {
                mapVersion = 5
                application.setmapVersion(5)
                application.setNotBoring(true)
                wordSwitch.isChecked = true
            } else {
                application.setNotBoring(false)
                if (!wordSwitch1.isChecked && !wordSwitch2.isChecked && !wordSwitch4.isChecked) {
                    mapVersion = 1
                    application.setmapVersion(1)
                    wordSwitch.isChecked = false
                }
            }
        })
        wordSwitch4.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked) {
                mapVersion = 5
                application.setmapVersion(5)
                application.setBoring(true)
                wordSwitch.isChecked = true
            } else {
                application.setBoring(false)
                if (!wordSwitch1.isChecked && !wordSwitch2.isChecked && !wordSwitch3.isChecked) {
                    mapVersion = 1
                    application.setmapVersion(1)
                    wordSwitch.isChecked = false
                }
            }
        })


        println(">>>>> [$tag]onCreateOptionsMenu")
        return true
    }

    //when the logout button  or change password on toolbar is selected
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
                logoutBox.setPositiveButton("Logout!", { _, _ ->
                    saveFile("", "currentUser.txt")
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    this.finish()
                })
                logoutBox.show()
                true
            }

            R.id.action_change_password -> {
                val editPassword = EditText(this)
                editPassword.inputType = InputType.TYPE_CLASS_NUMBER
                editPassword.gravity = Gravity.CENTER
                editPassword.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))

                val changePassword = AlertDialog.Builder(this)
                changePassword.setTitle("Change password,$currentUser?")
                changePassword.setMessage("Enter your new password:")
                changePassword.setView(editPassword)
                changePassword.setNegativeButton("Cancel", null)
                changePassword.setPositiveButton("Commit!", { _, _ ->
                    val text = editPassword.text.toString()
                    if (text == "") {
                        Toast.makeText(this, "Password should not be empty!", Toast.LENGTH_SHORT).show()
                    } else {
                        saveFile(text, "password_$currentUser.txt")
                        Toast.makeText(this, "Password changed!", Toast.LENGTH_SHORT).show()
                    }
                })
                changePassword.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_start -> {
            }
            R.id.nav_share -> {         //Share gitHub link
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = "https://github.com/Battery233"
                Toast.makeText(this, "Link copied in clipboard\nShare with your friends!", Toast.LENGTH_LONG).show()
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Try this fantastic game called Songle. Link here:\nhttps://github.com/Battery233")
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
            R.id.nav_accuracy -> {             //for changing accuracy settings
                val editAccuracy = EditText(this)
                editAccuracy.inputType = InputType.TYPE_CLASS_NUMBER
                editAccuracy.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))
                editAccuracy.gravity = Gravity.CENTER

                //To get accuracy number input
                val chooseSongBox = AlertDialog.Builder(this)
                chooseSongBox.setTitle("Change word collection distance(Default 15m):")
                chooseSongBox.setMessage("Current value is ${accuracyGps.toInt()}m:")
                chooseSongBox.setView(editAccuracy)
                chooseSongBox.setPositiveButton("Submit!", { _, _ ->
                    val text = editAccuracy.text.toString()
                    if (text == "") {
                        Toast.makeText(this, "You really need to input something!", Toast.LENGTH_SHORT).show()
                    } else {
                        val i = text.toDouble()
                        when {
                            i < 5 -> Toast.makeText(this, "Distance should no less than 5m", Toast.LENGTH_SHORT).show()
                            i > 50 -> Toast.makeText(this, "Distance should no more than 50m", Toast.LENGTH_SHORT).show()
                            else -> {
                                accuracyGps = i
                                val application = this.application as MyApplication
                                application.setaccuracy(accuracyGps)
                                Toast.makeText(this, "Current max collection distance: ${accuracyGps.toInt()} m", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })
                chooseSongBox.setNegativeButton("Cancel", null)
                chooseSongBox.show()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        println(">>>>> [$tag]onNavigationItemSelected")
        return true
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
