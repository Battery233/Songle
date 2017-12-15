package com.example.great.songle

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val permissionsRequestAccessFineLocation = 1
    private var mLastLocation: Location? = null             // getLastLocation can return null
    private val tag = "MapsActivity"
    //Get the song list information here
    private var currentSong = 1                            //Flag for the song chosen in the list
    private var mapVersion = 1                             //map version
    private var accuracy = 15.0
    private val maxNumber = 200
    private val totalWordTypes = 4
    private var placeMarkerNumber = 0
    private var line = 0
    private var column = 0
    private var totalPointsMax = totalWordTypes * (maxNumber + 1)
    private val placeMarker = arrayOfNulls<Marker>(totalPointsMax)
    private var veryInteresting = true
    private var interesting = false
    private var notBoring = false
    private var boring = false
    private val noWhere = LatLng(89.654, 123.456)
    private var wordsAddedVisible = 0
    private var currentSongInfo: XmlParser.SongInfo? = null
    private var hintTime = 0
    private var currentUser = ""
    private var currentSongTitle = ""
    private var youTubeLink = ""
    private var ifSolved = false
    private var hinHistory = 0
    private var mapOpened = 0
    private var guess_time = 0
    private var guess_correct_time = 0
    private var solvedSongList = ArrayList<Int>()
    private var collectedWordsIndex = ArrayList<Int>()      //collectedWordsIndex[0] is the amount of the word collected. [1] to [n] are the indexed of collected words

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get global variable
        val application = this.application as MyApplication
        currentSong = application.getcurrentSong()
        mapVersion = application.getmapVersion()
        accuracy = application.getaccuracy()
        veryInteresting = application.getVeryInteresting()
        interesting = application.getInteresting()
        notBoring = application.getNotBoring()
        boring = application.getBoring()
        currentUser = application.getUser()
        currentSongInfo = XmlParser().parse(this.openFileInput("songList.xml"))[currentSong - 1]
        collectedWordsIndex.add(0)
        solvedSongList.add(0)
        hinHistory = BufferedReader(InputStreamReader(this.openFileInput("hint_$currentUser.txt"))).readLine().toInt()
        mapOpened = BufferedReader(InputStreamReader(this.openFileInput("map_opened_$currentUser.txt"))).readLine().toInt()
        guess_time = BufferedReader(InputStreamReader(this.openFileInput("guess_times_$currentUser.txt"))).readLine().toInt()
        guess_correct_time = BufferedReader(InputStreamReader(this.openFileInput("guess_correct_times_$currentUser.txt"))).readLine().toInt()
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Create an instance of GoogleAPIClient.
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        //read the file of collected words
        var isFileExist = false
        try {
            this.openFileInput("user${currentUser}Song${currentSong}V$mapVersion.txt")
            isFileExist = true
        } catch (e: Exception) {
        }
        if (isFileExist) {
            val reader = BufferedReader(InputStreamReader(this.openFileInput("user${currentUser}Song${currentSong}V$mapVersion.txt"))).readLine()
            collectedWordsIndex[0] = reader.split(" ")[0].toInt()
            var i = 1
            while (i <= collectedWordsIndex[0]) {
                collectedWordsIndex.add(reader.split(" ")[i].toInt())
                i++
            }
        }

        //Load solved song list
        isFileExist = false
        try {
            this.openFileInput("solved_song_list_$currentUser.txt")
            isFileExist = true
        } catch (e: Exception) {
        }
        if (isFileExist) {
            val reader = BufferedReader(InputStreamReader(this.openFileInput("solved_song_list_$currentUser.txt"))).readLine()
            solvedSongList[0] = reader.split(" ")[0].toInt()
            var i = 1
            while (i <= solvedSongList[0]) {
                solvedSongList.add(reader.split(" ")[i].toInt())
                i++
            }
        }
        //Get the song title
        val intent = intent
        currentSongTitle = intent.getStringExtra("currentSongTitle")
        youTubeLink = intent.getStringExtra("youTubeLink")
        ifSolved = intent.getBooleanExtra("ifSolved",false)

        println(">>>>>[$tag] Recover document: Size = ${collectedWordsIndex.size}, or as ${collectedWordsIndex[0]}, last one is ${collectedWordsIndex[collectedWordsIndex.size - 1]}")
        println(">>>>> [$tag] exiting onCreate, mGoogleApiClient==$mGoogleApiClient")
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
        println(">>>>> [$tag]onStart")
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected)
            mGoogleApiClient.disconnect()
        //save the history of collected marks
        var saveMark = collectedWordsIndex[0].toString()
        var i = 1
        while (i <= collectedWordsIndex[0]) {
            saveMark = saveMark.plus(" ")
            saveMark = saveMark.plus(collectedWordsIndex[i].toString())
            i++
        }
        saveFile(saveMark, "user${currentUser}Song${currentSong}V$mapVersion.txt")

        hinHistory+=hintTime
        saveFile(hinHistory.toString(),"hint_$currentUser.txt")
        saveFile((mapOpened+1).toString(),"map_opened_$currentUser.txt")

        println(">>>>> [$tag]onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        println(">>>>> [$tag]onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        println(">>>>> [$tag]onRestart")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        /* test isMyLocationEnabled
        generate MyLocationButton or give a message   */
        try {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            // mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            println(">>>>> [$tag]onMapReady:Buttons shown successfully")
        } catch (se: SecurityException) {
            Toast.makeText(this, "Oops! Cannot get you location now", Toast.LENGTH_LONG).show()
            println(">>>>> [$tag]onMapReady:SecurityException")
        }

        //parser for the kml file
        val kmlLocation: String? = if (currentSong in 1..9)
            "MapV${mapVersion}Song0$currentSong.kml"
        else
            "MapV${mapVersion}Song$currentSong.kml"
        val fileIn = this.openFileInput(kmlLocation)
        println(">>>>>[$tag]KML stream input: $fileIn")
        val mapMarkers = KmlParser().parse(fileIn)
        println(">>>>> [$tag] Load Map$currentSong.kml : $fileIn")
        /*val layer = KmlLayer(mMap,fileIn,this)            //cannot edit marks in KMLLayer
        layer.addLayerToMap()*/
        var counter = 0
        while (counter < mapMarkers!!.size) {
            println(">>>>> [$tag] MapMarkers${1 + counter}=" + mapMarkers[counter])
            counter++
        }
        counter = 0
        placeMarkerNumber = mapMarkers.size

        //To show the markers in the map. check if marker is the type wanted and if it was collected before
        while (counter < mapMarkers.size && counter < totalPointsMax) {
            when (mapMarkers[counter].description) {
                "boring" -> {
                    if (boring) {
                        if (collectedWordsIndex.size - 1 > 0) {
                            if (wordsCollectedBefore(collectedWordsIndex, counter)) {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                placeMarker[counter]!!.isVisible = false
                            } else {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                wordsAddedVisible++
                            }

                        } else {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            wordsAddedVisible++
                        }
                    } else {
                        placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                        placeMarker[counter]!!.isVisible = false
                    }
                }
                "notboring" -> {
                    if (notBoring) {
                        if (collectedWordsIndex.size - 1 > 0) {
                            if (wordsCollectedBefore(collectedWordsIndex, counter)) {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                placeMarker[counter]!!.isVisible = false
                            } else {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                wordsAddedVisible++
                            }
                        } else {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            wordsAddedVisible++
                        }
                    } else {
                        placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                        placeMarker[counter]!!.isVisible = false
                    }

                }
                "interesting" -> {
                    if (interesting) {
                        if (collectedWordsIndex.size - 1 > 0) {
                            if (wordsCollectedBefore(collectedWordsIndex, counter)) {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                placeMarker[counter]!!.isVisible = false
                            } else {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                wordsAddedVisible++
                            }
                        } else {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            wordsAddedVisible++
                        }
                    } else {
                        placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                        placeMarker[counter]!!.isVisible = false
                    }
                }
                "veryinteresting" -> {
                    if (veryInteresting) {
                        if (collectedWordsIndex.size - 1 > 0) {
                            if (wordsCollectedBefore(collectedWordsIndex, counter)) {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                placeMarker[counter]!!.isVisible = false
                            } else {
                                placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                                wordsAddedVisible++
                            }
                        } else {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            wordsAddedVisible++
                        }
                    } else {
                        placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                        placeMarker[counter]!!.isVisible = false
                    }
                }
                "unclassified" -> {
                    if (collectedWordsIndex.size - 1 > 0) {
                        if (wordsCollectedBefore(collectedWordsIndex, counter)) {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(noWhere.latitude, noWhere.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            placeMarker[counter]!!.isVisible = false
                        } else {
                            placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                            wordsAddedVisible++
                        }
                    } else {
                        placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                        wordsAddedVisible++
                    }
                }
            }
            counter++
        }
        //move camera to the center of the play zone55.945025
        val edinburgh = LatLng(55.945025, -3.188550)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edinburgh))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        val recoveredData = "You've collected ${collectedWordsIndex[0]} words before,\nThere are $wordsAddedVisible words for collection"
        val newData = "According to game setting\nThere are $wordsAddedVisible words for collection"
        if (collectedWordsIndex[0] != 0) {
            val counterToast = Toast.makeText(this, recoveredData, Toast.LENGTH_LONG)
            counterToast.setGravity(Gravity.CENTER, 0, 0)
            counterToast.show()
        } else {
            val counterToast = Toast.makeText(this, newData, Toast.LENGTH_LONG)
            counterToast.setGravity(Gravity.CENTER, 0, 0)
            counterToast.show()
        }

        //set the event when the hint button is clicked
        mapHint.setOnClickListener {
            val i = hintTime % 4
            val hint1 = currentSongInfo!!.Title[0]
            val hint2 = currentSongInfo!!.Title.length
            val hint3 = currentSongInfo!!.Artist[0]
            when (i) {
                0 -> Toast.makeText(this, "Hint1:\nThe title starts with $hint1", Toast.LENGTH_SHORT).show()
                1 -> Toast.makeText(this, "Hint2:\nThe title has $hint2 chars", Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(this, "Hint3:\nThe artist name starts with $hint3", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "You have got enough hits!", Toast.LENGTH_SHORT).show()
            }
            hintTime++
        }

        //The event when press the guess button in map activity
        mapGuess.setOnClickListener{
            val editText = EditText(this)
            editText.gravity = Gravity.CENTER
            editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(100))
            val guessBox = AlertDialog.Builder(this)
            guessBox.setTitle("Any idea about song No.$currentSong?")
            guessBox.setMessage("Case-insensitive, careful symbols:")
            guessBox.setView(editText)
            guessBox.setPositiveButton("Am I right?", { _, _ ->
                guess_time++
                saveFile(guess_time.toString(),"guess_times_$currentUser.txt")
                val text = editText.text.toString()
                val rightOrNot = text.equals(currentSongTitle,true)
                if(rightOrNot){
                    guess_correct_time++
                    saveFile(guess_correct_time.toString(),"guess_correct_times_$currentUser.txt")
                    val guessRight = AlertDialog.Builder(this)
                    guessRight.setTitle("Congratulations!")
                    guessRight.setMessage("You made it by collecting ${collectedWordsIndex[0]} words!")
                    guessRight.setPositiveButton("Watch it on YouTube→",{ _, _ ->
                        if(!ifSolved){
                            solvedSongList[0]++
                            solvedSongList.add(currentSong)
                            var saveMark = solvedSongList[0].toString()
                            var i = 1
                            while (i <= solvedSongList[0]) {
                                saveMark = saveMark.plus(" ")
                                saveMark = saveMark.plus(solvedSongList[i].toString())
                                i++
                            }
                            saveFile(saveMark, "solved_song_list_$currentUser.txt")
                        }
                        val intent = Intent(this, WebActivity::class.java)
                        intent.putExtra("Link",youTubeLink)
                        startActivity(intent)
                        this@MapsActivity.finish()
                    })
                    guessRight.setNegativeButton("No, thanks!", { _, _ ->
                        if(!ifSolved){
                            solvedSongList[0]++
                            solvedSongList.add(currentSong)
                            var saveMark = solvedSongList[0].toString()
                            var i = 1
                            while (i <= solvedSongList[0]) {
                                saveMark = saveMark.plus(" ")
                                saveMark = saveMark.plus(solvedSongList[i].toString())
                                i++
                            }
                            saveFile(saveMark, "solved_song_list_$currentUser.txt")
                        }

                        this@MapsActivity.finish()
                    })
                    guessRight.show()
                }else{
                    Toast.makeText(this,"Oops, not right!\nClick the bulb icon to get hint",Toast.LENGTH_LONG).show()
                }
            })
            guessBox.setNegativeButton("Cancel", null)
            guessBox.show()
        }

        //set the event for viewing collected words
        mapLook.setOnClickListener{
            val total = collectedWordsIndex.size-1
            if(total==0){
                val viewWords = AlertDialog.Builder(this)
                viewWords.setTitle("You haven't collect any words!")
                viewWords.setMessage("Come back later!")
                viewWords.setPositiveButton("OK!", { _, _ ->})
                viewWords.show()
            }
            else{
                val viewWords = AlertDialog.Builder(this)
                viewWords.setTitle("Words review")
                viewWords.setMessage("Great! You have collected ${collectedWordsIndex.size-1} words")
                viewWords.setPositiveButton("Show me now!", { _, _ ->
                    val intent = Intent(this, CollectedWordsActivity::class.java)
                    intent.putExtra("collectedWords",collectedWordsIndex)
                    startActivity(intent)
                })
                viewWords.setNegativeButton("Cancel",null)
                viewWords.show()
            }
        }

        println(">>>>> [$tag]onMapReady")
    }

    private fun createLocationRequest() {
        // Set the parameters for the location request
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000 // preferably every 5 seconds
        mLocationRequest.fastestInterval = 1000 // at most every second
        mLocationRequest.priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY
        // Can we access the user’s current location?
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this)
        }
        println(">>>>> [$tag]createLocationRequest")
    }

    override fun onConnected(connectionHint: Bundle?) {
        try {
            createLocationRequest()
        } catch (ise: IllegalStateException) {
            println(">>>>[$tag] onConnected: IllegalStateException thrown")
        }
        // Can we access the user's current location?
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mLastLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        } else ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                permissionsRequestAccessFineLocation)
        println(">>>>> [$tag]onConnected")
    }

    override fun onLocationChanged(current: Location?) {

        if (current == null) {
            println(">>>>> [$tag] onLocationChanged: Location unknown")
        } else {//To collect the word:
            val myLocation = LatLng(current.latitude, current.longitude)
            var count = 0
            println(""">>>>> [$tag] onLocationChanged: ${current.latitude}/${current.longitude} now""")
            while (count < placeMarkerNumber) {
                val distance = SphericalUtil.computeDistanceBetween(myLocation, placeMarker[count]!!.position)
                println(">>>>>[$tag]OnLocationChanged distance between location $count: : $myLocation and $placeMarker is $distance m")
                if (distance < accuracy) {                            //if distance < accuracy, consider the word is collected
                    println(">>>>>[$tag]placeMarkNumber = $placeMarkerNumber, count = $count")
                    val index = placeMarker[count]!!.title
                    line = (index.split("  ")[0]).split(":")[0].toInt()
                    column = (index.split("  ")[0]).split(":")[1].toInt()
                    val word = readLyricFile(currentSong, line, column)
                    Toast.makeText(this, "Word collected: $word", Toast.LENGTH_SHORT).show()
                    placeMarker[count]!!.isVisible = false
                    placeMarker[count]!!.position = noWhere
                    collectedWordsIndex[0]++
                    collectedWordsIndex.add(count)
                    println(">>>>>[$tag]collect word:Total words: ${collectedWordsIndex.size - 1} File $currentSong, line $line column $column , word : $word, It's the number $count word in list,type ${placeMarker[count]!!.title}")
                }
                count++
            }
        }

    }

    override fun onConnectionSuspended(flag: Int) {
        println(" >>>>[$tag] onConnectionSuspended")
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Toast.makeText(this, "Connection to Google APIs Failed", Toast.LENGTH_LONG).show()
        println(" >>>> [$tag]onConnectionFailed")
    }

    private fun saveFile(data: String, filename: String) {
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

    private fun wordsCollectedBefore(list: ArrayList<Int>, index: Int): Boolean {
        var i = 1
        while (i < list.size - 1) {
            if (list[i] == index) {
                return true
            }
            i++
        }
        return false
    }

    //Locate the specific word in the lyrics
    private fun readLyricFile(currentSong: Int, line: Int, column: Int): String? {
        val address = if (currentSong in 1..9)
            "Lyric0$currentSong.txt"
        else
            "Lyric$currentSong.txt"
        var text: String? = ""
        try {
            val fileIn: FileInputStream? = this.openFileInput(address)
            val reader = BufferedReader(InputStreamReader(fileIn))
            var i = 0
            while (i < line) {
                text = reader.readLine()
                i++
            }
        } catch (e: Exception) {
            println(">>>>> Failed to read specific word in file")
        }
        return text!!.split(" ")[column - 1]
    }
}