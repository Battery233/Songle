package com.example.great.songle

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val tag = "MainActivity"
    val songNumber = 0

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

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Clicked!", Snackbar.LENGTH_LONG)
                    .setAction("Close") {Toast.makeText(this, "Yes!",Toast.LENGTH_SHORT).show() }.show()
            val intent = Intent(this, MapsActivity::class.java)                     //goto map activity
            startActivity(intent)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        //change fonts
        val typeface=Typeface.createFromAsset(assets,"fonts/comicbd.ttf")
        textView2.typeface =typeface
        textView3.typeface = typeface
        textView4.typeface = typeface
        textView5.typeface = typeface
        textView4.text = "There are $songNumber Songs in the list"
        println(">>>>> [$tag]OnCreate")
    }

    // Runtime requests
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            666->{
                if(grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Nice! Your location is now available!",Toast.LENGTH_LONG).show()
                    println(">>>>> [$tag]onRequestPermissionsResult:$requestCode PERMISSION_GRANTED")
                }
                else{
                    Toast.makeText(this, "Location access denied",Toast.LENGTH_LONG).show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        println(">>>>> [$tag]onOptionsItemSelected")
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            /*R.id.nav_camera -> {
                // Handle the camera action
            }*/

          /*  R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }*/
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        println(">>>>> [$tag]onNavigationItemSelected")
        return true
    }
}
