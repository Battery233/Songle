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
    data class SongInfo(val Number: Int, val Artist: String, val Title: String, val Link: String, var Solved: Int)

    private val ns: String? = null
    private val tag = "XMLParser"

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input: InputStream): List<SongInfo> {
        input.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                    false)
            parser.setInput(input, null)
            parser.nextTag()
            println(">>>>> [$tag]parser")
            return readSongs(parser)
        }
    }

    /* Following by functions for a specific tag */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSongs(parser: XmlPullParser): List<SongInfo> {
        val entries = ArrayList<SongInfo>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                println(">>>>> [$tag]readSongs->continue")
                continue
            }
            if (parser.name == "Song") {
                println(">>>>> [$tag]entries.add(readSong(parser))")
                entries.add(readSong(parser))
            } else {
                println(">>>>> [$tag]readSongs->skip")
                skip(parser)
            }
        }
        println(""">>>>> [$tag]size of entries${entries.size}""")
        return entries
    }

    @Throws(XmlPullParserException::class, IOException::class)            //read every <Song> tag
    private fun readSong(parser: XmlPullParser): SongInfo {
        parser.require(XmlPullParser.START_TAG, ns, "Song")
        var number = 0
        var artist = ""
        var title = ""
        var link = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            println(">>>>> [$tag]readSong->while")
            if (parser.eventType != XmlPullParser.START_TAG) {
                println(">>>>> [$tag]readSong->while->continue")
                continue
            }
            println(">>>>> [$tag]readSong->when")
            when (parser.name) {
                "Number" -> number = readNumber(parser)
                "Artist" -> artist = readArtist(parser)
                "Title" -> title = readTitle(parser)
                "Link" -> link = readLink(parser)
                else -> skip(parser)
            }
        }
        return SongInfo(number, artist, title, link, 0)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readNumber(parser: XmlPullParser): Int {
        parser.require(XmlPullParser.START_TAG, ns, "Number")
        val number = Integer.valueOf(readText(parser))
        parser.require(XmlPullParser.END_TAG, ns, "Number")
        println(">>>>> [$tag]readNumber")
        return number
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readArtist(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Artist")
        val artist = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Artist")
        println(">>>>> [$tag]readArtist")
        return artist
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTitle(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Title")
        val title = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Title")
        println(">>>>> [$tag]readTitle,title")
        return title
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readLink(parser: XmlPullParser): String {
        parser.require(XmlPullParser.START_TAG, ns, "Link")
        val link = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, "Link")
        println(">>>>> [$tag]readLink,link")
        return link
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        println(">>>>> [$tag]readText:$result")
        return result
    }

    @Throws(IOException::class, IOException::class)              //skip unwanted tags
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}