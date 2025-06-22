package no.realitylab.arface.data.remote

import no.realitylab.arface.data.request.AddToCartRequest
import no.realitylab.arface.data.response.CartResponse
import no.realitylab.arface.data.request.LoginRequest
import no.realitylab.arface.data.response.LoginResponse
import no.realitylab.arface.data.response.OrderCreatResponse
import no.realitylab.arface.data.request.OrderRequest
import no.realitylab.arface.data.response.OrderResponse
import no.realitylab.arface.data.response.ProductResponse
import no.realitylab.arface.data.request.QuantityRequest
import no.realitylab.arface.data.request.RegisterRequest
import no.realitylab.arface.data.response.RegisterResponse
import no.realitylab.arface.data.response.SearchResponse
import no.realitylab.arface.data.request.UpdateProfileRequest
import no.realitylab.arface.data.response.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/signup")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @GET("product/all") // ví dụ: "api/products"
    suspend fun getAllProducts(): Response<ProductResponse>

    @GET("user/profile")
    fun getUserProfile(
        @Header("Authorization") token: String
    ): Call<UserResponse>

    @GET("cart/")
    fun getCart(
        @Header("Authorization") token: String
    ): Call<CartResponse>

    @POST("cart/add")
    fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequest
    ): Call<Void>


    @DELETE("cart/remove/{productId}/{variation}")
    fun deleteCartItem(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
        @Path("variation") variation: String
    ): Call<Void>

    @DELETE("cart/remove/all")
    fun deleteAllCartItem(
        @Header("Authorization") authHeader: String,
    ): Call<Void>

    @PUT("cart/update/{productId}/{variation}")
    fun updateCartItem(
        @Header("Authorization") authHeader: String,
        @Path("productId") productId: String,
        @Path("variation") variation: String,
        @Body quantityBody: QuantityRequest
    ): Call<Void>


    @GET("product/search")
    suspend fun searchProducts(
        @Query("q") query: String
    ): Response<SearchResponse>

    @POST("order/create") // endpoint tạo order
    fun createOrder(
        @Header("Authorization") authToken: String,
        @Body orderRequest: OrderRequest
    ): Call<OrderCreatResponse>

    @GET("order/myorders")
    fun getAllOrders(
        @Header("Authorization") authHeader: String
    ): Call<OrderResponse>


    @PUT("user/update")
    fun updateProfile(
        @Header("Authorization") authHeader: String,
        @Body request: UpdateProfileRequest
    ): Call<Void>


}
