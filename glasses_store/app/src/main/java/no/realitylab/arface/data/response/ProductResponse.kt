package no.realitylab.arface.data.response

import no.realitylab.arface.model.Product

data class ProductResponse(
    val success: Boolean,
    val message: String,
    val data: ProductData
)

data class ProductData(
    val TotalProducts: Int,
    val allProducts: List<Product>?
)
