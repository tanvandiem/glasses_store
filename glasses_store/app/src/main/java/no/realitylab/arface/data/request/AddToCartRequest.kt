package no.realitylab.arface.data.request

data class AddToCartRequest(
    val productId: String,
    val variationId: String,
    val quantity: Int
)
