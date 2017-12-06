package com.example.great.songle

import android.content.res.Resources
import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by great on 2017/11/5.
*For fetching XML files
 */

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}

class DownloadXmlTask(private val resources : Resources,
                      private val caller : DownloadCompleteListener,
                      private val summaryPref : Boolean) :
        AsyncTask<String, Void, String>(){
    private  val tag = "DownloadXmlTask"

    override fun doInBackground(vararg urls: String): String {
        println(">>>>[$tag]DownloadXmlTask: doInBackground")
        return try {
            loadXmlFromNetwork(urls[0])
        } catch (e: IOException)
        {
            "DownloadCompleteListener Unable to load content. Check your network connection"
        }catch (e: XmlPullParserException){
                "DownloadCompleteListener Error parsing XML"
        }
    }
    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }

    private fun loadXmlFromNetwork(urlString: String): String {
        val result = StringBuilder()
        val stream = downloadUrl(urlString)
        println(">>>>> [$tag]GetDownloaded stream")
        val reader=BufferedReader(InputStreamReader(stream))
        var line: String? = null
        while ({ line = reader.readLine(); line }()!=null)
        {
            result.append(line)
        }

        return  result.toString()
    }

    @Throws(IOException::class)
    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        // Starts the query
        conn.connect()
        return conn.inputStream
    }
}