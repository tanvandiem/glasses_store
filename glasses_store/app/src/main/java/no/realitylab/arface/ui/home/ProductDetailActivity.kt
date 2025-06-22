package no.realitylab.arface.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import no.realitylab.arface.R
import no.realitylab.arface.ui.ar.TryOnGlassesActivity
import no.realitylab.arface.ui.ar.SceneViewActivity
import no.realitylab.arface.data.remote.RetrofitClient
import no.realitylab.arface.data.request.AddToCartRequest
import no.realitylab.arface.model.Product
import no.realitylab.arface.model.Variation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class ProductDetailActivity : AppCompatActivity() {
    val formatter = DecimalFormat("#,###")
    private var quantity = 1
    private var selectedVariation: Variation? = null
    private lateinit var variationMap: Map<String, Variation>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val product = intent.getSerializableExtra("product") as? Product

        val imageView = findViewById<ImageView>(R.id.ivDetailImage)
        val nameView = findViewById<TextView>(R.id.tvDetailName)
        val priceView = findViewById<TextView>(R.id.tvDetailPrice)
        val descView = findViewById<TextView>(R.id.tvDetailDescription)
        val quantityText = findViewById<TextView>(R.id.tvQuantity)
        val tvStock = findViewById<TextView>(R.id.tvStock)
        val btnDecrease = findViewById<ImageButton>(R.id.btnDecrease)
        val btnIncrease = findViewById<ImageButton>(R.id.btnIncrease)
        val btnAddCart = findViewById<Button>(R.id.btnAddCart)
        val btnGlass = findViewById<Button>(R.id.button_glasses)
        val btnMove = findViewById<Button>(R.id.button_glasse)

        val rgSize = findViewById<RadioGroup>(R.id.rgSize)
        val rbSizeS = findViewById<RadioButton>(R.id.rbSizeS)
        val rbSizeM = findViewById<RadioButton>(R.id.rbSizeM)
        val rbSizeL = findViewById<RadioButton>(R.id.rbSizeL)

//        btnGlass.setOnClickListener {
//            startActivity(Intent(this, GlassesActivity::class.java))
//        }
        btnGlass.setOnClickListener {
            product?.subCategory?.let { url ->
                val intent = Intent(this, TryOnGlassesActivity::class.java)
                intent.putExtra("GLASSES_URL", url)
                startActivity(intent)
            } ?: Toast.makeText(this, "Không có đường dẫn kính", Toast.LENGTH_SHORT).show()
        }

        btnMove.setOnClickListener {
            product?.subCategory?.let {
                val intent = Intent(this, SceneViewActivity::class.java)
                intent.putExtra(SceneViewActivity.EXTRA_MODEL_TYPE, it)
                startActivity(intent)
            }
        }



        product?.let {
            nameView.text = it.name
            priceView.text = "đ${formatter.format(it.price)}"
            descView.text = it.description

            Glide.with(this)
                .load(it.images.firstOrNull())
                .into(imageView)

            // Map size -> variation để dễ truy cập
            variationMap = it.variations.associateBy { variation -> variation.size }

            // Gán sự kiện chọn size
            rgSize.setOnCheckedChangeListener { _, checkedId ->
                val selectedSize = when (checkedId) {
                    R.id.rbSizeS -> "S"
                    R.id.rbSizeM -> "M"
                    R.id.rbSizeL -> "L"
                    else -> null
                }

                selectedSize?.let { size ->
                    selectedVariation = variationMap[size]
                    selectedVariation?.let { variation ->
                        quantity = 1
                        quantityText.text = quantity.toString()
                        tvStock.text = "Kho: ${variation.quantity}"
                    }
                }
            }
        }

        btnIncrease.setOnClickListener {
            selectedVariation?.let { variation ->
                if (quantity < variation.quantity) {
                    quantity++
                    quantityText.text = quantity.toString()
                } else {
                    Toast.makeText(this, "Vượt quá số lượng tồn kho", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Vui lòng chọn size trước", Toast.LENGTH_SHORT).show()
        }

        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityText.text = quantity.toString()
            }
        }

        btnAddCart.setOnClickListener {
            val sharedPreferences = getSharedPreferences("auth_preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("TOKEN", null)

            if (token == null || product == null || selectedVariation == null) {
                Toast.makeText(this, "Vui lòng đăng nhập hoặc chọn size", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = AddToCartRequest(
                productId = product._id,
                variationId = selectedVariation!!._id,
                quantity = quantity
            )

            RetrofitClient.instance.addToCart("Bearer $token", request)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@ProductDetailActivity, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ProductDetailActivity, "Thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@ProductDetailActivity, "Lỗi: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
