package no.realitylab.arface.ui.ar

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.AugmentedFaceNode
import no.realitylab.arface.R

class TryOnGlassesActivity : AppCompatActivity() {

    companion object {
        const val MIN_OPENGL_VERSION = 3.0
    }

    private lateinit var arFragment: FaceArFragment
    private var faceRegionsRenderable: ModelRenderable? = null
    private val faceNodeMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkIsSupportedDeviceOrFinish()) return

        setContentView(R.layout.activity_try_on_glasses)

        // Nút quay lại
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }


        // Nhận URL model từ Intent
        val modelUrl = intent.getStringExtra("GLASSES_URL") ?: run {
            Toast.makeText(this, "Không có model được truyền", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        arFragment = supportFragmentManager.findFragmentById(R.id.face_fragment) as FaceArFragment

        // Load model
        ModelRenderable.builder()
            .setSource(
                this,
                RenderableSource.builder()
                    .setSource(this, Uri.parse(modelUrl), RenderableSource.SourceType.GLB)
                    .setScale(1.0f)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()
            )
            .setRegistryId(modelUrl)
            .build()
            .thenAccept { modelRenderable ->
                faceRegionsRenderable = modelRenderable
                modelRenderable.isShadowCaster = false
                modelRenderable.isShadowReceiver = false
            }
            .exceptionally { throwable ->
                Toast.makeText(this, "Lỗi tải model: ${throwable.message}",
                    Toast.LENGTH_LONG).show()
                null
            }

        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView.scene

        scene.addOnUpdateListener {
            if (faceRegionsRenderable != null) {
                sceneView.session
                    ?.getAllTrackables(AugmentedFace::class.java)?.let {
                        for (f in it) {
                            if (!faceNodeMap.containsKey(f)) {
                                val faceNode = AugmentedFaceNode(f)
                                faceNode.setParent(scene)
                                faceNode.faceRegionsRenderable = faceRegionsRenderable
                                faceNodeMap[f] = faceNode
                            }
                        }
                        val iter = faceNodeMap.entries.iterator()
                        while (iter.hasNext()) {
                            val entry = iter.next()
                            val face = entry.key
                            if (face.trackingState == TrackingState.STOPPED) {
                                entry.value.setParent(null)
                                iter.remove()
                            }
                        }
                    }
            }
        }
    }

    private fun checkIsSupportedDeviceOrFinish(): Boolean {
        if (ArCoreApk.getInstance().checkAvailability(this)
            == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
            Toast.makeText(this, "Thiết bị không hỗ trợ ARCore", Toast.LENGTH_LONG).show()
            finish()
            return false
        }

        val openGlVersionString = (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)
            ?.deviceConfigurationInfo
            ?.glEsVersion

        openGlVersionString?.let {
            if (it.toDouble() < MIN_OPENGL_VERSION) {
                Toast.makeText(this, "Cần OpenGL ES 3.0 trở lên", Toast.LENGTH_LONG).show()
                finish()
                return false
            }
        }
        return true
    }
}
