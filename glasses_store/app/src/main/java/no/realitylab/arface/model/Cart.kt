package no.realitylab.arface.model

import java.io.Serializable

data class Cart(
    val _id: String,
    val user: String,
    val items: List<CartItem>,
    val total: Int
): Serializable
