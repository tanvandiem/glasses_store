package no.realitylab.arface.data.response

import no.realitylab.arface.model.Cart

data class CartResponse(
    val success: Boolean,
    val cart: Cart
)