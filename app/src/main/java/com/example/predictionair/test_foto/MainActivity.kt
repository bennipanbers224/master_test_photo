package com.example.predictionair.test_foto

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.log.fileLogger
import io.fotoapparat.log.logcat
import io.fotoapparat.log.loggers
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.parameter.ScaleType
import io.fotoapparat.selector.auto
import io.fotoapparat.selector.autoFocus
import io.fotoapparat.selector.back
import io.fotoapparat.selector.continuousFocusPicture
import io.fotoapparat.selector.firstAvailable
import io.fotoapparat.selector.fixed
import io.fotoapparat.selector.highestFps
import io.fotoapparat.selector.highestResolution
import io.fotoapparat.selector.highestSensorSensitivity
import io.fotoapparat.selector.hz50
import io.fotoapparat.selector.hz60
import io.fotoapparat.selector.manualJpegQuality
import io.fotoapparat.selector.none
import io.fotoapparat.selector.off
import io.fotoapparat.view.CameraView
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

    private lateinit var fotoapparat: Fotoapparat
    lateinit var bitmapResult: Bitmap
    lateinit var compressKtpBase64:String
    lateinit var ivCapture:ImageButton
    lateinit var btnNext:Button
    lateinit var btnRepeat:Button
    lateinit var cameraView:CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ivCapture = findViewById(R.id.ivCapture)
        btnNext = findViewById(R.id.btnNext)
        btnRepeat = findViewById(R.id.btnRepeat)
        cameraView = findViewById(R.id.cameraView)

        chackandOpencamera()
        cameraInit()
        setupWatcherListener()

    }

    private fun setupWatcherListener(){
        ivCapture.setOnClickListener {
            val photoResult = fotoapparat.takePicture()
            photoResult.toBitmap()
                .whenAvailable { bitmapPhoto ->

                    if (bitmapPhoto != null) {
                        val deviceRotate = bitmapPhoto.rotationDegrees
                        var rotatedBitmap = bitmapPhoto.bitmap
                        if (deviceRotate == 270) {
                            rotatedBitmap = rotateBitmap(bitmapPhoto.bitmap, 90f)
                        }
                        bitmapResult = rotatedBitmap
                        fotoapparat.stop()
                        btnNext.visibility = View.VISIBLE
                        btnRepeat.visibility = View.VISIBLE
                        ivCapture.visibility = View.GONE
                    }
                }
        }
        btnNext.setOnClickListener {

//            val bitmap= BitmapFactory.decodeResource(resources, R.drawable.ktp_example)
            compressKtpBase64 = compress(bitmapResult)
        }
        btnRepeat.setOnClickListener {

            btnNext.visibility = View.GONE
            btnRepeat.visibility = View.GONE
            ivCapture.visibility = View.VISIBLE
            fotoapparat.start()
        }
    }

    private fun cameraInit() {
        fotoapparat= Fotoapparat(
            context = this,
            view = cameraView,
            scaleType = ScaleType.CenterCrop,
            lensPosition = back(),
            cameraConfiguration =  CameraConfiguration(
                pictureResolution = firstAvailable({ Resolution(1280, 720) }, highestResolution()),
                previewResolution = highestResolution(),
                previewFpsRange = highestFps(),
                focusMode = firstAvailable(
                    continuousFocusPicture(),
                    autoFocus(),                       // if continuous focus is not available on device, auto focus will be used
                    fixed()                            // if even auto focus is not available - fixed focus mode will be used
                ),
                flashMode = off(),
                antiBandingMode = firstAvailable(       // (optional) similar to how it is done for focus mode & flash, now for anti banding
                    auto(),
                    hz50(),
                    hz60(),
                    none()
                ),
                jpegQuality = manualJpegQuality(80),
                sensorSensitivity = highestSensorSensitivity(),
                frameProcessor = { _ -> }
            ),
            logger = loggers(
                logcat(),
                fileLogger(this)
            )
        )
    }

    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun compress(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                cameraInit()
            } else {
                Toast.makeText(this,"permission deind",Toast.LENGTH_LONG).show()
            }
        }

    fun chackandOpencamera(){


        if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            cameraInit()
        }else{
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onStart() {
        super.onStart()
        fotoapparat.start()
    }

    override fun onResume() {
        fotoapparat.start()
        super.onResume()
    }


    override fun onStop() {
        fotoapparat.stop()
        super.onStop()
    }
}