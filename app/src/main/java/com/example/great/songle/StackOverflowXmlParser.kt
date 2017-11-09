package com.example.great.songle

import android.os.DropBoxManager
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Map

/**
 * Created by great on 2017/11/5.
 *  Phrasing Xml
 */

data class Song(val Number:Int,val Artist:String,val Tittle:String, val Link:URL)