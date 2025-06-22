package no.realitylab.arface.data.response

import com.google.gson.annotations.SerializedName
import no.realitylab.arface.model.VariationDetails

data class OrderItemResponse(
    val product: String,
    val quantity: Int,
    val variation: String,
    val variationDetails: VariationDetails,
    @SerializedName("_id") val id: String
)

data class OrderDataResponse(
    val user: String,
    val items: List<OrderItemResponse>,
    val totalAmount: Int,
    val status: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val paymentId: String?,
    val address: String,
    val phoneNumber: String,
    @SerializedName("_id") val id: String,
    val createdAt: String,
    val updatedAt: String,
    @SerializedName("__v") val version: Int
)

data class OrderCreatResponse(
    val success: Boolean,
    val message: String,
    val data: OrderDataResponse?
)