package com.example.project1
import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.project1.databinding.ActivityMainBinding
import com.example.project1.models.Shop
import com.example.project1.ui.login.LoginActivity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    lateinit var geofencingClient: GeofencingClient
    private lateinit var databaseRef: DatabaseReference
    private var shops = ArrayList<Shop>()
    private var geoFenceList = ArrayList<Geofence>()

    companion object DatabaseSetup {
        var database2: FirebaseDatabase? = null
        val MY_PERMISSIONS_REQUEST_INTERNET = 99
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        database2 = FirebaseDatabase.getInstance()
        FirebaseAuth.getInstance().addAuthStateListener { fireBaseAuth ->
            if (fireBaseAuth.currentUser == null) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
        setUpDatabase()
        getPermissions()
        checkIntentPermission()
        geofencingClient = LocationServices.getGeofencingClient(this)

    }

    override fun onStart() {
        super.onStart()
        binding.goToProductListButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.goToProductListButton.setTextColor(
            sharedPreferences.getInt(
                "buttonTextColor",
                Color.parseColor("#000000")
            )
        )
        binding.goToOptionsButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.goToOptionsButton.setTextColor(
            sharedPreferences.getInt(
                "buttonTextColor",
                Color.parseColor("#000000")
            )
        )
    }

    fun goToProductList(view: View) {
        startActivity(Intent(this, ProductListActivity::class.java))
    }

    fun goToOptions(view: View) {
        startActivity(Intent(this, OptionsActivity::class.java))
    }

    fun goToMap(view: View) {
        startActivity(Intent(this, MapsActivity::class.java))
    }

    fun goToShopList(view: View) {
        startActivity(Intent(this, ShopsListActivity::class.java))
    }
    fun goToWeb(view: View) {
        startActivity(Intent(this, WebActivity::class.java))
    }

    fun logout(view: View) {
        FirebaseAuth.getInstance().signOut()
    }

    private fun setUpDatabase() {
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/shops")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val p = dataSnapshot.children.mapNotNull { child -> child.getValue(Shop::class.java) }
                shops.clear()
                shops.addAll(p)
                p.onEach { shop -> addGeoFence(shop) }
                if(!geoFenceList.isEmpty()){
                setUpGeoFence()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE", "Failed to read value.", error.toException())
            }
        })
    }

    private fun addGeoFence(shop: Shop) {
        geoFenceList.add(
            Geofence.Builder()
                .setRequestId(shop.id)
                .setCircularRegion(shop.coordinateX, shop.coordinateY, shop.radius.toFloat())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Long.MAX_VALUE)
                .build()
        )
    }

    private fun getGeoFencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geoFenceList)
        }.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
                16
            )

        }
    }
    private fun setUpGeoFence(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        geofencingClient.addGeofences(getGeoFencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
            }
            addOnFailureListener {
            }
        }
    }

    private fun checkIntentPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.INTERNET
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.INTERNET
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("INTERNET")
                    .setMessage("INTERNET")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.INTERNET),
                            MY_PERMISSIONS_REQUEST_INTERNET
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.INTERNET),
                    MY_PERMISSIONS_REQUEST_INTERNET
                )
            }
        }
    }
}