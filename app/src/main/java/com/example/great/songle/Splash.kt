package com.example.great.songle

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_splash.*


class Splash : AppCompatActivity() {
    private  val tag = "Splash"
    @SuppressLint("SetTextI18n")
    private inner class NetworkReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent){
            val connMgr =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)
                            as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            when {
                networkInfo?.type == ConnectivityManager.TYPE_WIFI -> {                       //give toast about internet type
                    Toast.makeText(this@Splash,"Connect via WIFI!", Toast.LENGTH_SHORT).show()
                    println(">>>>> [$tag]OnCreate: NetworkReceiver WIFI")
                }
                networkInfo != null -> {
                    Toast.makeText(this@Splash,"Connect via 4G Data!", Toast.LENGTH_SHORT).show()
                    println(">>>>> [$tag]OnCreate: NetworkReceiver DATA")
                }
                else -> {
                    Toast.makeText(this@Splash,"No Internet access!", Toast.LENGTH_SHORT).show()
                    println(">>>>> [$tag]OnCreate: NetworkReceiver No internet")
                }
            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        println(">>>>> [$tag]OnCreate: ActionbarHide")

        //Setup NetworkReceiver to monitor network change
        val networkReceiver = NetworkReceiver()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        this.registerReceiver(networkReceiver, filter)                 //registerReceiver



        val packagemanager = packageManager
        try {
            val pm = packagemanager.getPackageInfo("com.example.great.songle", 0)           //Show version number at bottom
            versionNumber.text = "Version:"+pm.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            println(">>>>> [$tag]OnCreate: PackageManager.NameNotFoundException")
        }

        // Thread for fetch XML for the list of the song
        Thread({
            val downloadXMLListener = DownloadCompleteListener()
            //val inputXML = DownloadXmlTask(downloadXMLListener).loadXmlFromNetwork("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            //println(">>>>>InputXML"+inputXML)
            //val streamXML:InputStream = ByteArrayInputStream(inputXML.toByteArray())
            val songList = XmlParser().parse(DownloadXmlTask(downloadXMLListener).downloadUrl("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml"))
            var counter = 0
            while(counter<songList.size) {
                println(">>>>>[$tag]songList"+counter+":" + songList[counter])                //print the list after parser
                counter++
            }
        }).start()

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@Splash,MainActivity::class.java)               //Delay 3seconds at splash
            startActivity(intent)
            this@Splash.finish()
        },3000)

    }

    /*override fun onStop() {
        unregisterReceiver(NetworkReceiver())
        super.onStop()
    }*/
}
