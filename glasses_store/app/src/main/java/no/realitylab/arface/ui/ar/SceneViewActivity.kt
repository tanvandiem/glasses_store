package no.realitylab.arface.ui.ar

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformationSystem
import no.realitylab.arface.databinding.ActivitySceneViewBinding
import no.realitylab.arface.node.DragTransformableNode
import java.util.concurrent.CompletionException

class SceneViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySceneViewBinding
    private var localModel = "default_model.glb"
    private val defaultScale = 1.0f

    companion object {
        const val EXTRA_MODEL_TYPE = "modelType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySceneViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        // Cấu hình AppBar
        binding.topAppBar.title = "Mô hình 3D"
        binding.topAppBar.setNavigationOnClickListener {
            resetModelScale()
            onBackPressedDispatcher.onBackPressed()
        }

        // Nhận model từ intent nếu có
        val modelType = intent.getStringExtra(EXTRA_MODEL_TYPE)
        if (!modelType.isNullOrEmpty() && modelType != "remote") {
            localModel = modelType
        }

        // Hiển thị mô hình
        renderLocalObject()
    }

    private fun renderLocalObject() {
        binding.skuProgressBar.visibility = View.VISIBLE

        ModelRenderable.builder()
            .setSource(
                this, RenderableSource.builder()
                    .setSource(this, Uri.parse(localModel), RenderableSource.SourceType.GLB)
                    .setScale(2.0f)
                    .setRecenterMode(RenderableSource.RecenterMode.CENTER)
                    .build()
            )
            .setRegistryId(localModel)
            .build()
            .thenAccept { modelRenderable ->
                binding.skuProgressBar.visibility = View.GONE
                addNodeToScene(modelRenderable)
            }
            .exceptionally { throwable ->
                binding.skuProgressBar.visibility = View.GONE
                val message = when {
                    throwable is CompletionException -> "Không thể tải mô hình. Vui lòng kiểm tra kết nối mạng."
                    throwable?.message?.contains("glb") == true -> "Lỗi khi tải file GLB. Vui lòng kiểm tra định dạng."
                    else -> "Lỗi tải mô hình: ${throwable?.message}"
                }
                showErrorDialog(message)
                null
            }
    }

    private fun addNodeToScene(model: ModelRenderable) {
        val transformationSystem = makeTransformationSystem()
        val dragTransformableNode = DragTransformableNode(1f, transformationSystem).apply {
            renderable = model
            localScale = com.google.ar.sceneform.math.Vector3(defaultScale, defaultScale, defaultScale)
        }

        binding.sceneView.scene.apply {
            addChild(dragTransformableNode)
            addOnPeekTouchListener { hitTestResult, motionEvent ->
                transformationSystem.onTouch(hitTestResult, motionEvent)
            }
        }

        dragTransformableNode.select()
    }

    private fun makeTransformationSystem(): TransformationSystem {
        return TransformationSystem(resources.displayMetrics, FootprintSelectionVisualizer())
    }

    private fun showErrorDialog(message: String) {
        Handler(Looper.getMainLooper()).post {
            AlertDialog.Builder(this)
                .setTitle("Lỗi")
                .setMessage(message)
                .setPositiveButton("Thử lại") { dialog, _ ->
                    renderLocalObject()
                    dialog.dismiss()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            binding.sceneView.resume()
        } catch (e: CameraNotAvailableException) {
            e.printStackTrace()
            showErrorDialog("Không thể mở camera: ${e.message}")
        }
    }

    override fun onPause() {
        super.onPause()
        binding.sceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            binding.sceneView.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetModelScale() {
        val sceneChildren = binding.sceneView.scene.children
        for (node in sceneChildren) {
            if (node is DragTransformableNode) {
                node.localScale = com.google.ar.sceneform.math.Vector3(defaultScale, defaultScale, defaultScale)
            }
        }
    }
}
