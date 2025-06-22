package no.realitylab.arface.model

import java.io.Serializable

data class Variation(
    val size: String,
    val quantity: Int,
    val _id: String
): Serializable

data class Product(
    val _id: String,
    val name: String,
    val description: String,
    val price: Int,
    val discountPrice: Int,
    val stock: Int,
    val brand: String,
    val gender: String,
    val category: String,
    val subCategory: String,
    val variations: List<Variation>,
    val thumbnail: String,
    val images: List<String>,
    val ratings: Float,
    val reviews: List<String>
): Serializable
