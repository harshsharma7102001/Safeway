package com.world4tech.safeway.util

import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.world4tech.safeway.R
import java.io.IOException

class GetNearbyPlacesData : AsyncTask<Any?, String?, String?>() {
    var googlePlacesData: String? = null
    var mMap: GoogleMap? = null
    var url: String? = null
//    override fun doInBackground(vararg objects: Any): String? {
//        mMap = objects[0] as GoogleMap
//        url = objects[1] as String
//        val downloadURL = DownloadURL()
//        try {
//            googlePlacesData = downloadURL.readUrl(url)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return googlePlacesData
//    }

    override fun onPostExecute(s: String?) {
        val nearbyPlaceList: List<HashMap<String, String>>
        val parser = DataParser()
        nearbyPlaceList = parser.parse(s)
        Log.d("nearbyplacesdata", "called parse method")
        showNearbyPlaces(nearbyPlaceList)
    }

    private fun showNearbyPlaces(nearbyPlaceList: List<HashMap<String, String>>) {
        for (i in nearbyPlaceList.indices) {
            val markerOptions = MarkerOptions()
            val googlePlace = nearbyPlaceList[i]
            val placeName = googlePlace["place_name"]
            val vicinity = googlePlace["vicinity"]
            val reference = googlePlace["reference"]
            val lat = googlePlace["lat"]!!.toDouble()
            val lng = googlePlace["lng"]!!.toDouble()
            val latLng = LatLng(lat, lng)
            markerOptions.position(latLng)
            markerOptions.title("$placeName : $vicinity")
            markerOptions.snippet(reference)
            //            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_common))
            mMap!!.addMarker(markerOptions)

            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(10f))
        }
    }

    override fun doInBackground(vararg objects: Any?): String? {
        mMap = objects[0] as GoogleMap
        url = objects[1] as String
        val downloadURL = DownloadURL()
        try {
            googlePlacesData = downloadURL.readUrl(url)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return googlePlacesData
    }
}