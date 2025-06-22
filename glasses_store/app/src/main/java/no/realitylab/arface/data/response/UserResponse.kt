package no.realitylab.arface.data.response

import no.realitylab.arface.model.User

data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: User
)