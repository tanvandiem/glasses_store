package no.realitylab.arface.data.response

import no.realitylab.arface.model.Product

data class SearchResponse(
    val success: Boolean,
    val message: String,
    val data: List<Product>
)
