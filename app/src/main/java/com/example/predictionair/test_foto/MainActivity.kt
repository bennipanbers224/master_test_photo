package com.example.predictionair.test_foto

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.core.ImageCapture.OutputFileResults
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import kotlin.String as String1


class MainActivity : ComponentActivity() {
    lateinit var bitmapResult: Bitmap
    lateinit var compressKtpBase64: String1
    lateinit var ivCapture:ImageButton
    lateinit var btnNext:Button
    lateinit var btnRepeat:Button
    lateinit var cameraView:PreviewView
    var cameraFacing:Int = CameraSelector.LENS_FACING_BACK

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            startCamera(cameraFacing)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivCapture = findViewById(R.id.ivCapture)
        btnNext = findViewById(R.id.btnNext)
        btnRepeat = findViewById(R.id.btnRepeat)
        cameraView = findViewById(R.id.cameraView)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera(cameraFacing)
        }

    }

    fun startCamera(cameraFacing:Int){
        val aspectRatio = aspectRatio(cameraView.getWidth(), cameraView.getHeight())
        val listenableFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(this)

        listenableFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider =
                    listenableFuture.get() as ProcessCameraProvider
                val preview: Preview = Preview.Builder().setTargetAspectRatio(aspectRatio).build()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(windowManager.defaultDisplay.rotation).build()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraFacing).build()
                cameraProvider.unbindAll()
                val camera: Camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                ivCapture.setOnClickListener(View.OnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    takePicture(imageCapture)
                })
//                toggleFlash.setOnClickListener(View.OnClickListener { setFlashIcon(camera) })
                preview.setSurfaceProvider(cameraView.surfaceProvider)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    fun takePicture(imageCapture: ImageCapture?) {
        val file = File(getExternalFilesDir(null), System.currentTimeMillis().toString() + ".jpg")
        val outputFileOptions = OutputFileOptions.Builder(file).build()
        imageCapture!!.takePicture(
            outputFileOptions,
            Executors.newCachedThreadPool(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: OutputFileResults) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Image saved at: " + file.path,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    startCamera(cameraFacing)
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to save: " + exception.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    startCamera(cameraFacing)
                }
            })
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = Math.max(width, height).toDouble() / Math.min(width, height)
        return if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
            AspectRatio.RATIO_4_3
        } else AspectRatio.RATIO_16_9
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onStop() {
        super.onStop()
    }
}