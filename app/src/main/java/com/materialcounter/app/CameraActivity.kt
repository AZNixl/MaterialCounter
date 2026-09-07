package com.materialcounter.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    
    private lateinit var previewView: PreviewView
    private lateinit var tvStatus: TextView
    private lateinit var tvCount: TextView
    private lateinit var btnCapture: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var resultPanel: LinearLayout
    private lateinit var tvResultCount: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var cameraExecutor: ExecutorService
    
    private var imageCapture: ImageCapture? = null
    private var capturedBitmap: Bitmap? = null
    private var detectedObjects = mutableListOf<Rect>()
    
    private val markerPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    
    private val textBgPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        
        previewView = findViewById(R.id.viewFinder)
        tvStatus = findViewById(R.id.tvStatus)
        tvCount = findViewById(R.id.tvCount)
        btnCapture = findViewById(R.id.btnCapture)
        imageView = findViewById(R.id.imageView)
        resultPanel = findViewById(R.id.resultPanel)
        tvResultCount = findViewById(R.id.tvResultCount)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        btnCapture.setOnClickListener {
            capturePhoto()
        }
        
        btnSave.setOnClickListener {
            saveRecord()
        }
        
        btnCancel.setOnClickListener {
            resetToCamera()
        }
        
        tvStatus.text = "点击拍照按钮拍摄材料"
        
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
    private fun capturePhoto() {
        tvStatus.text = "拍摄中..."
        
        imageCapture?.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()
                    
                    runOnUiThread {
                        tvStatus.text = "识别中..."
                        detectObjects(bitmap)
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CameraActivity,
                            "拍照失败: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        tvStatus.text = "点击拍照按钮拍摄材料"
                    }
                }
            }
        )
    }
    
    private fun detectObjects(bitmap: Bitmap) {
        capturedBitmap = bitmap
        detectedObjects.clear()
        
        // 配置物体检测器
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        
        val detector = ObjectDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)
        
        detector.process(image)
            .addOnSuccessListener { detectedObjectsList ->
                // 收集所有检测到的物体边界框
                detectedObjectsList.forEach { obj ->
                    detectedObjects.add(obj.boundingBox)
                }
                
                runOnUiThread {
                    if (detectedObjects.isEmpty()) {
                        Toast.makeText(this, "未识别到物体，请重新拍照", Toast.LENGTH_LONG).show()
                        tvStatus.text = "未识别到物体"
                    } else {
                        showDetectionResult()
                    }
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "识别失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    tvStatus.text = "识别失败，请重新拍照"
                }
            }
    }
    
    private fun showDetectionResult() {
        val bitmap = capturedBitmap ?: return
        
        // 隐藏相机预览
        previewView.visibility = View.GONE
        btnCapture.visibility = View.GONE
        
        // 显示结果
        imageView.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvCount.visibility = View.VISIBLE
        resultPanel.visibility = View.VISIBLE
        
        // 绘制标记
        val markedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(markedBitmap)
        
        detectedObjects.forEachIndexed { index, rect ->
            // 绘制边框
            canvas.drawRect(rect, markerPaint)
            
            // 绘制编号背景
            val text = "${index + 1}"
            val textBounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, textBounds)
            val textWidth = textBounds.width() + 20f
            val textHeight = textBounds.height() + 20f
            
            val textX = rect.centerX().toFloat()
            val textY = rect.top.toFloat() - 10f
            
            canvas.drawRect(
                textX - textWidth / 2,
                textY - textHeight,
                textX + textWidth / 2,
                textY,
                textBgPaint
            )
            
            // 绘制编号
            canvas.drawText(text, textX, textY - 10f, textPaint)
        }
        
        imageView.setImageBitmap(markedBitmap)
        
        val count = detectedObjects.size
        tvCount.text = "$count"
        tvResultCount.text = "识别到: $count 个物体"
        tvStatus.text = "识别完成"
    }
    
    private fun saveRecord() {
        val count = detectedObjects.size
        if (count == 0) {
            Toast.makeText(this, "没有识别到物体", Toast.LENGTH_SHORT).show()
            return
        }
        
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
        
        Toast.makeText(this, "已保存：$count 个", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun resetToCamera() {
        // 显示相机预览
        previewView.visibility = View.VISIBLE
        btnCapture.visibility = View.VISIBLE
        
        // 隐藏结果
        imageView.visibility = View.GONE
        tvCount.visibility = View.GONE
        resultPanel.visibility = View.GONE
        
        tvStatus.text = "点击拍照按钮拍摄材料"
        tvStatus.visibility = View.VISIBLE
        
        // 清空数据
        capturedBitmap = null
        detectedObjects.clear()
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
