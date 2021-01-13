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
import com.example.project1.adapters.ProductArrayAdapter
import com.example.project1.databinding.ActivityProductListBinding
import com.example.project1.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.disposables.Disposable


class ProductListActivity : AppCompatActivity() {

    private var products = ArrayList<Product>()
    private lateinit var adapter: ProductArrayAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityProductListBinding
    private lateinit var databaseRef: DatabaseReference
    private val subscriptions = ArrayList<Disposable>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/products")
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_list)
        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        adapter = ProductArrayAdapter(this, R.layout.product_row, products, databaseRef)
        binding.listOfProducts.adapter = adapter
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    var data = databaseRef.push()
                    data.setValue(Product(data.key!!, "Piano", 200.0, 20))
                    data = databaseRef.push()
                    data.setValue(Product(data.key!!, "Bread", 2.5, 5))
                    data = databaseRef.push()
                    data.setValue(Product(data.key!!, "Phone", 150.50, 1))
                }
                val p =
                    dataSnapshot.children.mapNotNull { child -> child.getValue(Product::class.java) }
                products.clear()
                products.addAll(p)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("DATABASE", "Failed to read value.", error.toException())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.addProductButton.setBackgroundColor(
            sharedPreferences.getInt(
                "buttonBackgroundColor",
                Color.parseColor("#FFFFFF")
            )
        )
        binding.addProductButton.setTextColor(
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

    fun onAddProduct(view: View) {
        startActivity(Intent(this, ProductViewActivity::class.java))
    }

}
