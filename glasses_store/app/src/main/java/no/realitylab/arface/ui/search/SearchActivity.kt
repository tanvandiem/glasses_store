package no.realitylab.arface.ui.search

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import no.realitylab.arface.R
import no.realitylab.arface.adapter.ProductAdapter
import no.realitylab.arface.data.remote.RetrofitClient

class SearchActivity : AppCompatActivity() {

    private lateinit var etSearch: EditText
    private lateinit var btnBack: ImageView
    private lateinit var rvSearchResults: RecyclerView
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        // Ánh xạ view
        etSearch = findViewById(R.id.etSearch)
        btnBack = findViewById(R.id.btnBack1)
        rvSearchResults = findViewById(R.id.rvSearchResults1)

        // Setup RecyclerView
        rvSearchResults.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(emptyList())
        rvSearchResults.adapter = productAdapter

        // Sự kiện nút quay lại
        btnBack.setOnClickListener {
            finish()
        }

        // Lấy query từ Intent nếu có
        val initialQuery = intent.getStringExtra("query")
        if (!initialQuery.isNullOrEmpty()) {
            etSearch.setText(initialQuery)         // ✅ set text
            etSearch.setSelection(initialQuery.length) // ✅ đặt con trỏ cuối
            performSearch(initialQuery)            // ✅ tìm kiếm luôn
        }

        // Lắng nghe nhập text
        etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            } else {
                productAdapter.updateProducts(emptyList()) // clear kết quả khi xóa hết chữ
            }
        }
    }


    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchProducts(query)
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!!.data  // ❗ lấy .data, không phải .products
                    productAdapter.updateProducts(products)
                } else {
                    Toast.makeText(this@SearchActivity, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show()
                    productAdapter.updateProducts(emptyList())
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
