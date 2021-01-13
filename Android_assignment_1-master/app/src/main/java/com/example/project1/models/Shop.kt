package com.example.project1.models

data class Shop(
    var id: String = "NOT A KEY",
    var title: String? = "Untitled",
    var description: String? = "Untitled",
    var radius: Int = 0,
    var coordinateX: Double = 0.0,
    var coordinateY: Double = 0.0
)