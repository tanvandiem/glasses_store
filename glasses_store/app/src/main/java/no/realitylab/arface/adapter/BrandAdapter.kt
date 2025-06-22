package no.realitylab.arface.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import no.realitylab.arface.R

class BrandAdapter(
    private var brands: List<String>,
    private val onBrandClick: (String) -> Unit
) : RecyclerView.Adapter<BrandAdapter.BrandViewHolder>() {

    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBrand: TextView = itemView.findViewById(R.id.tvBrand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand_button, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = brands[position]
        holder.tvBrand.text = brand

        holder.itemView.setOnClickListener {
            onBrandClick(brand)
        }
    }

    override fun getItemCount(): Int = brands.size

    fun updateBrands(newBrands: List<String>) {
        brands = newBrands
        notifyDataSetChanged()
    }
}
