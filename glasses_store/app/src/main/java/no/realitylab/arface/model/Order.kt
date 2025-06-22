package no.realitylab.arface.model

import java.io.Serializable

data class Order(
    val _id: String,
    val user: String,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val address: String,
    val phoneNumber: String,
    val createdAt: String
): Serializable

data class OrderItem(
    val _id: String,
    val product: Product,
    val quantity: Int,
    val variationDetails: VariationDetails
): Serializable
