package no.realitylab.arface.data.response

import no.realitylab.arface.model.User

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: User,
    val token: String
)




