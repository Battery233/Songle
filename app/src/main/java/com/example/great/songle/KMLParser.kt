package com.example.great.songle

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Created by great on 2017/12/10.
 * Parser for kml
 */

class KmlParser {
    data class MapMarkers(val name: String, val description: String, val longitude: Double, val latitude: Double,val height:Double)
    private val ns: String? = null
    private  val tag = "KMLParser"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<MapMarkers> {
        input.use{
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, "utf-8")
            parser.nextTag()
            println(">>>>> [$tag]parser")
            return readDocument(parser)
        }
    }

    /* Following by functions for a specific tag */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDocument(parser: XmlPullParser): List<MapMarkers>{
        val entries = ArrayList<MapMarkers>()
        parser.require(XmlPullParser.START_TAG, ns, "Document")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG){
                println(">>>>> [$tag]readDocument->continue")
                continue
            }
            if (parser.name =="Placemark") {
                println(">>>>> [$tag]entries.add(readSong(parser))")
                entries.add(readPlacemark(parser))
            }else{
                println(">>>>> [$tag]readDocument->skip")
                skip(parser)
            }
        }
        println(""">>>>> [$tag]size of entries${entries.size}""")
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readPlacemark(parser: XmlPullParser): MapMarkers {
        parser.require(XmlPullParser.START_TAG,ns,"Placemark")
        var name = ""
        var description = ""
        var longtitude = 0.0
        var latitude = 0.0
        var point = ""
        while(parser.next() != XmlPullParser.END_TAG){
            println(">>>>> [$tag]readPlacemark->while")
            if(parser.eventType!= XmlPullParser.START_TAG){
                println(">>>>> [$tag]readPlacemark->while->continue")
                continue
            }
            println(">>>>> [$tag]readPlacemark->when")
            when(parser.name){
                "name"->name=readName(parser)
                "description"->description=readDescription(parser)
                "Point"->point=readPoint(parser)
                else->skip(parser)
            }
        }
        longtitude = point.split(",")[0].toDouble()
        latitude = point.split(",")[1].toDouble()
        //parser.require(XmlPullParser.END_TAG,ns,"Song")
        return MapMarkers(name,description,longtitude,latitude,0.0)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readName(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "name")
        val name =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"name")
        println(">>>>> [$tag]readName")
        return name
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readDescription(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "description")
        val description =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"description")
        println(">>>>> [$tag]readDescription")
        return description
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPoint(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "Point")
        val point =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"Point")
        println(">>>>> [$tag]readPoint")
        return point
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser):String{
        var result = ""
        if(parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        println(">>>>> [$tag]readText:$result")
        return result
    }

    @Throws(IOException::class, IOException::class)              //skip unwanted tags
    private fun skip(parser: XmlPullParser){
        if (parser.eventType!= XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth!=0){
            when(parser.next())
            {
                XmlPullParser.END_TAG->depth--
                XmlPullParser.START_TAG->depth++
            }
        }
    }

}