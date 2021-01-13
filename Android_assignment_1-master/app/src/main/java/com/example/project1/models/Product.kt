package com.example.project1.models

data class Product(
    var id: String = "NOT A KEY",
    var title: String? = "Untitled",
    var price: Double = 0.0,
    var amount: Int = 0,
    var purchased: Boolean = false
)