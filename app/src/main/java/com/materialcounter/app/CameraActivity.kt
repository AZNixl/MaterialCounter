package com.materialcounter.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
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
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private var markerPoints = mutableListOf<Pair<Float, Float>>()
    
    private val markerPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    
    private val circlePaint = Paint().apply {
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
                        showPhotoForMarking(bitmap)
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
    
    @SuppressLint("ClickableViewAccessibility")
    private fun showPhotoForMarking(bitmap: Bitmap) {
        capturedBitmap = bitmap
        markerPoints.clear()
        
        // 隐藏相机预览
        previewView.visibility = View.GONE
        btnCapture.visibility = View.GONE
        
        // 显示图片和标记界面
        imageView.visibility = View.VISIBLE
        tvStatus.visibility = View.VISIBLE
        tvCount.visibility = View.VISIBLE
        resultPanel.visibility = View.VISIBLE
        
        imageView.setImageBitmap(bitmap)
        tvStatus.text = "点击照片标记每个材料的一端"
        updateCount()
        
        // 监听点击事件在照片上标记
        imageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.x
                val y = event.y
                
                // 将屏幕坐标转换为图片坐标
                val imageViewWidth = imageView.width.toFloat()
                val imageViewHeight = imageView.height.toFloat()
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()
                
                // 计算图片在 ImageView 中的实际显示区域（fitCenter）
                val imageRatio = bitmapWidth / bitmapHeight
                val viewRatio = imageViewWidth / imageViewHeight
                
                val scaleX: Float
                val scaleY: Float
                val offsetX: Float
                val offsetY: Float
                
                if (imageRatio > viewRatio) {
                    // 图片更宽，以宽度为准
                    scaleX = bitmapWidth / imageViewWidth
                    scaleY = scaleX
                    val scaledHeight = bitmapHeight / scaleY
                    offsetX = 0f
                    offsetY = (imageViewHeight - scaledHeight) / 2f
                } else {
                    // 图片更高，以高度为准
                    scaleY = bitmapHeight / imageViewHeight
                    scaleX = scaleY
                    val scaledWidth = bitmapWidth / scaleX
                    offsetX = (imageViewWidth - scaledWidth) / 2f
                    offsetY = 0f
                }
                
                val imageX = (x - offsetX) * scaleX
                val imageY = (y - offsetY) * scaleY
                
                // 检查是否在图片范围内
                if (imageX >= 0 && imageX <= bitmapWidth && imageY >= 0 && imageY <= bitmapHeight) {
                    markerPoints.add(Pair(imageX, imageY))
                    updateMarkedImage()
                    updateCount()
                }
                
                true
            } else {
                false
            }
        }
    }
    
    private fun updateMarkedImage() {
        val bitmap = capturedBitmap ?: return
        val markedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(markedBitmap)
        
        markerPoints.forEachIndexed { index, point ->
            // 绘制红色圆圈标记
            canvas.drawCircle(point.first, point.second, 40f, circlePaint)
            canvas.drawCircle(point.first, point.second, 15f, markerPaint)
            
            // 绘制编号
            canvas.drawText("${index + 1}", point.first, point.second + 20f, textPaint)
        }
        
        imageView.setImageBitmap(markedBitmap)
    }
    
    private fun updateCount() {
        val count = markerPoints.size
        tvCount.text = "$count"
        tvResultCount.text = "已标记: $count 个"
    }
    
    private fun saveRecord() {
        val count = markerPoints.size
        if (count == 0) {
            Toast.makeText(this, "请先标记物体", Toast.LENGTH_SHORT).show()
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
        
        // 隐藏图片和标记界面
        imageView.visibility = View.GONE
        tvCount.visibility = View.GONE
        resultPanel.visibility = View.GONE
        
        tvStatus.text = "点击拍照按钮拍摄材料"
        tvStatus.visibility = View.VISIBLE
        
        // 清空数据
        capturedBitmap = null
        markerPoints.clear()
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
