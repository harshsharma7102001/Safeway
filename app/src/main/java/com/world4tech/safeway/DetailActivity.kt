package com.world4tech.safeway

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.world4tech.safeway.databinding.ActivityDetailBinding
import com.world4tech.safeway.parameters.MapData
import com.world4tech.safeway.util.decodePolyline
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.ArrayList

class DetailActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var orgname:String
    private lateinit var binding: ActivityDetailBinding
    private var name:String =""
    private var email:String =""
    private var phone:String = " "
    private var website:String=" "
    private var statues:String=" "
    private var imagelink:String=""
    private var link:Array<String> = arrayOf()
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var temporaryLoc: LatLng?=null
    private var finaldest:LatLng?=null
    private var km:String = ""
    private var est_time:String=""

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    private  lateinit var lastLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Progress Dialogue
        val circularProgress = binding.circularProgress
        circularProgress.setColor(Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN)
        circularProgress.setBodyColor(R.color.gradient)
        circularProgress.setRotationSpeeed(25)
        circularProgress.visibility = View.VISIBLE
        //-------------------------Progress dialogue
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val i = intent?.extras?.getString("completeLocation").toString()
        println("Full addresss is: $i")
        val names = intent?.extras?.getString("name").toString()
        println("Name of location is: $name")
        orgname = names.replace("\\s".toRegex(), "+")
        println("The modified name is : $orgname")
        val lat = intent?.extras?.getString("latitude").toString()
        val checklat = lat
        println("latitude is: $lat")
        val lon = intent?.extras?.get("longitude").toString()
        println("Longitude adress is: $lon")
        val latitude = lat.toDouble()
        val longitude = lon.toDouble()
        finaldest = LatLng(latitude, longitude)
        binding.completeLoc.text = names
        FetchTask().execute()
        val handler = Handler()
        //mAPS activity Task
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag")
        }
        //---------------------------------------------------------------------------------
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (checklat.isNotEmpty()) {
                    mapFragment.getMapAsync {
                        mMap = it
                        val originLocation =
                            LatLng(temporaryLoc!!.latitude, temporaryLoc!!.longitude)
                        val destinationLocation = LatLng(lat.toDouble(), lon.toDouble())
                        mMap.addMarker(MarkerOptions().position(destinationLocation))
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
                } else {
                    Toast.makeText(
                        this@DetailActivity,
                        "Kindly choose a destination",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, 5000)
        handler.postDelayed(object : Runnable {
            override fun run() {
                binding.name.text = name
                binding.status.text = statues
                binding.phone.text = phone
            }
        }, 5000)
        handler.postDelayed(object : Runnable {
            override fun run() {
                binding.distance.text = km
                binding.estimatedTime.text = est_time
            }

        }, 5000)

    }
    inner class FetchTask():AsyncTask<Void,Void,String>(){

        override fun doInBackground(vararg p0: Void?): String? {
            try{
                val doc = Jsoup.connect("https://www.google.com/search?q=$orgname").get()
                var locname = doc.getElementsByClass("qrShPb kno-ecr-pt PZPZlf q8U8x hNKfZe")
                var locstatus = doc.getElementsByClass("JjSWRd")
                var locphone = doc.getElementsByClass("LrzXr zdqRlf kno-fv")
                val img = doc.getElementsByTag("img")
                name = locname.text()
                println("Fetched name is: $name")
                statues = locstatus.text()
                println("Fetched status is: $status")
                phone = locphone.text()
                println("Fetched Phone number is: $phone")
            }catch (e:Exception){
                println("Error got while fetchins is: ${e.message}")
            }
            return null
        }
    }
    //mAPS aCTIVITY
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
        }catch (e:Exception) {
            Log.d("TAG",e.message.toString())
        }
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener (this)
        setupMap()
        FetchDistance().execute()
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
//                        Speedometer Setting
//                        ---------------------------------------------
                    println("Lat long is ${location.latitude} and ${location.longitude}")
                    placeMarkerOnMap(temporaryLoc!!)
                }else{
                    Toast.makeText(this,"No location data available", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e:Exception){
            Toast.makeText(this,e.message, Toast.LENGTH_SHORT).show()
        }

        return
    }
    //---------------------------------(draw route)
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

    //------------------------------------------------------------------

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
            .title("My Locations")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_top))
        mMap.addMarker(markerOptions)
    }

    private fun checkPermission(): Unit {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE),
                LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED && grantResults[2]== PackageManager.PERMISSION_GRANTED
            ) {
                setupMap()
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onMarkerClick(p0: Marker)= false

    //Fetching Distance from current location to destinatin location
    //Distance Class name: - ivN21e tUEI8e fontBodyMedium
    inner class FetchDistance():AsyncTask<Void,Void,String>(){

        override fun doInBackground(vararg p0: Void?): String? {
            try{

                val doc = Jsoup.connect("https://www.google.com/search?q=distance+from+${temporaryLoc!!.latitude}%2C${temporaryLoc!!.longitude}+to+${finaldest!!.latitude}%2C${finaldest!!.longitude}").get()
                var time = doc.getElementsByClass("UdvAnf")
                val rec_dis = time.text()
                val rec_time=rec_dis.substring(0,rec_dis.indexOf("m")).replace("\\s".toRegex(), "")
                val act_dis = rec_dis.substring(rec_dis.indexOf("(")+1, rec_dis.indexOf(")"))
                km = act_dis
                est_time = "$rec_time min"
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
    private fun makePhoneCall() {
        if(binding.phone.text.isNotEmpty()){
            val phone_number = binding.phone.text.toString()
            val phone_intent = Intent(Intent.ACTION_CALL)
            phone_intent.data = Uri.parse("tel:+91$phone_number")
            startActivity(phone_intent)
        }else{
            Toast.makeText(this@DetailActivity,"Not Much data available for this location",Toast.LENGTH_SHORT).show()
        }
    }
}