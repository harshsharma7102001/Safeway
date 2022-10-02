package com.world4tech.safeway

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.world4tech.safeway.databinding.ActivityHelpBinding
import com.world4tech.safeway.util.GetNearbyPlacesData
import org.jsoup.Jsoup
import java.util.*

class HelpActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    private lateinit var binding:ActivityHelpBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var temporaryLoc: LatLng?=null
    //newly added for places
    var PROXIMITY_RADIUS: Int = 3000
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    var userLocationMarker: Marker? = null
    var userLocationAccuracyCircle: Circle? = null
    private  lateinit var lastLocation: Location
    private var markerOptions:MarkerOptions?= null
    var p3:Float = 0f
    var p4:Float = 0f
    private var destinationLoc:LatLng?=null
    private val INTERVAL = (1000 * 2).toLong()
    private val FASTEST_INTERVAL = (1000 * 1).toLong()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        mapFragment.getMapAsync(this)
        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag")
        }
        val i = intent?.extras?.getString("btn_no").toString()
//        val name=intent?.extras?.getString("name").toString()
        setTextwithno(i)
        setTextwithnotwo(i)
        val handler = Handler()
            handler.postDelayed(object :Runnable{
                override fun run() {
                    println("URl of police station is: https://www.google.com/search?q=${temporaryLoc!!.latitude}%2C${temporaryLoc!!.longitude}+police+phone+numer")
                    FetchDistance().execute()
                }
            },7000)
        ///maps near by places
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try{
            val success: Boolean = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.")
            }
        }catch (e:Exception){
            Log.d("Tag",e.message.toString())
        }
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener (this)
        setupMap()
//         Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun setupMap() {
        checkPermission()
        try{
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location !=null){
                    lastLocation = location
//                        val currentLatLong = LatLng(location.latitude,location.longitude)
                    val currentLatLong = LatLng(location.latitude,location.longitude)
                    temporaryLoc = currentLatLong
                    findNearByPlace("hospital")
                    println("Lat long is ${location.latitude} and ${location.longitude}")
//                        getAreaName(location.latitude,location.longitude)
//                        placeMarkerOnMap(temporaryLoc!!)
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
                    requestNewLocationData();
                }else{
                    Toast.makeText(this,"No location data available", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e:Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }

        return
    }
    private fun checkPermission(): Unit {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS),
                LOCATION_REQUEST_CODE
            )
            return
        }
//        mMap.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED
            ) {
                setupMap()
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
    //    Location Looping every second-------------------------------------------------------
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest.setInterval(INTERVAL)
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            p3 = mLastLocation!!.longitude.toFloat()
            p4 = mLastLocation.latitude.toFloat()
            val destination:LatLng  = LatLng(mLastLocation.latitude,mLastLocation.longitude)
            destinationLoc = destination
            val dSpeed = mLastLocation.speed.toDouble()

            if (mMap!=null){
                val LatLng = LatLng(locationResult.lastLocation!!.latitude,locationResult.lastLocation!!.longitude)
//                placeMarkerOnMap(LatLng)
                setUserLocationMarker(locationResult.lastLocation!!)
            }
            setupMap()
        }
    }
    private fun setUserLocationMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (userLocationMarker == null) {
            //Create a new marker
            markerOptions = MarkerOptions()
            markerOptions!!.position(latLng)
            markerOptions!!.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top))
            markerOptions!!.rotation(location.bearing)
            markerOptions!!.anchor(0.5.toFloat(), 0.5.toFloat())
            userLocationMarker = mMap.addMarker(markerOptions!!)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        } else {
            //use the previously created marker
            userLocationMarker!!.position = latLng
            userLocationMarker!!.rotation = location.bearing
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        }
        if (userLocationAccuracyCircle == null) {
            val circleOptions = CircleOptions()
            circleOptions.center(latLng)
            circleOptions.strokeWidth(4f)
            circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
            circleOptions.fillColor(Color.argb(32, 255, 0, 0))
            circleOptions.radius(location.accuracy.toDouble())
            userLocationAccuracyCircle = mMap.addCircle(circleOptions)
        } else {
            userLocationAccuracyCircle!!.setCenter(latLng)
            userLocationAccuracyCircle!!.setRadius(location.accuracy.toDouble())
        }
    }
    override fun onMarkerClick(p0: Marker):Boolean{
        val lat = p0.position.latitude
        val lon = p0.position.longitude
        val loc = getSosArea(lat,lon)
        val i = Intent(this,DetailActivity::class.java)
        i.putExtra("name",p0.title.toString())
        i.putExtra("completeLocation",loc)
        i.putExtra("latitude",lat.toString())
        i.putExtra("longitude",lon.toString())
        startActivity(i)

        return true
    }
    //-----------------------------------------------------------------------------
    private fun setTextwithnotwo(i: String) {
        val accident="Look for possible signs of injury and try giving first-aid until help arrives.\n" +
                "Console them and provide water/juice to keep them hydrated.\n" +
                "Ask help from passersby to take out victims safely out of their vehicle and relocate it to the roadside.\n" +
                "Stay with them until help arrives."
        val theft="If you see something suspicious, ask them if something is wrong or try to help them from a safer distance.\n" +
                "Try gathering people to take down robbers and hand them over to Police.\n" +
                "In case the robbers have left, let the victims know that you have called for emergency. Help them with a phone if they need."
        val heart="Loosen any tight clothing around the neck of to aid breathing.\n" +
                "Cushion their head if they are laid on ground. Don’t make them move unnecessarily until convulsions stop.\n" +
                "Turn them to their side after the convulsions stop.\n" +
                "Make them talk and note the time the seizure starts and stops.\n" +
                "Wait for medical help and don’t make the victim do much activity. CPR can be given if victim falls unconcious."
        if (i=="1"){
            binding.logo.setBackgroundResource(R.drawable.accident_icon)
            binding.textfieldtwo.text = accident
            binding.headingone.text= "Steps to Remember After Accident"
        }else if(i=="2"){
            binding.logo.setBackgroundResource(R.drawable.emergency)
            binding.textfieldtwo.text =heart
            binding.headingone.text="How to Help Someone During Heart Attack"
        }else if(i=="3"){
            binding.logo.setBackgroundResource(R.drawable.theft_icon)
            binding.textfieldtwo.text = theft
            binding.headingone.text = "Steps to Remember After Theft/Robbery"
        }
    }

    private fun setTextwithno(s: String) {
        val accident = "Try moving your legs, arms, and other body parts slowly to look for blood and bruises. \n" +
                "Wash the wound and cover it with a clean cloth or keep it pressed to avoid major blood loss.\n" +
                "Reach out for First-aid kit in car if you have kept one. Remember to keep one in future.\n" +
                "If possible, drive your vehicle to safer side of the road. Exit the vehicle soon after.\n" +
                "Contact your Insurance company to claim insurance and wait for police to register FIR.\n" +
                "Stay hydrated until Ambulance arrives. "
        val robbery = "Stay calm and try to remember the vehicle number of the robbers and note it down to provide it to Police.\n" +
                "Make a list of things taken by the robbers. If they have taken your vehicle, remember the things present in your car.\n" +
                "In case of a car theft, contact the Insurance Company and let them know about the robbery.\n" +
                "If they have injured you or anyone else, wash the wound and cover it with a clean cloth, provide first-aid if possible.\n" +
                "Wait for police to arrive and file an FIR immediately."
        val heartattack = "Take the person out of the vehicle and lay them down in an open environment.\n" +
                "If you feel early symptoms of heart attack or slight pain, swallow or chew an Aspirin. Keep them handy in your vehicle’s first-aid kit.\n" +
                "If you see someonegoing through a possible heart attack and falls unconcious, help them with a CPR."
        if (s=="1"){
            binding.textfield.setText(accident)
            binding.headingtwo.text = "If Someone Else Had a Car Accident"
            val handler = Handler()
            handler.postDelayed(object :Runnable{
                override fun run() {
                    findNearByPlace("hospital")
                }
            },3000)
        }else if(s=="2"){
            binding.textfield.setText(robbery)
            binding.headingtwo.text = "During Seizure or other Medical Attacks"
//            val handler = Handler()
//            handler.postDelayed(object :Runnable{
//                override fun run() {
//                    findNearByPlace("police+station")
//                }
//            },3000)
        }else{
            binding.textfield.setText(heartattack)
            binding.headingtwo.text="If You Are Witnessing a Live Theft"
//            val handler = Handler()
//            handler.postDelayed(object :Runnable{
//                override fun run() {
//                    findNearByPlace("police+station")
//                }
//            },3000)
        }
    }
    // ----------------new function for fetching near by things-----------------------------------------------------
    fun findNearByPlace(type: String) {
        val dataTransfer = arrayOfNulls<Any>(2)
        val getNearbyPlacesData = GetNearbyPlacesData()
        val nearby = type
        val url = getUrl(temporaryLoc!!.latitude, temporaryLoc!!.longitude, nearby)
        dataTransfer[0] = mMap
        dataTransfer[1] = url
        getNearbyPlacesData.execute(*dataTransfer)
        //Set Custom InfoWindow Adapter
        //Set Custom InfoWindow Adapter

    }
    fun getUrl(latitude: Double, longitude: Double, nearby: String): String? {
        val googlePlaceUrl =
            StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?")
        googlePlaceUrl.append("location=$latitude,$longitude")
        googlePlaceUrl.append("&radius=$PROXIMITY_RADIUS")
        googlePlaceUrl.append("&type=$nearby")
        googlePlaceUrl.append("&sensor=true")
        googlePlaceUrl.append("&key=" + "AIzaSyCKPH_dYkt6H3tX2IG0PaxqhQxYxgMKJys")
        Log.d("MapsActivity", "url = $googlePlaceUrl")
        return googlePlaceUrl.toString()
    }

    //-----------------------------------------------------------------------------
    fun getSosArea(lat: Double,lon: Double):String{
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val cityName = addresses[0].getAddressLine(0)
            return cityName
//            println("current area name is: ${stateName[3]} and current city name is: ${stateName[5]} \n array of adress is: $addresses")
        }catch (e:Exception){
            println("You got some error: ${e.message}")
            val message = "Latitude: $lat ,Longitude: $lon"
            return message
        }
    }
    //Fetch police station
    inner class FetchDistance():AsyncTask<Void,Void,String>(){
        override fun doInBackground(vararg p0: Void?): String? {
            try{

                val doc = Jsoup.connect("https://www.google.com/search?q=${temporaryLoc!!.latitude}${temporaryLoc!!.longitude}+police+number").get()
                var time = doc.getElementsByClass("QsDR1c")
                val rec_dis = time.text()
                val rec_time=rec_dis.substring(0,rec_dis.indexOf("m")).replace("\\s".toRegex(), "")
                val act_dis = rec_dis.substring(rec_dis.indexOf("(")+1, rec_dis.indexOf(")"))
//                km = act_dis
//                est_time = "$rec_time min"
                println("Police Staion Data fetched is:  $rec_dis")
            }catch (e:Exception){
                println("Error got while fetchins is: ${e.message}")
//                println("Url of google maps website is: https://www.google.com/maps/dir/'${temporaryLoc!!.latitude},${temporaryLoc!!.longitude}'/'${destinationLoc!!.latitude},${destinationLoc!!.longitude}'/data=!3m1!4b1!4m10!4m9!1m3!2m2!1d77.1384139!2d28.6201526!1m3!2m2!1d77.1855!2d28.5245!3e0")

            }
            return null
        }
    }
}