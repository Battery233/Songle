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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.data.kml.KmlLayer
import java.io.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private val permissionsRequestAccessFineLocation = 1
    private var mLastLocation: Location? = null             // getLastLocation can return null
    private val tag = "MapsActivity"

    //Get the song index number here
    private var currentSong = 1                            //Flag for the song chosen in the list
    private var mapVersion = 1                             //map version

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        try {
            val  fileIn: FileInputStream? = if (currentSong in 1..9)            //TODO: This is the advantage of kotlin
                this.openFileInput("MapV${mapVersion}Song0$currentSong.kml")
            else
                this.openFileInput("MapV${mapVersion}Song$currentSong.kml")
            val layer = KmlLayer(mMap,fileIn,this)
            layer.addLayerToMap()
            println(">>>>> [$tag] Load Map$currentSong.kml : ${fileIn.toString()}")
        }
        catch (e:IOException)
        {
            Toast.makeText(this,"Load KML failed",Toast.LENGTH_SHORT).show()
        }

        /*sample of a landmark
        Add a marker in Edinburgh and move the camera
        val Edinburgh = LatLng(55.9439327, -3.1905939)
        mMap.addMarker(MarkerOptions().position(LatLng(55.942900, -3.19099)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("5"))
        */
        println(">>>>> [$tag]onMapReady")
        println("The output word is: "+readLyricFile(currentSong,8,5))
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
        } else {
            println(""">>>>> [$tag] onLocationChanged: ${current.latitude}/${current.longitude} now""")
        }

    }

    override fun onConnectionSuspended(flag: Int) {
    println(" >>>>[$tag] onConnectionSuspended")
    }
    override fun onConnectionFailed(result : ConnectionResult) {
        Toast.makeText(this,"Connection to Google APIs Failed",Toast.LENGTH_LONG).show()
        println(" >>>> [$tag]onConnectionFailed")
    }

    //Locate the specific word in the lyrics
    private fun readLyricFile(songNumber:Int,line:Int,column:Int):String?{
        val address = if (songNumber in 1..9)
            "Lyric0$songNumber.txt"
        else
            "Lyric$songNumber.txt"
        var text:String? = ""
        try {
            val  fileIn:FileInputStream? = this.openFileInput(address)
            val reader =BufferedReader(InputStreamReader(fileIn))
            var i = 0
            while (i < line)
            {
                text = reader.readLine()
                i++
            }
        } catch (e: Exception) {
            println(">>>>> Failed to read specific word in file")
        }
        return text!!.split(" ")[column-1]
    }
}