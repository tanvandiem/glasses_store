package no.realitylab.arface.data.request

import no.realitylab.arface.model.VariationDetails

data class OrderRequest(
    val user: String,
    val items: List<OrderItem1>,
    val totalAmount: Int,
    val paymentMethod: String,
    val address: String,
    val phoneNumber: String
)

data class OrderItem1(
    val product: String,
    val variation: String,
    val quantity: Int,
    val price: Int,
    val variationDetails: VariationDetails
)





