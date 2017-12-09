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
import java.io.*

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



        val packageInfoManager = packageManager
        try {
            val pm = packageInfoManager.getPackageInfo("com.example.great.songle", 0)           //Show version number at bottom
            versionNumber.text = "Version:"+pm.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            println(">>>>> [$tag]OnCreate: PackageManager.NameNotFoundException")
        }

        // Thread for fetch XML for the list of the song and lyrics and maps
        Thread({
            //val inputXML = DownloadXmlTask(downloadXMLListener).loadXmlFromNetwork("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            //println(">>>>>InputXML"+inputXML)
            //val streamXML:InputStream = ByteArrayInputStream(inputXML.toByteArray())
            try {
                val downloadXMLListener = DownloadCompleteListener()
                val songList = XmlParser().parse(DownloadXmlTask(downloadXMLListener).downloadUrl("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml"))
                var counter = 0
                while (counter < songList.size) {
                    println(">>>>>[$tag]songList" + counter + ":" + songList[counter])                //print the list after parser
                    counter++
                }
                //Download Lyrics&map
                counter = 1

                while (counter < 10) {
                    download("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/0$counter/lyrics.txt", "Lyric0$counter.txt")
                    var map = 1
                    while (map<6) {
                        download("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/0$counter/map$map.kml", "MapV${map}Song0$counter.kml")
                        map++
                    }
                    counter++
                }
                while (counter < songList.size + 1) {
                    download("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/$counter/lyrics.txt", "Lyric$counter.txt")
                    var map = 1
                    while (map<6) {
                        download("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/$counter/map${map}.kml", "MapV${map}Song$counter.kml")
                        map++
                    }
                    counter++
                }
            }
            catch (e:IOException)
            {
                Toast.makeText(this,"Download file failed!", Toast.LENGTH_SHORT).show()
            }
            /*/GET lyric
                try {
                val result = StringBuilder()
                var  fileIn:FileInputStream? = null
                fileIn= this.openFileInput("Lyric27.txt")
                val reader=BufferedReader(InputStreamReader(fileIn))
                var line: String? = null
                while ({ line = reader.readLine(); line }()!=null)
                {
                    result.append(line)
                }
                println("123")
                println("$count "+result.toString())
            }
            catch(e:IOException){
                println("dada")
            }*/

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

    private fun download(urlString: String,fileName:String){
        val input = DownloadXmlTask(DownloadCompleteListener()).loadXmlFromNetwork(urlString)
        saveFile(input,fileName)
        println(">>>>> [$tag]File $fileName Saved!")
    }

    private fun saveFile(data:String,filename:String) //TODO: if no internet
    {
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
