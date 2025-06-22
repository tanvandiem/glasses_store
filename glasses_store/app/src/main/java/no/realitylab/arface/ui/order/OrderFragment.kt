package no.realitylab.arface.ui.order

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import no.realitylab.arface.R
import no.realitylab.arface.adapter.OrderAdapter
import no.realitylab.arface.data.remote.RetrofitClient
import no.realitylab.arface.data.response.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class OrderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_order, container, false)

        recyclerView = view.findViewById(R.id.rvOrders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderAdapter = OrderAdapter()
        recyclerView.adapter = orderAdapter

        fetchOrders()

        return view
    }

    private fun fetchOrders() {
        val token = "Bearer " + getToken()

        RetrofitClient.instance.getAllOrders(token)
            .enqueue(object : Callback<OrderResponse> {
                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val orders = response.body()!!.data.orders

                        // Sắp xếp theo trạng thái:
                        // 1. Pending -> trên cùng
                        // 2. Shipped -> giữa
                        // 3. Delivered -> cuối
                        val sortedOrders = orders.sortedWith(
                            compareBy(
                                { getStatusPriority(it.status) },
                                { -parseDate(it.createdAt) } // Mới nhất trước
                            )
                        )

                        orderAdapter.submitList(sortedOrders)
                    } else {
                        Toast.makeText(requireContext(), "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Lỗi: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Ưu tiên trạng thái
    private fun getStatusPriority(status: String): Int {
        return when (status) {
            "Pending" -> 1   // Chờ xác nhận -> cao nhất
            "Shipped" -> 2   // Đang giao -> trung bình
            "Delivered" -> 3 // Đã giao -> thấp nhất
            else -> 4        // Khác
        }
    }

    // Parse ngày giờ từ createdAt
    private fun parseDate(dateString: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            dateFormat.parse(dateString)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getToken(): String {
        val sharedPrefs = requireContext().getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
        return sharedPrefs.getString("TOKEN", "") ?: ""
    }
}
