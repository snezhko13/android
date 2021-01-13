package com.example.project1

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.project1.databinding.ActivityShopViewBinding
import com.example.project1.models.Shop
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.disposables.Disposable


class ShopViewActivity : AppCompatActivity(), MapFragment.OnCoordinatesSelectedListener {
    private val subscriptions = ArrayList<Disposable>()
    private lateinit var shop: Shop
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityShopViewBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation:Location
    private val permissionId = 12
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_view)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_view)
        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/shops")
        getPermissions()

    }

    override fun onStart() {
        super.onStart()
        val productId = intent.getStringExtra("shopId")
        if (productId != null) {
            databaseRef = databaseRef.child(productId)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    shop = dataSnapshot.getValue(Shop::class.java)!!
                    binding.titleTextBox.setText(shop.title)
                    binding.descriptionTextBox.setText(shop.description)
                    binding.radiusTextBox.setText(shop.radius.toString())
                    binding.coordinatesXTextBox.setText(shop.coordinateX.toString())
                    binding.coordinatesYTextBox.setText(shop.coordinateY.toString())
                    binding.deleteButton.visibility = View.VISIBLE
                    val bundle = Bundle()
                    bundle.putDouble("coordinateX", shop.coordinateX)
                    bundle.putDouble("coordinateY", shop.coordinateY)
                    initMapFragment(bundle)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    finish()
                }
            })

        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                lastLocation = location
                val bundle = Bundle()
                bundle.putDouble("coordinateX", lastLocation.latitude)
                bundle.putDouble("coordinateY", lastLocation.longitude)
                initMapFragment(bundle)
            }

        }

        binding.saveButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.saveButton.setTextColor(
            sharedPreferences.getInt(
                "buttonTextColor",
                Color.parseColor("#000000")
            )
        )
        binding.deleteButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.deleteButton.setTextColor(
            sharedPreferences.getInt(
                "buttonTextColor",
                Color.parseColor("#000000")
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.forEach(Disposable::dispose)
    }

    fun onSave(view: View) {
        val title = binding.titleTextBox.text.takeIf { it.isNotEmpty() }?.toString() ?: "Something"
        val description =
            binding.descriptionTextBox.text.takeIf { it.isNotEmpty() }?.toString() ?: "Something"
        val radius = binding.radiusTextBox.text.takeIf { it.isNotEmpty() }?.toString()?.toInt() ?: 0
        val coordinateX =
            binding.coordinatesXTextBox.text.takeIf { it.isNotEmpty() }?.toString()?.toDouble()
                ?: 0.0
        val coordinateY =
            binding.coordinatesYTextBox.text.takeIf { it.isNotEmpty() }?.toString()?.toDouble()
                ?: 0.0
        if (::shop.isInitialized) {
            databaseRef.setValue(
                Shop(
                    shop.id,
                    title,
                    description,
                    radius,
                    coordinateX,
                    coordinateY
                )
            )
        } else {
            val data = databaseRef.push()
            data.setValue(Shop(data.key!!, title, description, radius, coordinateX, coordinateY))
        }
        finish()
    }

    fun onDelete(view: View) {
        databaseRef.removeValue()
        finish()
    }

    override fun onAttachFragment(fragment: Fragment) {
        if (fragment is MapFragment) {
            fragment.setOnCoordinatesSelectedListener(this)
        }
    }

    fun addFragment(fragment: Fragment, addToBackStack: Boolean, tag: String?) {
        val manager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = manager.beginTransaction()
        if (addToBackStack) {
            ft.addToBackStack(tag)
        }
        ft.replace(R.id.container_frame_back, fragment, tag)
        ft.commitAllowingStateLoss()
    }

    override fun onCoordinatesSelected(position: LatLng) {
        binding.coordinatesXTextBox.setText(position.latitude.toString())
        binding.coordinatesYTextBox.setText(position.longitude.toString())
    }

    private fun initMapFragment(bundle: Bundle) {
        val mapFragment = MapFragment()
        mapFragment.arguments = bundle
        addFragment(mapFragment, false, "one")
    }

    private fun getPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionId
            )

        }
    }
}
