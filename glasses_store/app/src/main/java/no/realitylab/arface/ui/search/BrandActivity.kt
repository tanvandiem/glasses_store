package no.realitylab.arface.ui.search
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import no.realitylab.arface.R
import no.realitylab.arface.adapter.ProductAdapter
import no.realitylab.arface.data.remote.RetrofitClient

class BrandActivity : AppCompatActivity() {

    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brand)

        val brandName = intent.getStringExtra("brandName")
        val tvTitle = findViewById<TextView>(R.id.tvBrandTitle)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewBrandProducts)

        // Thiết lập RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        productAdapter = ProductAdapter(emptyList())
        recyclerView.adapter = productAdapter

        // Gán tên brand vào tiêu đề
        tvTitle.text = brandName

        // Xử lý nút quay lại
        btnBack.setOnClickListener {
            finish()
        }

        // Gọi API
        brandName?.let {
            searchByBrand(it)
        }
    }

    private fun searchByBrand(brandName: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.searchProducts(brandName)
                if (response.isSuccessful) {
                    val products = response.body()!!.data  // ❗ lấy .data, không phải .products
                    productAdapter.updateProducts(products)
                } else {
                    Toast.makeText(this@BrandActivity, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BrandActivity, "Lỗi khi gọi API", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
