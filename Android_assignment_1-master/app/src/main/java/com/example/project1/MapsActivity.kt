package com.example.project1

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.project1.models.Shop
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var shops = ArrayList<Shop>()
    private lateinit var mMap: GoogleMap
    private lateinit var databaseRef: DatabaseReference
    private var markers = ArrayList<Marker>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0f))
        }
        setUpDatabase()
    }

    public override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        prepareMap()
    }

    private fun prepareMap() {
        if (::mMap.isInitialized) {
            markers.onEach { marker -> marker.remove() }
            markers = ArrayList()
            shops.onEach { shop ->
                val latlng = LatLng(shop.coordinateX, shop.coordinateY)
                markers.add(
                    mMap.addMarker(
                        MarkerOptions().position(latlng).title(shop.title)
                    )
                )
                val circleOptions = CircleOptions()
                    .center(latlng)
                    .radius(shop.radius.toDouble())
                    .fillColor(0x40ff0000)
                    .strokeColor(Color.TRANSPARENT)
                    .strokeWidth(2f)
                mMap.addCircle(circleOptions)
            }
            mLocationRequest = LocationRequest()
            mLocationRequest.interval = 120000
            mLocationRequest.fastestInterval = 120000
            mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
            } else {
                checkLocationPermission()
            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(
                            this@MapsActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationClient.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()
                        )
                        mMap.isMyLocationEnabled = true
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun setUpDatabase() {
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/shops")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val p = dataSnapshot.children.mapNotNull { child -> child.getValue(Shop::class.java) }
                shops.clear()
                shops.addAll(p)
                prepareMap()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE", "Failed to read value.", error.toException())
            }
        })
    }

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                val location = locationList.last()
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude())
                mLastLocation = location
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }
                val latLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11.0F))
            }
        }
    }

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
