package no.realitylab.arface.ui.cart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import no.realitylab.arface.R
import no.realitylab.arface.ui.order.CheckOutActivity
import no.realitylab.arface.data.remote.RetrofitClient
import no.realitylab.arface.model.CartItem
import no.realitylab.arface.data.response.CartResponse
import no.realitylab.arface.data.request.QuantityRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class CartFragment : Fragment() {

    private lateinit var itemsContainer: LinearLayout
    private lateinit var tvTotalCost: TextView
    val formatter = DecimalFormat("#,###.##")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        itemsContainer = view.findViewById(R.id.itemsContainer)
        tvTotalCost = view.findViewById(R.id.tvTotalCost)
        val btnCheckout = view.findViewById<Button>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            openCheckoutScreen()
        }

        fetchCartData()

        return view
    }

    private fun openCheckoutScreen() {
        val sharedPreferences = requireContext().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"
        val api = RetrofitClient.instance
        val call = api.getCart(authHeader)

        call.enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful) {
                    val cart = response.body()?.cart
                    if (cart != null) {
                        val intent = Intent(requireContext(), CheckOutActivity::class.java)
                        intent.putExtra("cart_data", cart) // Gửi nguyên giỏ hàng
                        startActivityForResult(intent, 1) // Request để nhận lại kết quả từ Checkout
                    }
                } else {
                    Toast.makeText(requireContext(), "Không lấy được giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Xử lý kết quả trả về từ màn hình Checkout
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Màn hình Checkout trả về kết quả, ví dụ giỏ hàng đã được thanh toán hoặc cập nhật
            fetchCartData() // Lấy lại dữ liệu giỏ hàng mới
        }
    }

    private fun fetchCartData() {
        val sharedPreferences = requireContext().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"
        val api = RetrofitClient.instance
        val call = api.getCart(authHeader)

        call.enqueue(object : Callback<CartResponse> {
            override fun onResponse(call: Call<CartResponse>, response: Response<CartResponse>) {
                if (response.isSuccessful) {
                    val cart = response.body()?.cart
                    if (cart != null) {
                        displayCartItems(cart.items)
                        tvTotalCost.text = "đ${formatter.format(cart.total)}"

                    }
                } else {
                    Toast.makeText(requireContext(), "Không lấy được dữ liệu giỏ hàng", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CartResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayCartItems(items: List<CartItem>) {
        itemsContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for ((index, item) in items.withIndex()) {
            val itemView = inflater.inflate(R.layout.cart_item, itemsContainer, false)

            val imgThumb = itemView.findViewById<ImageView>(R.id.imgThumb)
            val tvName = itemView.findViewById<TextView>(R.id.tvProductName)
            val tvQuantity = itemView.findViewById<TextView>(R.id.tvQuantity)
            val tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
            val tvSize = itemView.findViewById<TextView>(R.id.tvSize)
            val btnDelete = itemView.findViewById<ImageButton>(R.id.ivCashIcon)
            val btnDecrease = itemView.findViewById<ImageButton>(R.id.btnDecrease)
            val btnIncrease = itemView.findViewById<ImageButton>(R.id.btnIncrease)

            // Gán dữ liệu
            tvName.text = item.product.name
            tvQuantity.text = "${item.quantity}"
            tvPrice.text = "đ${formatter.format(item.price * item.quantity)}"
            tvSize.text = "${item.variationDetails.size}"

            Glide.with(requireContext())
                .load(item.thumbnail)
                .into(imgThumb)

            // Khi click tăng số lượng
            btnIncrease.setOnClickListener {
                val newQuantity = item.quantity + 1
                updateCartItemQuantity(item.product._id, item.variation, newQuantity)
            }

            // Khi click giảm số lượng
            btnDecrease.setOnClickListener {
                val newQuantity = item.quantity - 1
                if (newQuantity >= 1) {
                    updateCartItemQuantity(item.product._id, item.variation, newQuantity)
                } else {
                    Toast.makeText(requireContext(), "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show()
                }
            }

            // Khi click xóa sản phẩm
            btnDelete.setOnClickListener {
                deleteCartItem(item.product._id, item.variation)
            }

            itemsContainer.addView(itemView)

            if (index < items.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).apply {
                        setMargins(16, 8, 16, 8)
                    }
                    setBackgroundColor(Color.parseColor("#EEEEEE"))
                }
                itemsContainer.addView(divider)
            }
        }
    }

    private fun deleteCartItem(productId: String, variation: String) {
        val sharedPreferences = requireContext().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"
        val call = RetrofitClient.instance.deleteCartItem(authHeader, productId, variation)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                    fetchCartData()
                } else {
                    Toast.makeText(requireContext(), "Xóa thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCartItemQuantity(productId: String, variation: String, newQuantity: Int) {
        val sharedPreferences = requireContext().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("TOKEN", null)

        if (token == null) {
            Toast.makeText(requireContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"
        val quantityBody = QuantityRequest(newQuantity)

        RetrofitClient.instance.updateCartItem(authHeader, productId, variation, quantityBody)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        fetchCartData()
                    } else {
                        Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi mạng: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
