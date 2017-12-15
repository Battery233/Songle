package com.example.great.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by great on 2017/11/5.
 * For fetching XML files from www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml
 */

class DownloadCompleteListener {
    fun downloadComplete(result: String) {
        println(">>>>>DownloadCallback" + result)
    }
}

class DownloadXmlTask(private val caller: DownloadCompleteListener) :
        AsyncTask<String, Void, String>() {
    private val tag = "DownloadXmlTask"

    override fun doInBackground(vararg urls: String): String {
        println(">>>>[$tag]DownloadXmlTask: doInBackground")
        return try {
            loadXmlFromNetwork(urls[0])
        } catch (e: IOException) {
            "DownloadCompleteListener Unable to load content. Check your network connection"
        } catch (e: XmlPullParserException) {
            "DownloadCompleteListener Error parsing XML"
        }
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }

    fun loadXmlFromNetwork(urlString: String): String {   //build the file string
        val result = StringBuilder()
        val stream = downloadUrl(urlString)
        val reader = BufferedReader(InputStreamReader(stream))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            result.append(line)
            result.append('\n')
        }
        return result.toString()
    }

    @Throws(IOException::class)
            // Given a string representation of a URL, sets up a connection and gets
            // an input stream.
    fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000 // milliseconds
        conn.connectTimeout = 15000 // milliseconds
        conn.requestMethod = "GET"
        conn.doInput = true
        // Starts the query
        conn.connect()
        println(">>>>> [$tag]GetDownloaded stream")
        return conn.inputStream
    }
}