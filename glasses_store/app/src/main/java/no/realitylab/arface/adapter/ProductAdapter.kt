package no.realitylab.arface.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import no.realitylab.arface.ui.home.ProductDetailActivity
import no.realitylab.arface.R
import no.realitylab.arface.model.Product
import java.text.DecimalFormat

class ProductAdapter(private var products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.ivProduct)
        val name: TextView = itemView.findViewById(R.id.tvName)
        val gender: TextView = itemView.findViewById(R.id.tvCategory)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        // Nếu muốn thêm đánh giá: val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
    }
    val formatter = DecimalFormat("#,###")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.name.text = product.name
        holder.gender.text = product.brand
        holder.price.text = "đ${formatter.format(product.price)}"

        Glide.with(holder.image.context)
            .load(product.images[0])
            .apply(RequestOptions().centerCrop().transform(RoundedCorners(24)))
            .into(holder.image)

        // Mở ProductDetailActivity khi nhấn vào item
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("product", product)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = products.size

    // ✅ Dùng để cập nhật dữ liệu khi tìm kiếm hoặc reload
    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
