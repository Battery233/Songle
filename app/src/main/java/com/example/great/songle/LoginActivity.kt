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


class LoginActivity : AppCompatActivity() {
    private var username: String = ""
    private var password: String = ""
    private val tag = "Login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Test if the Screen resolution reached the requirement of at least 1280 Pixels in height
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

        val application = this.application as MyApplication
        //press login button
        sign_in_button.setOnClickListener {
            username = user.text.toString()
            password = passwordIn.text.toString()
            if (username != "" && password != "") {
                println(">>>>>[$tag] username = $username, password = $password")
                try {
                    val reader = BufferedReader(InputStreamReader(this.openFileInput("password_$username.txt"))).readLine()
                    if (reader == password) {
                        application.setUser(username)
                        Toast.makeText(this, "Welcome back, $username!", Toast.LENGTH_LONG).show()
                        saveFile(username, "currentUser.txt")                              //to "remember" the user
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
                    saveFile(password, "password_$username.txt")
                    saveFile(username, "currentUser.txt")                                   //to "remember" the user
                    application.setUser(username)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    println(">>>>>[$tag]finish process no1")
                    this.finish()
                }
            } else {
                Toast.makeText(this, "You need a name and password!", Toast.LENGTH_LONG).show()
            }
        }
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
