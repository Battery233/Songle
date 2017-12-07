package com.example.great.songle

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * Created by great on 2017/11/5.
 *  Phrasing Xml
 */

class XmlParser {
    data class SongInfo(val Number: Int, val Artist: String, val Tittle: String, val Link: String, var Solved: Int)
    private val ns: String? = null
    private  val tag = "XMLParser"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<SongInfo> {
        input.use{
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, null)
            parser.nextTag()
            println(">>>>> [$tag]XmlParser:parser")
            return readSongs(parser)
        }
    }

    /* Following by functions for a specific tag */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSongs(parser: XmlPullParser): List<SongInfo>
    {
        val entries = ArrayList<SongInfo>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            if (parser.name =="Song")
                entries.add(readSong(parser))
            else
                println("Uninterested tags")
        }
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser):SongInfo{
        parser.require(XmlPullParser.START_TAG,ns,"Song")
        var number = 0
        var artist = ""
        var tittle = ""
        var link = ""
        var solved = 0
        while(parser.next() != XmlPullParser.END_TAG){
            if(parser.eventType!= XmlPullParser.END_TAG)
                continue
            when(parser.name)
            {
                "Number"->number=readNumber(parser)
                "Artist"->artist=readArtist(parser)
                "Tittle"->tittle=readTittle(parser)
                "Link"->link=readLink(parser)
                "Solved"->solved = 0
            }
        }
        return SongInfo(number,artist,tittle,link,solved)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readNumber(parser: XmlPullParser):Int{
        parser.require(XmlPullParser.START_TAG, ns, "Number")
        val number =Integer.valueOf(readText(parser))
        parser.require(XmlPullParser.END_TAG,ns,"Number")
        println(">>>>> [$tag]XmlParser:number")
        return number
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readArtist(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "Artist")
        val artist =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"Artist")
        println(">>>>> [$tag]XmlParser:Artist")
        return artist
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTittle(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "Tittle")
        val tittle =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"Tittle")
        println(">>>>> [$tag]XmlParser:Tittle")
        return tittle
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser):String{
        parser.require(XmlPullParser.START_TAG, ns, "Link")
        val link =readText(parser)
        parser.require(XmlPullParser.END_TAG,ns,"Link")
        println(">>>>> [$tag]XmlParser:Link")
        return link
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser):String{
        var result = ""
        if((parser.next()) == XmlPullParser.TEXT) {
            result = parser.text
            parser.next()
        }
        return result
    }

}