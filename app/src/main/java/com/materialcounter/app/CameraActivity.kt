package com.materialcounter.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    
    private lateinit var previewView: PreviewView
    private lateinit var tvResult: TextView
    private lateinit var btnCapture: Button
    private lateinit var cameraExecutor: ExecutorService
    
    private var imageCapture: ImageCapture? = null
    private val scannedCodes = mutableSetOf<String>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        
        previewView = findViewById(R.id.preview_view)
        tvResult = findViewById(R.id.tv_result)
        btnCapture = findViewById(R.id.btn_capture)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        btnCapture.setOnClickListener {
            captureAndAnalyze()
        }
        
        startCamera()
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "相机启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            
        }, ContextCompat.getMainExecutor(this))
    }
    
    @SuppressLint("UnsafeOptInUsageError")
    private fun captureAndAnalyze() {
        tvResult.text = getString(R.string.scanning)
        
        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        
                        val scanner = BarcodeScanning.getClient()
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                processBarcodes(barcodes)
                            }
                            .addOnFailureListener { e ->
                                runOnUiThread {
                                    Toast.makeText(
                                        this@CameraActivity,
                                        "识别失败: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            "拍照失败: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }
    
    private fun processBarcodes(barcodes: List<Barcode>) {
        for (barcode in barcodes) {
            barcode.rawValue?.let { value ->
                scannedCodes.add(value)
            }
        }
        
        val count = scannedCodes.size
        val resultText = getString(R.string.count_result, count)
        
        runOnUiThread {
            tvResult.text = resultText
            
            if (count > 0) {
                saveRecord(count)
                Toast.makeText(this, "已识别 $count 个材料码", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未识别到条形码或二维码", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveRecord(count: Int) {
        val prefs = getSharedPreferences("material_counter", Context.MODE_PRIVATE)
        val gson = Gson()
        
        val historyJson = prefs.getString("history", "[]")
        val type = object : TypeToken<MutableList<CountRecord>>() {}.type
        val history: MutableList<CountRecord> = gson.fromJson(historyJson, type)
        
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        
        history.add(0, CountRecord(timestamp, count))
        
        if (history.size > 100) {
            history.removeAt(history.size - 1)
        }
        
        prefs.edit().putString("history", gson.toJson(history)).apply()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

data class CountRecord(
    val timestamp: String,
    val count: Int
)
