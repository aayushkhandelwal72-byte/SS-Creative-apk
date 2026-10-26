package com.example.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class BrushType(val displayName: String, val description: String) {
    PEN("Pen", "Sharp precise sketching line"),
    SMOOTH("Brush", "Soft rounded studio brush"),
    CALLIGRAPHY("Chisel", "Expressive calligraphy angle"),
    MARKER("Highlighter", "Vibrant translucent highlighter"),
    NEON("Neon Glow", "Luminous glowing effect"),
    ERASER("Eraser", "Clean stroke eraser")
}

enum class ShapeType(val displayName: String) {
    NONE("Freehand"),
    LINE("Line"),
    ARROW("Arrow"),
    RECTANGLE("Rectangle"),
    RECTANGLE_FILLED("Filled Box"),
    CIRCLE("Circle"),
    CIRCLE_FILLED("Filled Oval")
}

enum class CanvasPaperStyle(val displayName: String, val backgroundColor: Color, val hasGrid: Boolean, val hasDots: Boolean) {
    CLEAN_WHITE("Studio White", Color(0xFFFFFFFF), false, false),
    BLUEPRINT_NAVY("Blueprint Navy", Color(0xFF0A1931), true, false),
    GRID_PAPER("Graph Grid", Color(0xFFF8FAFC), true, false),
    DOT_MATRIX("Dot Matrix", Color(0xFFF1F5F9), false, true),
    WARM_PARCHMENT("Parchment", Color(0xFFFDFBF7), false, false),
    DARK_SLATE("Slate Board", Color(0xFF1E293B), false, false)
}

sealed class CanvasElement {
    data class StrokeElement(
        val points: List<Offset>,
        val color: Color,
        val strokeWidth: Float,
        val alpha: Float = 1f,
        val brushType: BrushType = BrushType.SMOOTH
    ) : CanvasElement()

    data class ShapeElement(
        val shapeType: ShapeType,
        val start: Offset,
        val end: Offset,
        val color: Color,
        val strokeWidth: Float,
        val alpha: Float = 1f
    ) : CanvasElement()

    data class TextElement(
        val text: String,
        val position: Offset,
        val color: Color,
        val fontSize: Float = 20f,
        val isBold: Boolean = true
    ) : CanvasElement()
}

data class SavedArtwork(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val elements: List<CanvasElement>,
    val paperStyle: CanvasPaperStyle = CanvasPaperStyle.CLEAN_WHITE,
    val createdAt: Long = System.currentTimeMillis(),
    val formattedDate: String = "Today"
)
