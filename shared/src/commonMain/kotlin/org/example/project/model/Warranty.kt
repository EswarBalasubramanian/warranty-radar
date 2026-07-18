package org.example.project.model

enum class ProductShape { Watch, Lamp, Laptop, Coffee, Other }

data class Warranty(
    val id: String,
    val productName: String,
    val store: String,
    val category: String,
    val purchaseDateLabel: String,
    val warrantyStatusLabel: String,
    val urgencyDays: Int?,
    val price: Double?,
    val shape: ProductShape,
    val warrantyEndDateLabel: String? = null
)
