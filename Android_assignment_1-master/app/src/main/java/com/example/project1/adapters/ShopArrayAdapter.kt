package com.example.project1.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.project1.ProductViewActivity
import com.example.project1.R
import com.example.project1.ShopViewActivity
import com.example.project1.models.Shop
import com.google.firebase.database.DatabaseReference


class ShopArrayAdapter(
    context: Context,
    resource: Int,
    objects: ArrayList<out Shop>
) :
    ArrayAdapter<Shop>(context, resource, objects) {
    private val shops: List<Shop> = objects
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val shop = shops[position]
        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.shop_row, null)
        val title = view.findViewById(R.id.title) as TextView
        val description = view.findViewById(R.id.description) as TextView
        val radius = view.findViewById(R.id.radius) as TextView
        val coordinatesX = view.findViewById(R.id.coordinatesX) as TextView
        val coordinatesY = view.findViewById(R.id.coordinatesY) as TextView
        title.text = shop.title
        description.text = shop.description
        radius.text = shop.radius.toString()
        coordinatesX.text = shop.coordinateX.toString()
        coordinatesY.text = shop.coordinateY.toString()

        view.setOnClickListener {
            val intent = Intent(context, ShopViewActivity::class.java)
            intent.putExtra("shopId", shop.id)
            context.startActivity(intent)
        }
        return view
    }
}