package com.example.great.songle

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL

/**
 * Created by great on 2017/11/5.
 *  Phrasing Xml
 */

class StackOverflowXmlParser {
    data class SongInfo(val Number: Int, val Artist: String, val Tittle: String, val Link: URL, var Solved: Int)
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
            println(">>>>> [$tag]StackOverflowXmlParser:parser")
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
                skip(parser)
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
            if(parser.eventType!=XmlPullParser.END_TAG)
                continue
            when(parser.name)
            {
                "number"->number=readNumber(parser)
                "Artist"->artist=readArtist(parser)
                "Tittle"->tittle=readTittle(parser)
                "Link"->link=readLink(parser)
                "Solved"->solved = 0

            }
        }
    }
}