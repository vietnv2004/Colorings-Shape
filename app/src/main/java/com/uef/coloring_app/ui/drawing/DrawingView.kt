package com.uef.coloring_app.ui.drawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.uef.coloring_app.core.haptic.HapticManager

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawPath: Path = Path()
    private var drawPaint: Paint = Paint()
    private var canvasPaint: Paint = Paint(Paint.DITHER_FLAG)
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    
    private var currentColor: Int = Color.BLACK
    private var brushSize: Float = 20f
    
    // Shape boundary for coloring restriction
    private var shapePath: Path? = null
    private var shapeBounds: RectF? = null
    
    // History for undo/redo
    private val pathList = ArrayList<PathData>()
    private var currentPathIndex = -1
    
    data class PathData(val path: Path, val paint: Paint)
    
    init {
        setupDrawing()
    }
    
    private fun setupDrawing() {
        drawPaint.apply {
            color = currentColor
            isAntiAlias = true
            strokeWidth = brushSize
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap!!)
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the background bitmap
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, canvasPaint)
        }
        
        // Draw current path
        canvas.drawPath(drawPath, drawPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        
        // Check if touch point is inside the shape boundary
        if (!isPointInsideShape(touchX, touchY)) {
            return false // Ignore touches outside the shape
        }
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(touchX, touchY)
                // Rung nhẹ khi bắt đầu vẽ
                HapticManager.drawing(context)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                drawCanvas?.drawPath(drawPath, drawPaint)
                
                // Save to history
                val newPaint = Paint(drawPaint)
                val newPath = Path(drawPath)
                
                // Remove any redo paths
                while (pathList.size > currentPathIndex + 1) {
                    pathList.removeAt(pathList.size - 1)
                }
                
                pathList.add(PathData(newPath, newPaint))
                currentPathIndex++
                
                drawPath.reset()
            }
            else -> return false
        }
        
        invalidate()
        return true
    }
    
    fun setColor(color: Int) {
        currentColor = color
        drawPaint.color = currentColor
    }
    
    fun setBrushSize(size: Float) {
        brushSize = size
        drawPaint.strokeWidth = brushSize
    }
    
    fun setShapeBoundary(shapePath: Path) {
        this.shapePath = Path(shapePath)
        this.shapeBounds = RectF()
        this.shapePath?.computeBounds(this.shapeBounds!!, true)
    }
    
    private fun isPointInsideShape(x: Float, y: Float): Boolean {
        return shapePath?.let { path ->
            val region = Region()
            val clipRegion = Region(0, 0, width, height)
            region.setPath(path, clipRegion)
            region.contains(x.toInt(), y.toInt())
        } ?: true // If no shape boundary set, allow all drawing
    }
    
    fun undo() {
        if (currentPathIndex >= 0) {
            currentPathIndex--
            redrawCanvas()
        }
    }
    
    fun redo() {
        if (currentPathIndex < pathList.size - 1) {
            currentPathIndex++
            redrawCanvas()
        }
    }
    
    fun clear() {
        pathList.clear()
        currentPathIndex = -1
        redrawCanvas()
    }
    
    private fun redrawCanvas() {
        // Clear the canvas
        canvasBitmap?.let {
            it.eraseColor(Color.TRANSPARENT)
        }
        
        // Redraw all paths up to current index
        for (i in 0..currentPathIndex) {
            if (i < pathList.size) {
                val pathData = pathList[i]
                drawCanvas?.drawPath(pathData.path, pathData.paint)
            }
        }
        
        invalidate()
    }
    
    fun getBitmap(): Bitmap? {
        return canvasBitmap
    }
    
    fun getCurrentColor(): Int {
        return currentColor
    }
    
    fun getStrokeCount(): Int {
        return currentPathIndex + 1
    }
    
    /**
     * Tính phần trăm diện tích đã tô màu trong shape
     */
    fun getColoredCoverage(): Float {
        if (canvasBitmap == null || shapePath == null) {
            return 0f
        }
        
        try {
            val bitmap = canvasBitmap!!
            var coloredPixels = 0
            var totalShapePixels = 0
            
            // Lấy bounds của shape
            val bounds = shapeBounds ?: return 0f
            val left = bounds.left.toInt().coerceAtLeast(0)
            val top = bounds.top.toInt().coerceAtLeast(0)
            val right = bounds.right.toInt().coerceAtMost(bitmap.width)
            val bottom = bounds.bottom.toInt().coerceAtMost(bitmap.height)
            
            // Tạo region từ shapePath để kiểm tra pixel có trong shape không
            val region = Region()
            val clipRegion = Region(0, 0, bitmap.width, bitmap.height)
            region.setPath(shapePath!!, clipRegion)
            
            // Kiểm tra từng pixel trong bounds
            for (x in left until right step 2) { // Sample mỗi 2 pixels để tăng performance
                for (y in top until bottom step 2) {
                    if (x < bitmap.width && y < bitmap.height) {
                        // Kiểm tra xem pixel có nằm trong shape không
                        if (region.contains(x, y)) {
                            totalShapePixels++
                            
                            val pixel = bitmap.getPixel(x, y)
                            val alpha = android.graphics.Color.alpha(pixel)
                            
                            // Pixel được coi là đã tô nếu alpha > threshold
                            if (alpha > 50) {
                                coloredPixels++
                            }
                        }
                    }
                }
            }
            
            // Tính % coverage
            return if (totalShapePixels > 0) {
                (coloredPixels.toFloat() / totalShapePixels).coerceIn(0f, 1f)
            } else {
                0f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return 0f
        }
    }
}


