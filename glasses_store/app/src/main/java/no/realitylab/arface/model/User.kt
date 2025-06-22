package no.realitylab.arface.model

import java.io.Serializable

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val contact: String?,
    val address: String?,
    val __v: Int
): Serializable
