package com.example.project1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment() {
    internal lateinit var callback: OnCoordinatesSelectedListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment?
        val coordinateX = arguments!!.getDouble("coordinateX")
        val coordinateY = arguments!!.getDouble("coordinateY")

        mapFragment!!.getMapAsync { mMap ->
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.clear()
            val googlePlex = CameraPosition.builder()
                .target(LatLng(coordinateX, coordinateY))
                .zoom(10f)
                .build()
            mMap.addMarker(MarkerOptions().position(LatLng(coordinateX, coordinateY)))
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(googlePlex))
            mMap.setOnMapClickListener { point ->
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(point))
                callback.onCoordinatesSelected(point)
            }
        }
        return rootView
    }

    fun setOnCoordinatesSelectedListener(callback: OnCoordinatesSelectedListener) {
        this.callback = callback
    }

    interface OnCoordinatesSelectedListener {
        fun onCoordinatesSelected(position: LatLng)
    }
}
