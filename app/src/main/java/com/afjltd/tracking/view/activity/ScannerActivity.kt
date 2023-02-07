package com.afjltd.tracking.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

import com.afjltd.tracking.databinding.AppScanActivityLayoutBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.app_scan_activity_layout.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class ScannerActivity : AppCompatActivity(), ImageAnalysis.Analyzer {
    private lateinit var binding: AppScanActivityLayoutBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    binding.root,
                    "The camera permission is necessary",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AppScanActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try{
            supportActionBar?.hide()
        }catch (e: Exception){}

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        cameraPermissionResult.launch(android.Manifest.permission.CAMERA)

        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                animateFlash()
            }
        }

       /* binding..setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }*/

        binding.btnBack.setOnClickListener{
          onBackPressed()
        }

    }



    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera() {
        val previewView = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        }

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()

                // Image analysis use case

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(getExecutor(),this )
                val camera=  cameraProvider.bindToLifecycle(this,
                    cameraSelector,
                    previewView, imageCapture )

                camera.cameraControl.setZoomRatio(0.5f)

/*
val camera=  cameraProvider.bindToLifecycle(this,
                    cameraSelector,
                    previewView, imageCapture,
                    imageAnalysis)
                //touch to focus listener
                binding.viewFinder.setOnTouchListener { _, event ->
                    if (event.action != MotionEvent.ACTION_UP) {
                        return@setOnTouchListener true
                    }
                    camera?.let { camera ->
                        camera.cameraControl.setLinearZoom(20 / 100.toFloat())

                        val pointFactory: MeteringPointFactory = binding.viewFinder.meteringPointFactory
                        val afPointWidth = 1.0f / 6.0f
                        val aePointWidth = afPointWidth * 1.5f
                        val x =event.x
                        val y = event.y

                        val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                            .createPoint(1f, 1f)
                        val afPoint = pointFactory.createPoint(x, y, afPointWidth)
                        val aePoint = pointFactory.createPoint(x, y, aePointWidth)
                        val future = camera.cameraControl.startFocusAndMetering(
                            FocusMeteringAction.Builder(
                               // afPoint,
                                autoFocusPoint,
                                FocusMeteringAction.FLAG_AF
                            ).addPoint(
                              //  aePoint,
                                autoFocusPoint,
                                FocusMeteringAction.FLAG_AE
                            ).apply {
                                //start auto-focusing after 2 seconds
                                setAutoCancelDuration(2, TimeUnit.SECONDS)
                            }.build()
                        )
                        future.addListener({}, ContextCompat.getMainExecutor(this))
                    }

                    return@setOnTouchListener true
                }*/
            } catch (e: Exception) {
                Log.d(TAG, "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))




    }

    fun getExecutor(): Executor {
        return ContextCompat.getMainExecutor(this)
    }


    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "JPEG_${System.currentTimeMillis()}.jpg"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                imgCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                        val intent = Intent()
                        intent.putExtra(FILE_PATH_EXTRA,file.toUri().path)
                        setResult(RESULT_CODE, intent)
                        finish()

                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            binding.root.context,
                            "Error taking photo",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Error taking photo:$exception")
                    }

                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun animateFlash() {
        binding.root.postDelayed({
            binding.root.foreground = ColorDrawable(Color.WHITE)
            binding.root.postDelayed({
                binding.root.foreground = null
            }, 50)
        }, 100)
    }

    companion object {
        val TAG = "MainActivity"
        val FILE_PATH_EXTRA="file_path"
        val RESULT_CODE = 1012
    }



    fun cropBitmapToCard(source: Bitmap, frame: View, cardPlaceHolder: View): Bitmap {
        val scaleX = source.width / frame.width as Float
        val scaleY = source.height / frame.height as Float
        val x = (cardPlaceHolder.left * scaleX) as Int
        val y = (cardPlaceHolder.top * scaleY) as Int
        Log.e(
            "MainActivity-Crop",
            "leftPos: " + cardPlaceHolder.left
                .toString() + " width: " + cardPlaceHolder.width
        )
        val width = (cardPlaceHolder.width * scaleX) as Int
        val height = (cardPlaceHolder.height * scaleY) as Int
        return Bitmap.createBitmap(source, x, y, width, height)
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun rotateBitmapIfNeeded(source: Bitmap, info: ImageInfo): Bitmap {
        val angle: Int = info.rotationDegrees
        val mat = Matrix()
        mat.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, mat, true)
    }

    override fun analyze(image: ImageProxy) {

        val bitmap: Bitmap? = binding.viewFinder.bitmap

        image.close()

        if (bitmap == null) return



       /* GlobalScope.launch (Dispatchers.Main){
            val bitmap1: Bitmap = toGrayscale(bitmap)
          val bitmap2 = createContrast(bitmap1, 0.50)
            binding.grayView.setImageBitmap(bitmap2)
        }*/
    }


}