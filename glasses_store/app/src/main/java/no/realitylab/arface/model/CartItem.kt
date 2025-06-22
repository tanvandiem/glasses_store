package no.realitylab.arface.model

import java.io.Serializable

data class CartItem(
    val product: Product,
    val variation: String,
    val quantity: Int,
    val price: Int,
    val thumbnail: String,
    val variationDetails: VariationDetails
): Serializable

data class VariationDetails(
    val size: String,
    val quantity: Int
): Serializable

