package no.realitylab.arface.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import no.realitylab.arface.R
import no.realitylab.arface.model.CartItem
import java.text.DecimalFormat

class CartAdapter(private val items: List<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    val formatter = DecimalFormat("#,###")
    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumb: ImageView = itemView.findViewById(R.id.imgThumb)
        val name: TextView = itemView.findViewById(R.id.tvProductName)
        val quantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.product.name
        holder.quantity.text = "Số lượng: ${item.quantity}"
        holder.price.text = "Giá: $${formatter.format(item.price * item.quantity)}"

        Glide.with(holder.thumb.context)
            .load(item.thumbnail)
            .into(holder.thumb)
    }

    override fun getItemCount() = items.size
}
