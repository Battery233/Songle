package com.example.great.songle

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import java.io.*
import android.util.DisplayMetrics

/**
 * Activity for login or register
 * When the splash activity found the login status is false or user logged out from main activity, the login activity will start
 * When the user has registered before, the activity will check if the password is correct,
 * if not, the user will be registered as a new play and some files for recording user info will be created
 */

class LoginActivity : AppCompatActivity() {
    private var username: String = ""
    private var password: String = ""
    private val tag = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Test if the Screen resolution reached the requirement of at least 1280 Pixels in height, the game require at least 1280
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        println(">>>>>[$tag] heightPixels = ${metrics.heightPixels}")
        if (metrics.heightPixels < 1280) {
            val handler = Handler()
            val chooseSongBox = AlertDialog.Builder(this)
            chooseSongBox.setTitle("Sorry you can't run Songle on this device!")
            chooseSongBox.setMessage("You need a device with at least 1280 heightPixels to play Songle\nGame will quit on 5 seconds")
            chooseSongBox.show()
            handler.postDelayed({
                this.finish()
            }, 5000)
        }

        val application = this.application as MyApplication //get application object for global variables
        //press login button
        sign_in_button.setOnClickListener {
            username = user.text.toString()
            password = passwordIn.text.toString()
            if (username != "" && password != "") {
                println(">>>>>[$tag] username = $username, password = $password")
                try {//see if thr user has exist
                    val reader = BufferedReader(InputStreamReader(this.openFileInput("password_$username.txt"))).readLine()
                    if (reader == password) {
                        application.setUser(username)
                        var time = BufferedReader(InputStreamReader(this.openFileInput("Login_times_$username.txt"))).readLine().toInt()
                        time++
                        saveFile(time.toString(), "Login_times_$username.txt")
                        saveFile(username, "currentUser.txt")                              //to "remember" the user
                        Toast.makeText(this, "Welcome back, $username!", Toast.LENGTH_LONG).show()
                        //login successful, go to main
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        println(">>>>>[$tag]finish process no1")
                        this.finish()
                    } else {
                        Toast.makeText(this, "Wrong password!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // if user not exist, add a new user
                    Toast.makeText(this, "New user added! User name: $username", Toast.LENGTH_LONG).show()
                    //here are some files for user info recording
                    saveFile(password, "password_$username.txt")
                    saveFile(username, "currentUser.txt")
                    saveFile("0", "solved_song_list_$username.txt")
                    saveFile("1", "Login_times_$username.txt")
                    saveFile("0", "youtube_$username.txt")
                    saveFile("0", "hint_$username.txt")
                    saveFile("0", "map_opened_$username.txt")
                    saveFile("0", "guess_times_$username.txt")
                    saveFile("0", "guess_correct_times_$username.txt")
                    application.setUser(username)
                    //register successful, go to main
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    println(">>>>>[$tag]finish process no1")
                    this.finish()
                }
            } else {//username password is empty
                Toast.makeText(this, "You need a name and password!", Toast.LENGTH_LONG).show()
            }
        }

        loginWithFacebookButton.setOnClickListener {
            Toast.makeText(this, "Can't login with Facebook now\nDeveloper key invalid in current version!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveFile(data: String, filename: String) {// save file. Parameter 1 as the file content, Parameter 2 is the file name
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
