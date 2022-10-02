package com.world4tech.safeway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.world4tech.safeway.adapter.MyAdapter
import com.world4tech.safeway.databinding.ActivityDestinationBinding
import com.world4tech.homework.database.Notes
import com.world4tech.homework.database.NotesViewModel
import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal

class DestinationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var dest:LatLng
    private lateinit var mMap: GoogleMap
    private lateinit var binding:ActivityDestinationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private  var temporaryLoc: LatLng?=null
    private  lateinit var lastLocation: Location
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }
    lateinit var viewModel: NotesViewModel
    private lateinit var mViewModel: NotesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkInternet()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        viewModel = ViewModelProvider(this).get(
            NotesViewModel::class.java)
        val adapter = MyAdapter(this)
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDiT71bT7Ql0s50AEkL4LjF98HdrIJLrag")
        }
        val newRecyclerView: RecyclerView = findViewById(R.id.recentData)
        var decision=0
        newRecyclerView.adapter=adapter
        newRecyclerView.layoutManager= LinearLayoutManager(this)
        mViewModel= ViewModelProvider(this)[NotesViewModel::class.java]
        mViewModel.allNotes.observe(this, Observer { list ->
            adapter.setData(list)
            decision = list.size
//            UpdateUi(decision)
            Log.d("TAG","list Size: ${list.size}")})
        //-----------------------------------------Recent Data added
        val autocompleteSupportFragment1 =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment1) as AutocompleteSupportFragment?
        autocompleteSupportFragment1!!.setPlaceFields(
            listOf(

                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL

            )
        )
        // Display the fetched information after clicking on one of the options
        autocompleteSupportFragment1.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                try{
                    val name = place.name
                    val address = place.address
                    val phone = place.phoneNumber.toString()
                    val latlng = place.latLng
                    val latitude = latlng?.latitude
                    val longitude = latlng?.longitude
                    val isOpenStatus: String = if (place.isOpen == true) {
                        "Open"
                    } else {
                        "Closed"
                    }
                    val rating = place.rating
                    val userRatings = place.userRatingsTotal
                    val destloc = LatLng(latitude!!,longitude!!)
                    dest = destloc
                    println("Latitude and longitude of location is: ${dest.latitude} and ${dest.longitude}")

                    val totalData = Notes(name,address,latitude.toString(),longitude.toString())
                    viewModel.addNotes(totalData)
                    val i = Intent(this@DestinationActivity,MapsActivity::class.java)
                    i.putExtra("lat","${dest.latitude}")
                    i.putExtra("lon","${dest.longitude}")
                    startActivity(i)
                }catch (e:Exception){
                    Toast.makeText(this@DestinationActivity,"Data not available",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext, "Some error occurred", Toast.LENGTH_SHORT).show()
            }
        })
        binding.fuelStaion.setOnClickListener {
//            val i = Intent(this,MapsActivity::class.java)
//            i.putExtra("location","petrolpump")
//            startActivity(i)
        }
        binding.garages.setOnClickListener { }
        binding.hospitals.setOnClickListener {  }
        binding.hotels.setOnClickListener {  }

        //Swipe gestures at bottom
        BottomSheetBehavior.from(binding.layout).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
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
        }catch (e:Exception) {
            Log.d("TAG",e.message.toString())
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
//                        Speedometer Setting
//                        ---------------------------------------------
                    println("Lat long is ${location.latitude} and ${location.longitude}")
                        placeMarkerOnMap(temporaryLoc!!)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,15f))
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
        val markerOptions = MarkerOptions().position(currentLatLong)
            .title("My Locations")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.currentposition_marker))
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
            Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS,Manifest.permission.CALL_PHONE),
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
                PackageManager.PERMISSION_GRANTED && grantResults[1]== PackageManager.PERMISSION_GRANTED && grantResults[2]==PackageManager.PERMISSION_GRANTED
            ) {
                setupMap()
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onMarkerClick(p0: Marker)= false

//    fun UpdateUi(decision: Int) {
//        if (decision>0){
//            empty_image.visibility= View.INVISIBLE
//        }else{
//            empty_image.visibility = View.VISIBLE
//        }
//    }

    //Check Internet
    private fun checkInternet() {
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder(
            this,
            lifecycle
        ).apply {
            dialogProperties.apply {
                connectionCallback = object : ConnectionCallback { // Optional
                    override fun hasActiveConnection(hasActiveConnection: Boolean) {
                        // ...
                    }
                }

                cancelable = false // Optional
                noInternetConnectionTitle = "No Internet" // Optional
                noInternetConnectionMessage =
                    "Check your Internet connection and try again." // Optional
                showInternetOnButtons = true // Optional
                pleaseTurnOnText = "Please turn on" // Optional
                wifiOnButtonText = "Wifi" // Optional
                mobileDataOnButtonText = "Mobile data" // Optional

                onAirplaneModeTitle = "No Internet" // Optional
                onAirplaneModeMessage = "You have turned on the airplane mode." // Optional
                pleaseTurnOffText = "Please turn off" // Optional
                airplaneModeOffButtonText = "Airplane mode" // Optional
                showAirplaneModeOffButtons = true // Optional
            }
        }.build()
    }
}