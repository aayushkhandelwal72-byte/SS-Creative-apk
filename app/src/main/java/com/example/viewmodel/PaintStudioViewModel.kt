package com.example.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.example.data.model.BrushType
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasPaperStyle
import com.example.data.model.SavedArtwork
import com.example.data.model.ShapeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class PaintStudioViewModel : ViewModel() {

    private val _elements = MutableStateFlow<List<CanvasElement>>(emptyList())
    val elements: StateFlow<List<CanvasElement>> = _elements.asStateFlow()

    private val undoStack = mutableListOf<List<CanvasElement>>()
    private val redoStack = mutableListOf<List<CanvasElement>>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _selectedBrush = MutableStateFlow(BrushType.SMOOTH)
    val selectedBrush: StateFlow<BrushType> = _selectedBrush.asStateFlow()

    private val _selectedColor = MutableStateFlow(Color(0xFF0A1931)) // Default Royal Navy
    val selectedColor: StateFlow<Color> = _selectedColor.asStateFlow()

    private val _strokeWidth = MutableStateFlow(12f)
    val strokeWidth: StateFlow<Float> = _strokeWidth.asStateFlow()

    private val _brushAlpha = MutableStateFlow(1.0f)
    val brushAlpha: StateFlow<Float> = _brushAlpha.asStateFlow()

    private val _selectedShape = MutableStateFlow(ShapeType.NONE)
    val selectedShape: StateFlow<ShapeType> = _selectedShape.asStateFlow()

    private val _paperStyle = MutableStateFlow(CanvasPaperStyle.CLEAN_WHITE)
    val paperStyle: StateFlow<CanvasPaperStyle> = _paperStyle.asStateFlow()

    // Live drawing state while finger/stylus is on screen
    private val _activePoints = MutableStateFlow<List<Offset>>(emptyList())
    val activePoints: StateFlow<List<Offset>> = _activePoints.asStateFlow()

    private val _activeShapePreview = MutableStateFlow<CanvasElement.ShapeElement?>(null)
    val activeShapePreview: StateFlow<CanvasElement.ShapeElement?> = _activeShapePreview.asStateFlow()

    private val _savedArtworks = MutableStateFlow<List<SavedArtwork>>(emptyList())
    val savedArtworks: StateFlow<List<SavedArtwork>> = _savedArtworks.asStateFlow()

    init {
        // Pre-populate with initial inspirational templates
        seedSampleArtworks()
    }

    private fun seedSampleArtworks() {
        val sample1 = SavedArtwork(
            id = "demo_1",
            title = "Studio Emblem Concept",
            elements = listOf(
                CanvasElement.ShapeElement(
                    shapeType = ShapeType.CIRCLE,
                    start = Offset(200f, 200f),
                    end = Offset(500f, 500f),
                    color = Color(0xFF0A1931),
                    strokeWidth = 14f
                ),
                CanvasElement.ShapeElement(
                    shapeType = ShapeType.CIRCLE_FILLED,
                    start = Offset(280f, 280f),
                    end = Offset(420f, 420f),
                    color = Color(0xFFD4AF37),
                    strokeWidth = 4f,
                    alpha = 0.85f
                ),
                CanvasElement.TextElement(
                    text = "SS CREATIVE",
                    position = Offset(220f, 560f),
                    color = Color(0xFF0A1931),
                    fontSize = 28f
                )
            ),
            paperStyle = CanvasPaperStyle.WARM_PARCHMENT,
            formattedDate = "Sample Concept"
        )
        _savedArtworks.value = listOf(sample1)
    }

    fun selectBrush(brush: BrushType) {
        _selectedBrush.value = brush
        _selectedShape.value = ShapeType.NONE
        if (brush == BrushType.ERASER) {
            // Eraser mode
        } else if (brush == BrushType.MARKER) {
            _brushAlpha.value = 0.45f
            if (_strokeWidth.value < 20f) _strokeWidth.value = 28f
        } else if (brush == BrushType.PEN) {
            _brushAlpha.value = 1f
            if (_strokeWidth.value > 10f) _strokeWidth.value = 6f
        }
    }

    fun selectShape(shape: ShapeType) {
        _selectedShape.value = shape
    }

    fun setColor(color: Color) {
        _selectedColor.value = color
    }

    fun setStrokeWidth(width: Float) {
        _strokeWidth.value = width
    }

    fun setBrushAlpha(alpha: Float) {
        _brushAlpha.value = alpha
    }

    fun setPaperStyle(style: CanvasPaperStyle) {
        _paperStyle.value = style
    }

    // Drawing lifecycle
    fun startStroke(point: Offset) {
        if (_selectedShape.value != ShapeType.NONE) {
            _activeShapePreview.value = CanvasElement.ShapeElement(
                shapeType = _selectedShape.value,
                start = point,
                end = point,
                color = _selectedColor.value,
                strokeWidth = _strokeWidth.value,
                alpha = _brushAlpha.value
            )
        } else {
            _activePoints.value = listOf(point)
        }
    }

    fun addStrokePoint(point: Offset) {
        if (_selectedShape.value != ShapeType.NONE) {
            val preview = _activeShapePreview.value
            if (preview != null) {
                _activeShapePreview.value = preview.copy(end = point)
            }
        } else {
            _activePoints.value = _activePoints.value + point
        }
    }

    fun finishStroke() {
        if (_selectedShape.value != ShapeType.NONE) {
            val preview = _activeShapePreview.value
            if (preview != null && (preview.start != preview.end)) {
                pushStateForUndo()
                _elements.value = _elements.value + preview
            }
            _activeShapePreview.value = null
        } else {
            val points = _activePoints.value
            if (points.isNotEmpty()) {
                val elementColor = if (_selectedBrush.value == BrushType.ERASER) {
                    _paperStyle.value.backgroundColor
                } else {
                    _selectedColor.value
                }
                val stroke = CanvasElement.StrokeElement(
                    points = points,
                    color = elementColor,
                    strokeWidth = _strokeWidth.value,
                    alpha = if (_selectedBrush.value == BrushType.ERASER) 1f else _brushAlpha.value,
                    brushType = _selectedBrush.value
                )
                pushStateForUndo()
                _elements.value = _elements.value + stroke
            }
            _activePoints.value = emptyList()
        }
    }

    fun addTextElement(text: String, position: Offset, color: Color, size: Float) {
        if (text.isNotBlank()) {
            pushStateForUndo()
            val textElement = CanvasElement.TextElement(
                text = text,
                position = position,
                color = color,
                fontSize = size
            )
            _elements.value = _elements.value + textElement
        }
    }

    private fun pushStateForUndo() {
        undoStack.add(_elements.value.toList())
        redoStack.clear()
        updateStackStates()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_elements.value.toList())
            val previous = undoStack.removeAt(undoStack.lastIndex)
            _elements.value = previous
            updateStackStates()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_elements.value.toList())
            val next = redoStack.removeAt(redoStack.lastIndex)
            _elements.value = next
            updateStackStates()
        }
    }

    fun clearCanvas() {
        if (_elements.value.isNotEmpty()) {
            pushStateForUndo()
            _elements.value = emptyList()
        }
    }

    private fun updateStackStates() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun saveArtwork(title: String) {
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        val formatted = dateFormat.format(Date())
        val name = title.ifBlank { "Studio Sketch #${_savedArtworks.value.size + 1}" }
        val artwork = SavedArtwork(
            title = name,
            elements = _elements.value.toList(),
            paperStyle = _paperStyle.value,
            formattedDate = formatted
        )
        _savedArtworks.value = listOf(artwork) + _savedArtworks.value
    }

    fun loadArtwork(artwork: SavedArtwork) {
        pushStateForUndo()
        _elements.value = artwork.elements
        _paperStyle.value = artwork.paperStyle
    }

    fun deleteArtwork(artworkId: String) {
        _savedArtworks.value = _savedArtworks.value.filter { it.id != artworkId }
    }

    fun loadPresetStencil(stencil: String, canvasSize: Size) {
        pushStateForUndo()
        val centerX = if (canvasSize.width > 0) canvasSize.width / 2f else 400f
        val centerY = if (canvasSize.height > 0) canvasSize.height / 2f else 600f

        when (stencil) {
            "Logo Grid" -> {
                _paperStyle.value = CanvasPaperStyle.BLUEPRINT_NAVY
                val list = mutableListOf<CanvasElement>()
                // Outer circle
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.CIRCLE,
                        start = Offset(centerX - 180f, centerY - 180f),
                        end = Offset(centerX + 180f, centerY + 180f),
                        color = Color(0xFFD4AF37),
                        strokeWidth = 6f
                    )
                )
                // Diagonal crosslines
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.LINE,
                        start = Offset(centerX - 180f, centerY),
                        end = Offset(centerX + 180f, centerY),
                        color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                        strokeWidth = 2f
                    )
                )
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.LINE,
                        start = Offset(centerX, centerY - 180f),
                        end = Offset(centerX, centerY + 180f),
                        color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                        strokeWidth = 2f
                    )
                )
                list.add(
                    CanvasElement.TextElement(
                        text = "SS LOGO GRID",
                        position = Offset(centerX - 90f, centerY + 240f),
                        color = Color(0xFFD4AF37),
                        fontSize = 22f
                    )
                )
                _elements.value = list
            }
            "UI Wireframe" -> {
                _paperStyle.value = CanvasPaperStyle.GRID_PAPER
                val list = mutableListOf<CanvasElement>()
                // Phone frame
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE,
                        start = Offset(centerX - 160f, centerY - 280f),
                        end = Offset(centerX + 160f, centerY + 280f),
                        color = Color(0xFF0A1931),
                        strokeWidth = 8f
                    )
                )
                // App bar
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE_FILLED,
                        start = Offset(centerX - 160f, centerY - 280f),
                        end = Offset(centerX + 160f, centerY - 220f),
                        color = Color(0xFF0A1931),
                        strokeWidth = 2f
                    )
                )
                // Banner box
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE,
                        start = Offset(centerX - 140f, centerY - 190f),
                        end = Offset(centerX + 140f, centerY - 70f),
                        color = Color(0xFF00A896),
                        strokeWidth = 4f
                    )
                )
                // Text placeholder
                list.add(
                    CanvasElement.TextElement(
                        text = "APP WIREFRAME",
                        position = Offset(centerX - 110f, centerY - 250f),
                        color = Color(0xFFFFFFFF),
                        fontSize = 18f
                    )
                )
                _elements.value = list
            }
            "Moodboard" -> {
                _paperStyle.value = CanvasPaperStyle.WARM_PARCHMENT
                val list = mutableListOf<CanvasElement>()
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE_FILLED,
                        start = Offset(centerX - 180f, centerY - 150f),
                        end = Offset(centerX - 60f, centerY - 30f),
                        color = Color(0xFF0A1931),
                        strokeWidth = 2f
                    )
                )
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE_FILLED,
                        start = Offset(centerX - 40f, centerY - 150f),
                        end = Offset(centerX + 80f, centerY - 30f),
                        color = Color(0xFFD4AF37),
                        strokeWidth = 2f
                    )
                )
                list.add(
                    CanvasElement.ShapeElement(
                        shapeType = ShapeType.RECTANGLE_FILLED,
                        start = Offset(centerX + 100f, centerY - 150f),
                        end = Offset(centerX + 180f, centerY - 30f),
                        color = Color(0xFF00A896),
                        strokeWidth = 2f
                    )
                )
                list.add(
                    CanvasElement.TextElement(
                        text = "BRAND COLOR PALETTE",
                        position = Offset(centerX - 140f, centerY + 30f),
                        color = Color(0xFF0A1931),
                        fontSize = 20f
                    )
                )
                _elements.value = list
            }
        }
    }

    // Export current drawing to Bitmap for image sharing or saving to device gallery
    fun createBitmap(width: Int, height: Int): Bitmap {
        val safeW = if (width > 0) width else 800
        val safeH = if (height > 0) height else 1200
        val bitmap = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        // Draw background
        canvas.drawColor(_paperStyle.value.backgroundColor.toArgb())

        val paint = Paint().apply {
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        for (element in _elements.value) {
            when (element) {
                is CanvasElement.StrokeElement -> {
                    paint.style = Paint.Style.STROKE
                    paint.color = element.color.toArgb()
                    paint.alpha = (element.alpha * 255).toInt().coerceIn(0, 255)
                    paint.strokeWidth = element.strokeWidth

                    if (element.points.size > 1) {
                        val path = AndroidPath()
                        path.moveTo(element.points[0].x, element.points[0].y)
                        for (i in 1 until element.points.size) {
                            val p = element.points[i]
                            path.lineTo(p.x, p.y)
                        }
                        canvas.drawPath(path, paint)
                    } else if (element.points.size == 1) {
                        val p = element.points[0]
                        canvas.drawCircle(p.x, p.y, element.strokeWidth / 2f, paint)
                    }
                }
                is CanvasElement.ShapeElement -> {
                    paint.color = element.color.toArgb()
                    paint.alpha = (element.alpha * 255).toInt().coerceIn(0, 255)
                    paint.strokeWidth = element.strokeWidth

                    val left = minOf(element.start.x, element.end.x)
                    val top = minOf(element.start.y, element.end.y)
                    val right = maxOf(element.start.x, element.end.x)
                    val bottom = maxOf(element.start.y, element.end.y)

                    when (element.shapeType) {
                        ShapeType.LINE -> {
                            paint.style = Paint.Style.STROKE
                            canvas.drawLine(element.start.x, element.start.y, element.end.x, element.end.y, paint)
                        }
                        ShapeType.ARROW -> {
                            paint.style = Paint.Style.STROKE
                            canvas.drawLine(element.start.x, element.start.y, element.end.x, element.end.y, paint)
                            // Arrowhead
                            val angle = atan2((element.end.y - element.start.y).toDouble(), (element.end.x - element.start.x).toDouble())
                            val arrowLength = element.strokeWidth * 3f + 10f
                            val p1X = element.end.x - arrowLength * cos(angle - Math.PI / 6).toFloat()
                            val p1Y = element.end.y - arrowLength * sin(angle - Math.PI / 6).toFloat()
                            val p2X = element.end.x - arrowLength * cos(angle + Math.PI / 6).toFloat()
                            val p2Y = element.end.y - arrowLength * sin(angle + Math.PI / 6).toFloat()
                            canvas.drawLine(element.end.x, element.end.y, p1X, p1Y, paint)
                            canvas.drawLine(element.end.x, element.end.y, p2X, p2Y, paint)
                        }
                        ShapeType.RECTANGLE -> {
                            paint.style = Paint.Style.STROKE
                            canvas.drawRect(left, top, right, bottom, paint)
                        }
                        ShapeType.RECTANGLE_FILLED -> {
                            paint.style = Paint.Style.FILL
                            canvas.drawRect(left, top, right, bottom, paint)
                        }
                        ShapeType.CIRCLE -> {
                            paint.style = Paint.Style.STROKE
                            canvas.drawOval(left, top, right, bottom, paint)
                        }
                        ShapeType.CIRCLE_FILLED -> {
                            paint.style = Paint.Style.FILL
                            canvas.drawOval(left, top, right, bottom, paint)
                        }
                        ShapeType.NONE -> {}
                    }
                }
                is CanvasElement.TextElement -> {
                    paint.style = Paint.Style.FILL
                    paint.color = element.color.toArgb()
                    paint.textSize = element.fontSize * 2.2f
                    paint.isFakeBoldText = element.isBold
                    canvas.drawText(element.text, element.position.x, element.position.y, paint)
                }
            }
        }
        return bitmap
    }
}
