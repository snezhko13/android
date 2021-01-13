package com.example.project1

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.project1.databinding.ActivityOptionsBinding
import java.util.regex.Pattern

class OptionsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_options)

        sharedPreferences = applicationContext.getSharedPreferences(
            applicationContext.packageName,
            Context.MODE_PRIVATE
        )
    }

    override fun onStart() {
        super.onStart()


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
    }

    fun saveChanges(view: View) {
        binding.errorMessage.visibility = View.GONE
        val hexPattern: Pattern = Pattern.compile("^#([A-Fa-f0-9]{6})\$")
        val backgroundColorText = binding.backgroundColorInput.text.toString()
        val textColorText = binding.textColorInput.text.toString()
        if (hexPattern.matcher(backgroundColorText).matches()) {
            val color = Color.parseColor(backgroundColorText)
            binding.saveButton.setBackgroundColor(color)
            sharedPreferences.edit().putInt("buttonBackgroundColor", color).apply()
        } else {
            binding.errorMessage.visibility = View.VISIBLE
        }
        if (hexPattern.matcher(textColorText).matches()) {
            val color = Color.parseColor(textColorText)
            binding.saveButton.setTextColor(color)
            sharedPreferences.edit().putInt("buttonTextColor", color).apply()
        } else {
            binding.errorMessage.visibility = View.VISIBLE
        }
    }
}
