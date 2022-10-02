package com.world4tech.safeway

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import com.world4tech.safeway.databinding.ActivityMapsBinding
import com.world4tech.safeway.parameters.MapData
import com.world4tech.safeway.util.GetNearbyPlacesData
import com.world4tech.safeway.util.decodePolyline
import com.world4tech.safeway.util.playmusic
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener, TextToSpeech.OnInitListener {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private  lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var temporaryLoc: LatLng?=null
    private var destinationLoc:LatLng?=null
    var userLocationMarker: Marker? = null
    var userLocationAccuracyCircle: Circle? = null
    private lateinit var roadname:Array<String>
    private lateinit var roadspeed:Array<String>
    private var markerOptions:MarkerOptions?= null
    private lateinit var finaldest:LatLng
    private var km:String=""
    private var destname=""
    private var ttl_time:String =""
    //Adding void feature
    private var tts: TextToSpeech? = null
    //newly added for places
    var PROXIMITY_RADIUS: Int = 2000
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    private lateinit var api:Array<String>
    private var currentlocation:String = ""
    private var CITY:String = ""
    private var streetName:String=""
    //---------Speedometer---------------------
    var p1: Float = 0f
    var p2 :Float = 0f
    var p3:Float = 0f
    var p4:Float = 0f
    private val INTERVAL = (1000 * 2).toLong()
    private val FASTEST_INTERVAL = (1000 * 1).toLong()
    private var count=0;
//    -------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Progress Dialogue
        val circularProgress = binding.circularProgress
        circularProgress.setColor(Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN)
        circularProgress.setBodyColor(R.color.gradient)
        circularProgress.setRotationSpeeed(25)
        circularProgress.visibility=View.VISIBLE
        api = arrayOf("22f3889a42339bf14fabbbf55c067095","8118ed6ee68db2debfaaa5a44c832918")
        // assign TextToSpeech to this context and this listener.
        tts = TextToSpeech(this, this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        val lat = intent?.extras?.getString("lat").toString().toDouble()
        val lon = intent?.extras?.getString("lon").toString().toDouble()
    val locations = intent?.extras?.getString("location").toString()
    finaldest = LatLng(lat,lon)
    val checklat = lat.toString()
        println("received latitude and longitude is: $lat and $lon")
        mapFragment.getMapAsync(this)
        // Initializing the Places API with the help of our API_KEY
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag")
        }
       //Create a Road name and speed activity
    roadname = arrayOf(" Suraj Kund Badkhal Rd"," Naraina Ring Road"," Ring Rd"," NH 48"," NH 1"," Outer Ring Road "," Gurugram-Faridabad Road"," Agra expressway"," Mathura Road"," Outer Ring Rd Flyover"," Lajpat Rai Market")
    roadspeed = arrayOf("50","60","60","70","80","90","70","100","60","60","60")
    getAreaName(lat,lon)
        //---------------------------------------------------------------------------------
        val handler = Handler()
        handler.postDelayed(object:Runnable{
            override fun run() {
                if (checklat.isNotEmpty()){
                    mapFragment.getMapAsync {
                        mMap = it
                        val originLocation = LatLng(temporaryLoc!!.latitude, temporaryLoc!!.longitude)
                        val destinationLocation = LatLng(lat,lon)
                        mMap.addMarker(MarkerOptions().position(destinationLocation)
                            .title("destination"))

                        val urll = getDirectionURL(
                            originLocation,
                            destinationLocation,
                            "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag"
                        )
                        GetDirection(urll).execute()
//                getDistance(temporaryLoc!!.latitude, temporaryLoc!!.longitude,destinationLocation.latitude,destinationLocation.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
                    }
                    binding.circularProgress.visibility = View.GONE
                }else{
                    if (locations.isNotEmpty()){
                        findNearByPlace(locations)
                    }else{
                        Toast.makeText(this@MapsActivity,"Kindly choose a destination",Toast.LENGTH_SHORT).show()
                    }
                }
                binding.overviewContainer.visibility = View.VISIBLE
            }
        },5000)
        binding.navigation.setOnClickListener {
//            fetchnearyByLocation("atm")
            findNearByPlace("Police")
        }
    binding.sos.setOnClickListener {
        count++;
        val handler = Handler()
        handler.postDelayed({
            if(count==1){
                val currentLocation = getSosArea(temporaryLoc!!.latitude,temporaryLoc!!.longitude)
                val i = Intent(this@MapsActivity,SosActivity::class.java)
                i.putExtra("option","1")
                i.putExtra("currentlocation",currentLocation)
                i.putExtra("lat",temporaryLoc!!.latitude.toString())
                i.putExtra("lon",temporaryLoc!!.longitude.toString())
                startActivity(i)
            }else{
                val currentLocation = getSosArea(temporaryLoc!!.latitude,temporaryLoc!!.longitude)
                val i = Intent(this@MapsActivity,SosActivity::class.java)
                i.putExtra("option","2")
                i.putExtra("currentlocation",currentLocation)
                i.putExtra("lat",temporaryLoc!!.latitude.toString())
                i.putExtra("lon",temporaryLoc!!.longitude.toString())
                startActivity(i)
            }
            count=0;
        },500)
    }
    binding.startBtn.setOnClickListener {
//        mapFragment.getMapAsync {
//            mMap = it
//            val originLocation = LatLng(temporaryLoc!!.latitude, temporaryLoc!!.longitude)
//            val destinationLocation = LatLng(lat,lon)
//            mMap.addMarker(MarkerOptions().position(destinationLocation))
//            val urll = getDirectionURL(
//                originLocation,
//                destinationLocation,
//                "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag"
//            )
//            GetDirection(urll).execute()
////                getDistance(temporaryLoc!!.latitude, temporaryLoc!!.longitude,destinationLocation.latitude,destinationLocation.longitude)
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))
//        }
//        mMap.clear()
//       finish()
        println("Url of google maps website is: https://www.google.com/maps/dir/'${temporaryLoc!!.latitude},${temporaryLoc!!.longitude}'/'${finaldest!!.latitude},${finaldest!!.longitude}'/data=!3m1!4b1!4m10!4m9!1m3!2m2!1d77.1384139!2d28.6201526!1m3!2m2!1d77.1855!2d28.5245!3e0")
    }
    binding.avatar.setOnClickListener {
        val i =Intent(this,ProfileActivity::class.java)
        startActivity(i)
    }
    binding.buddy.setOnClickListener {
        if(binding.moreDialogue.visibility == View.VISIBLE){
            binding.moreDialogue.visibility = View.INVISIBLE
        }else{
            binding.moreDialogue.visibility = View.VISIBLE
        }
    }
    binding.CabBooking.setOnClickListener {
        val i = Intent(this,BuddyActivity::class.java)
        startActivity(i)
    }
    binding.quickRepair.setOnClickListener {
        val i = Intent(this,BuddyActivity::class.java)
        startActivity(i)
    }
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
        val handler = Handler()
        handler.postDelayed(object :Runnable{
            override fun run() {
                weather().execute()
//                weatherTask().execute()
                FetchTask().execute()

            }
        },4000)
        handler.postDelayed(object :Runnable{
            override fun run() {
                binding.destination.text = km
                binding.duration.text = ttl_time
                println("In Handler value of kilometer is: $km and time is: $ttl_time")
                analyseweather()
            }
        },8000)
//         Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun analyseweather() {
        if(binding.status.text == "Fog"){
            speakToText("Weather is foggy. Kindly drive carefully")
        }else if(binding.status.text=="Sunny"){
            speakToText("All set. Enjoy your ride")
        }else if(binding.status.text == "Smoke"){
            speakToText("Visibility is low. So drive carefully")
        }
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
//                        Speedometer Setting
                        p1 = location.longitude.toFloat()
                        p2 = location.latitude.toFloat()
                        val dSpeed = location.speed.toDouble()
                        val a = 3.6 * dSpeed
                        val kmhSpeed = Math.round(a).toInt()
                        binding.speed.text = kmhSpeed.toString()
//                        ---------------------------------------------
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

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        markerOptions = MarkerOptions().position(currentLatLong)
            .title("My Locations")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.currentposition_marker))
        mMap.addMarker(markerOptions!!)
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
    override fun onMarkerClick(p0: Marker):Boolean{
       if(p0.title=="destination"){
           val i = Intent(this,NewnewsActivity::class.java)
           i.putExtra("area",destname)
           startActivity(i)
       }else{
           val lat = p0.position.latitude
           val lon = p0.position.longitude
           val loc = getSosArea(lat,lon)
           val i = Intent(this,DetailActivity::class.java)
           i.putExtra("name",p0.title.toString())
           i.putExtra("completeLocation",loc)
           i.putExtra("latitude",lat.toString())
           i.putExtra("longitude",lon.toString())
           startActivity(i)
       }

        return true
    }
    //-------------------------------Direction-----------------------------------------------


    //----------------------------------------draw route Function --------------------------------------
    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag"
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }
    //-------------------------------------------Get Area name function----------------------------------------
    fun getAreaName(lat:Double,lon:Double){
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            val cityName = addresses[0].getAddressLine(0)
            val stateName = cityName.split(",")
            currentlocation = stateName[2].toString()
            var cityname = stateName[2]
            var ucityname = cityname.replace("\\s".toRegex(), "")
            var countryname = stateName[4]
            destname = ucityname
            var totalinfo ="$ucityname,$countryname"
            CITY = totalinfo.replace("\\s".toRegex(), "")
            binding.areaname.text = ucityname
            println("========================================City Details==============================================================")
            println("Updated City details name is: ${CITY}")
            println("ucity name is: $ucityname & $countryname")
            println("The weather url is:https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=${api[1]} ")
            println("Current Street name is: ${stateName}")
            streetName = stateName[1]
            println("The distance btw 2 points is: ${SphericalUtil.computeDistanceBetween(temporaryLoc, destinationLoc).toString()} ")
//            println("current area name is: ${stateName[3]} and current city name is: ${stateName[5]} \n array of adress is: $addresses")
        }catch (e:Exception){
            println("You got some error: ${e.message}")
        }
    }
    //-------------->Speed LImit Setting Fucntion
    private fun setSpeedLimit() {
        val currentSpeed = binding.speed.text.toString().toInt()
        println("You are in $streetName")
       for (i in roadname.indices){
           println("Data in roadname array is: ${roadname[i]} and current road name is: ${streetName}")
           if (streetName == roadname[i]){
               binding.speedlimit.text = roadspeed[i]
               val limit = binding.speedlimit.text.toString().toInt()
               if (currentSpeed > limit){
                   playmusic(this@MapsActivity)
                   binding.speed.setTextColor(Color.parseColor("#FF0000"))
               }
           }else{
               binding.speedlimit.text = "60"
           }
       }
        //Time now
        val dateandTime = Calendar.getInstance().time
        val tf= SimpleDateFormat("hh:mm")
        val time= tf.format(dateandTime)
        val txt_time=time.toString()
        binding.timenow.text = txt_time
    }



    //---------------------------------------

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
    //-----------------------------------------------------------------------------------------Weather Activity
    inner class weather():AsyncTask<Void,Void,String>(){
        override fun doInBackground(vararg p0: Void?): String? {
            try{
                val doc = Jsoup.connect("https://www.google.com/search?q=temperature+at+$CITY").get()
                var tempdegree = doc.getElementsByClass("wob_t q8U8x")
                var condition = doc.getElementsByClass("wob_dcp")
                var tempis=  tempdegree.text()
                var cond = condition.text()
//                name = locname.text()
                println("--------------------------Fetching weather data -----------------------------------")
                println("Searched url is: https://www.google.com/search?q=temperature+at+$CITY ")
                println("Fetched temperature  is: $tempis & condition is: $cond")
                binding.status.text = cond
                binding.temp.text=tempis +"°C"

                println("--------------------------Ending weather data ---------------------------------")
//                phone = locphone.text()
//                println("Fetched Phone number is: $phone")
            }catch (e:Exception){
                println("Error got while fetchins is: ${e.message}")
            }
            return null
        }
    }


    inner class weatherTask() : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()

        }
        override fun doInBackground(vararg params: String?): String? {
            //-------------------------gETTING CURRENT LOCATION IN DETAILS
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=${api[1]}").readText(
                    Charsets.UTF_8
                )
                println("Website of weather is: https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=${api[1]}")
            }catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt*1000)
                )
                val temp = main.getString("temp")+"°C"
                val weatherDescription = weather.getString("description")
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                binding.areaname.text = currentlocation


            } catch (e: Exception) {
                println("Error received is: ${e.message}")
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
            val a = 3.6 * dSpeed
            val kmhSpeed = Math.round(a).toInt()
            binding.speed.text = kmhSpeed.toString()
            val handler = Handler()
            handler.postDelayed(object :Runnable{
                override fun run() {
                    setSpeedLimit()
                }
            },3000)
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
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        } else {
            //use the previously created marker
            userLocationMarker!!.position = latLng
            userLocationMarker!!.rotation = location.bearing
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
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

    //--------------------------------------------------------Calculate Distance Btw 2 points
     fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1,lon1,lat2,lon2,result)
        val distance: Float = result.get(0)
        return distance
    }
    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
    // ----------------new function for fetching near by things-----------------------------------------------------
    fun findNearByPlace(type: String) {
        val dataTransfer = arrayOfNulls<Any>(2)
        val getNearbyPlacesData = GetNearbyPlacesData()
        val nearby = type
        val url = getUrl(temporaryLoc!!.latitude, temporaryLoc!!.longitude, nearby)
        println("Url received fromfindNearByPlace function is : $url")
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

    //For fetching data from google use this url: - https://www.google.com/maps/dir/'28.6201526,77.1384139'/'28.5245,77.1855'/data=!3m1!4b1!4m10!4m9!1m3!2m2!1d77.1384139!2d28.6201526!1m3!2m2!1d77.1855!2d28.5245!3e0
    //Time Class name: - Fk3sm fontHeadlineSmall delay-light
    //Distance Class name: - ivN21e tUEI8e fontBodyMedium
    inner class FetchTask():AsyncTask<Void,Void,String>(){

        override fun doInBackground(vararg p0: Void?): String? {
            try{

                val doc = Jsoup.connect("https://www.google.com/search?q=distance+from+${temporaryLoc!!.latitude}%2C${temporaryLoc!!.longitude}+to+${finaldest!!.latitude}%2C${finaldest!!.longitude}").get()
                var time = doc.getElementsByClass("UdvAnf")
                val rec_dis = time.text()
                val rec_time=rec_dis.substring(0,rec_dis.indexOf("m")).replace("hr", ".").replace("min", "").replace("\\s".toRegex(), "")
                val act_dis = rec_dis.substring(rec_dis.indexOf("(")+1, rec_dis.indexOf(")")).replace("km", "");
                km = act_dis
                ttl_time = rec_time
//
//             println("Fetched time is: ${time.text()} and url is: https://www.google.com/search?q=distance+from+${temporaryLoc!!.latitude}%2C${temporaryLoc!!.longitude}+to+${finaldest!!.latitude}%2C${finaldest!!.longitude}\" ")
                println("Fetched data is: $rec_dis")
                println("Got Time is: ${rec_time} && distance is : $act_dis")
            }catch (e:Exception){
                println("Error got while fetchins is: ${e.message}")
//                println("Url of google maps website is: https://www.google.com/maps/dir/'${temporaryLoc!!.latitude},${temporaryLoc!!.longitude}'/'${destinationLoc!!.latitude},${destinationLoc!!.longitude}'/data=!3m1!4b1!4m10!4m9!1m3!2m2!1d77.1384139!2d28.6201526!1m3!2m2!1d77.1855!2d28.5245!3e0")

            }
            return null
        }
    }
    //--------------------------------This section contains functions of speaking----------------------------------------------------------
    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if(result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                throw IllegalStateException("TextToSpeech not supported or missing!")
            }
            else {
                Log.e("TTS", "Initialization failed!")
            }
        }
    }
    private fun speakToText(text: String) {
        // flushes the current text from the ET and starts the new line.
        // uterranceid is just a remark or comment
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
    override fun onDestroy() {
        super.onDestroy()
        // if the tts still has information present..
        if(tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
//        binding = this
    }

}