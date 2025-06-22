package no.realitylab.arface.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import no.realitylab.arface.R
import no.realitylab.arface.ui.search.BrandActivity
import no.realitylab.arface.ui.ar.GlassesActivity
import no.realitylab.arface.ui.search.SearchActivity
import no.realitylab.arface.adapter.BrandAdapter
import no.realitylab.arface.adapter.ProductAdapter
import no.realitylab.arface.data.remote.RetrofitClient

class HomeFragment : Fragment() {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var brandAdapter: BrandAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewProducts = view.findViewById<RecyclerView>(R.id.recyclerViewProducts)
        val recyclerViewBrands = view.findViewById<RecyclerView>(R.id.recyclerViewBrands)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val btnGlass = view.findViewById<Button>(R.id.button_glasses)

        recyclerViewProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        productAdapter = ProductAdapter(emptyList())
        recyclerViewProducts.adapter = productAdapter

        recyclerViewBrands.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        brandAdapter = BrandAdapter(emptyList()) { brandName ->
            val intent = Intent(requireContext(), BrandActivity::class.java)
            intent.putExtra("brandName", brandName)
            startActivity(intent)
        }
        recyclerViewBrands.adapter = brandAdapter

        btnGlass.setOnClickListener {
            startActivity(Intent(requireContext(), GlassesActivity::class.java))
        }

        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra("query", query)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        loadAllProducts()
    }

    private fun loadAllProducts() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAllProducts()
                if (response.isSuccessful) {
                    val products = response.body()?.data?.allProducts ?: emptyList()
                    productAdapter.updateProducts(products)

                    // ✅ Tạo danh sách brand duy nhất từ sản phẩm
                    val brandSet = products.mapNotNull { it.brand }.toSet().toList()
                    brandAdapter.updateBrands(brandSet)
                } else {
                    Log.e("API_ERROR", "Response failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Exception: ${e.message}")
            }
        }
    }



}
