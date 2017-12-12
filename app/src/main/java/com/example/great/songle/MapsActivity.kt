package com.example.great.songle

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import java.io.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val permissionsRequestAccessFineLocation = 1
    private var mLastLocation: Location? = null             // getLastLocation can return null
    private val tag = "MapsActivity"

    //Get the song index number here
    private var currentSong = 1                            //Flag for the song chosen in the list
    private var mapVersion = 1                             //map version
    private var accuracy = 15.0
    private val placeMarker = arrayOfNulls<Marker>(604)
    private var wordsCollected = Array(604) { -1 }
    private var placeMarkerNumber = 0
    private var line = 0
    private var column = 0
    private var wordsCollectedNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get global variable
        val application = this.application as MyApplication
        currentSong = application.getcurrentSong()
        mapVersion = application.getmapVersion()
        accuracy = application.getaccuracy()
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
        println(">>>>> [$tag]onStop")
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
        val kmlLocation: String? = if (currentSong in 1..9) //TODO: This is the advantage of kotlin   if(a = 0)
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
        while (counter < mapMarkers.size && counter < 604) {
            when (mapMarkers[counter].description) {
                "boring" -> placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                "notboring" -> placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                "interesting" -> placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                "veryinteresting" -> placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
                "unclassified" -> placeMarker[counter] = mMap.addMarker(MarkerOptions().position(LatLng(mapMarkers[counter].latitude, mapMarkers[counter].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title("${mapMarkers[counter].name}  ${mapMarkers[counter].description}"))
            }
            counter++
        }
        //move camera to the center of the play zone55.945025
        val edinburgh = LatLng(55.945025, -3.188550)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edinburgh))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))

        println(">>>>> [$tag]onMapReady")
        //Toast.makeText(this, "The word you collected is: " + readLyricFile(currentSong, 3, 5), Toast.LENGTH_LONG).show()
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
        val noWhere = LatLng(90.0,0.0)
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
                    println(">>>>>[$tag]collect word: File $currentSong, line $line column $column , word : $word")
                    Toast.makeText(this, "Word collected: $word", Toast.LENGTH_SHORT).show()
                    placeMarker[count]!!.isVisible = false
                    placeMarker[count]!!.position = noWhere
                    wordsCollected[wordsCollectedNumber] = count                                    //record the index of collected words
                    wordsCollectedNumber++
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