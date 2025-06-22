package no.realitylab.arface.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import no.realitylab.arface.R
import no.realitylab.arface.data.response.Order
import java.text.DecimalFormat

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private val orders = mutableListOf<Order>()
    val formatter = DecimalFormat("#,###")
    fun submitList(newList: List<Order>) {
        orders.clear()
        orders.addAll(newList)
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        private val productListContainer: LinearLayout = itemView.findViewById(R.id.productListContainer)

        fun bind(order: Order) {
            // Trạng thái đơn hàng
            tvOrderStatus.text = when (order.status.lowercase()) {
                "delivered" -> "Đã giao"
                "cancelled" -> "Đã hủy"
                "processing" -> "Đang xử lý"
                "shipped" -> "Đang giao"
                "pending" -> "Đang chờ xác nhận"
                else -> order.status
            }
            tvOrderStatus.setTextColor(
                when (order.status.lowercase()) {
                    "delivered" -> Color.parseColor("#4CAF50")
                    "cancelled" -> Color.parseColor("#F44336")
                    else -> Color.parseColor("#FF9800")
                }
            )

            // Xóa dữ liệu cũ nếu có
            productListContainer.removeAllViews()

            // Thêm từng sản phẩm trong đơn
            for (item in order.items) {
                val product = item.product

                val productView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.item_order_product, productListContainer, false)

                val imgThumb = productView.findViewById<ImageView>(R.id.imgThumb)
                val tvProductName = productView.findViewById<TextView>(R.id.tvProductName)
                val tvPrice = productView.findViewById<TextView>(R.id.tvPrice)
                val tvQuantity = productView.findViewById<TextView>(R.id.tvQuantity)
                val tvSize = productView.findViewById<TextView>(R.id.tvSize)

                tvProductName.text = product?.name ?: "Sản phẩm"
                tvPrice.text = "${formatter.format(product?.price ?: 0)} đ"
                tvQuantity.text = "Số lượng: ${item.quantity}"
                tvSize.text = "Kích cỡ: ${item.variationDetails.size ?: "-"}"

                Glide.with(itemView.context)
                    .load(product?.thumbnail)
                    .placeholder(R.drawable.test_image)
                    .into(imgThumb)

                productListContainer.addView(productView)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size
}
