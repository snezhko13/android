package com.example.project1

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.project1.databinding.ActivityProductViewBinding
import com.example.project1.models.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.disposables.Disposable


class ProductViewActivity : AppCompatActivity() {
    private val subscriptions = ArrayList<Disposable>()
    private lateinit var product: Product
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityProductViewBinding
    private lateinit var databaseRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_view)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_view)
        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
        databaseRef = FirebaseDatabase.getInstance()
            .getReference("users/" + FirebaseAuth.getInstance().currentUser!!.uid + "/products")
    }

    override fun onStart() {
        super.onStart()
        val productId = intent.getStringExtra("productId")
        if (productId != null) {
            databaseRef = databaseRef.child(productId)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    product = dataSnapshot.getValue(Product::class.java)!!
                    binding.titleTextBox.setText(product.title)
                    binding.priceTextBox.setText(product.price.toString())
                    binding.amountTextBox.setText(product.amount.toString())
                    binding.isPurchasedCheckBox.isChecked = product.purchased
                    binding.deleteButton.visibility = View.VISIBLE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    finish()
                }
            })

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
        val price =
            binding.priceTextBox.text.takeIf { it.isNotEmpty() }?.toString()?.toDouble() ?: 0.0
        val amount = binding.amountTextBox.text.takeIf { it.isNotEmpty() }?.toString()?.toInt() ?: 0
        val purchased = binding.isPurchasedCheckBox.isChecked
        if (::product.isInitialized) {
            databaseRef.setValue(Product(product.id, title, price, amount, purchased))
        } else {
            val data = databaseRef.push()
            data.setValue(Product(data.key!!, title, price, amount, purchased))
            val sendIntent: Intent = Intent().apply {
                action = "com.example.android2.MyReceiver"
                component = ComponentName(
                    "com.example.android2",
                    "com.example.android2.MyReceiver"
                )
                putExtra("id", data.key!!)
                putExtra("title", title)
            }
            sendBroadcast(sendIntent, "com.example.project1")
        }
        finish()
    }

    fun onDelete(view: View) {
        databaseRef.removeValue()
        finish()
    }
}
