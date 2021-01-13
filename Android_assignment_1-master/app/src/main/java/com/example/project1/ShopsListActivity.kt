package com.example.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.project1.adapters.ShopArrayAdapter
import com.example.project1.databinding.ActivityShopsListBinding
import com.example.project1.models.Shop
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.disposables.Disposable

class ShopsListActivity : AppCompatActivity() {
    private var shops = ArrayList<Shop>()
    private lateinit var adapter: ShopArrayAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityShopsListBinding
    private lateinit var databaseRef: DatabaseReference
    private val subscriptions = ArrayList<Disposable>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shops_list)
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/shops")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shops_list)
        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        adapter = ShopArrayAdapter(this, R.layout.product_row, shops)
        binding.listOfShops.adapter = adapter
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    val data = databaseRef.push()
                    data.setValue(Shop(data.key!!, "Tesco", "Best shop ever", 100, 0.0, 0.0))
                }
                val p =
                    dataSnapshot.children.mapNotNull { child -> child.getValue(Shop::class.java) }
                shops.clear()
                shops.addAll(p)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.addShopButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.addShopButton.setTextColor(
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

    fun onAddShop(view: View) {
        startActivity(Intent(this, ShopViewActivity::class.java))
    }
}
