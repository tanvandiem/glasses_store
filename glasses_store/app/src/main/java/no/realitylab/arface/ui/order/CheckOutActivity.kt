package no.realitylab.arface.ui.order

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import no.realitylab.arface.R
import no.realitylab.arface.data.remote.RetrofitClient
import no.realitylab.arface.model.Cart
import no.realitylab.arface.data.response.OrderCreatResponse
import no.realitylab.arface.data.request.OrderItem1
import no.realitylab.arface.data.request.OrderRequest
import no.realitylab.arface.model.VariationDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class CheckOutActivity : AppCompatActivity() {

    private lateinit var orderSummaryLayout: LinearLayout
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var addressEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var totalCostTextView: TextView
    val formatter = DecimalFormat("#,###.##")

    private var cart: Cart? = null

    // Payment method data class với code và tên hiển thị
    data class PaymentMethod(val code: String, val displayName: String)

    private val paymentMethods = listOf(
        PaymentMethod("COD", "Thanh toán khi nhận hàng"),
        PaymentMethod("Card", "Thẻ")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        orderSummaryLayout = findViewById(R.id.orderSummaryLayout)
        paymentMethodSpinner = findViewById(R.id.paymentMethodSpinner)
        addressEditText = findViewById(R.id.addressEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        confirmButton = findViewById(R.id.confirmButton)
        totalCostTextView = findViewById(R.id.tvTotalCost)

        cart = intent.getSerializableExtra("cart_data") as? Cart

        if (cart == null) {
            Toast.makeText(this, "Không có dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupPaymentSpinner()
        populateOrderSummary()

        confirmButton.setOnClickListener {
            placeOrder()
        }
    }

    private fun setupPaymentSpinner() {
        val adapter = object : ArrayAdapter<PaymentMethod>(
            this,
            android.R.layout.simple_spinner_item,
            paymentMethods
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as? TextView)?.setTextColor(resources.getColor(android.R.color.black))
                (view as? TextView)?.text = paymentMethods[position].displayName
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? TextView)?.setTextColor(resources.getColor(android.R.color.white))
                (view as? TextView)?.text = paymentMethods[position].displayName
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paymentMethodSpinner.adapter = adapter
    }

    private fun populateOrderSummary() {
        cart?.items?.forEach { item ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_checkout_product, orderSummaryLayout, false)

            val imageView = itemView.findViewById<ImageView>(R.id.productImageV)
            val nameTextView = itemView.findViewById<TextView>(R.id.productNameTextV)
            val variationTextView = itemView.findViewById<TextView>(R.id.productVariationTextV)
            val priceTextView = itemView.findViewById<TextView>(R.id.productPriceTextV)
            val quantityTextView = itemView.findViewById<TextView>(R.id.productQuantityTextV)

            Glide.with(this)
                .load(item.product.thumbnail)
                .into(imageView)

            nameTextView.text = item.product.name
            variationTextView.text = "Kích thước: ${item.variationDetails?.size ?: "Không xác định"}"
            priceTextView.text = "Giá: đ${formatter.format(item.price)}"
            quantityTextView.text = "Số lượng: ${item.quantity}"

            orderSummaryLayout.addView(itemView)
        }

        totalCostTextView.text = "Tổng cộng: đ${formatter.format(cart?.total ?: 0)}"
    }

    private fun placeOrder() {
        val selectedPayment = (paymentMethodSpinner.selectedItem as PaymentMethod).code
        val address = addressEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ và số điện thoại", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("auth_preferences", MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)
        val token = sharedPreferences.getString("TOKEN", null)

        if (userId.isNullOrEmpty() || token.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val orderItems = cart?.items?.map { item ->
            OrderItem1(
                product = item.product._id,
                variation = item.variation,
                quantity = item.quantity,
                price = item.price,
                variationDetails = VariationDetails(
                    size = item.variationDetails?.size ?: "",
                    quantity = item.variationDetails?.quantity ?: 0
                )
            )
        } ?: emptyList()

        val orderRequest = OrderRequest(
            user = userId,
            items = orderItems,
            totalAmount = cart?.total ?: 0,
            paymentMethod = selectedPayment,
            address = address,
            phoneNumber = phone
        )

        Log.d("CheckOutActivity", "OrderRequest gửi lên: $orderRequest")

        val apiService = RetrofitClient.instance

        apiService.createOrder("Bearer $token", orderRequest)
            .enqueue(object : Callback<OrderCreatResponse> {
                override fun onResponse(
                    call: Call<OrderCreatResponse>,
                    response: Response<OrderCreatResponse>
                ) {
                    Log.d("CheckOutActivity", "Phản hồi createOrder: $response")

                    if (response.isSuccessful && response.body()?.success == true) {
                        apiService.deleteAllCartItem("Bearer $token")
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    Toast.makeText(this@CheckOutActivity, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                                    setResult(RESULT_OK)
                                    finish()
                                }

                                override fun onFailure(call: Call<Void>, t: Throwable) {
                                    Toast.makeText(this@CheckOutActivity, "Lỗi khi xóa giỏ hàng: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            })
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Không rõ lỗi"
                        Log.e("CheckOutActivity", "Lỗi phản hồi: $errorMsg")
                        Toast.makeText(this@CheckOutActivity, "Đặt hàng thất bại: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderCreatResponse>, t: Throwable) {
                    Log.e("CheckOutActivity", "Lỗi mạng: ${t.localizedMessage}")
                    Toast.makeText(this@CheckOutActivity, "Lỗi kết nối: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
