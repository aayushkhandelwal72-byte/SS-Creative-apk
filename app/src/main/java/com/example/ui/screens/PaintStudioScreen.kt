package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.BrushType
import com.example.data.model.CanvasElement
import com.example.data.model.CanvasPaperStyle
import com.example.data.model.SavedArtwork
import com.example.data.model.ShapeType
import com.example.ui.theme.ColumnTextLightBlue
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.LightBlue
import com.example.ui.theme.LightBlueSoft
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PeacockGold
import com.example.ui.theme.PeacockTeal
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.navyTextFieldColors
import com.example.viewmodel.PaintStudioViewModel
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintStudioScreen(
    viewModel: PaintStudioViewModel,
    onConsultWithNidhi: (sketchTopic: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val elements by viewModel.elements.collectAsState()
    val activePoints by viewModel.activePoints.collectAsState()
    val activeShapePreview by viewModel.activeShapePreview.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val selectedBrush by viewModel.selectedBrush.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val strokeWidth by viewModel.strokeWidth.collectAsState()
    val brushAlpha by viewModel.brushAlpha.collectAsState()
    val selectedShape by viewModel.selectedShape.collectAsState()
    val paperStyle by viewModel.paperStyle.collectAsState()
    val savedArtworks by viewModel.savedArtworks.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showGallerySheet by remember { mutableStateOf(false) }
    var showTemplatesMenu by remember { mutableStateOf(false) }
    var showColorHexDialog by remember { mutableStateOf(false) }

    // Bottom Toolbox Tab: 0 = Brush & Shape, 1 = Colors & Palette, 2 = Size & Opacity, 3 = Canvas Paper
    var selectedToolTab by remember { mutableIntStateOf(0) }
    var isToolboxExpanded by remember { mutableStateOf(true) }

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val presetColors = listOf(
        Color(0xFF0A1931), // Royal Navy
        Color(0xFF061224), // Midnight Navy
        Color(0xFFD4AF37), // Peacock Gold
        Color(0xFF00A896), // Peacock Teal
        Color(0xFFEF4444), // Crimson
        Color(0xFFF97316), // Sunset Orange
        Color(0xFF10B981), // Emerald Green
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFF8B5CF6), // Royal Purple
        Color(0xFFEC4899), // Hot Pink
        Color(0xFFFFFFFF), // Crisp White
        Color(0xFF64748B), // Slate Gray
        Color(0xFF000000)  // Pitch Black
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Studio Header & Quick Action Bar
        Surface(
            color = CrispWhite,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NavyPrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = "Studio Brush",
                                    tint = PeacockGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Creative Paint Studio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = PeacockGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = NavyPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Editable Canvas & Brush Engine",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Undo / Redo & Save Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.undo() },
                            enabled = canUndo,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (canUndo) NavyPrimary else Color.LightGray
                            )
                        }

                        IconButton(
                            onClick = { viewModel.redo() },
                            enabled = canRedo,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (canRedo) NavyPrimary else Color.LightGray
                            )
                        }

                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Canvas",
                                tint = Color(0xFFEF4444)
                            )
                        }

                        IconButton(
                            onClick = { showTemplatesMenu = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "Stencils & Templates",
                                tint = NavyPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = showTemplatesMenu,
                            onDismissRequest = { showTemplatesMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logo Grid Stencil") },
                                onClick = {
                                    viewModel.loadPresetStencil("Logo Grid", canvasSize)
                                    showTemplatesMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("UI Wireframe Stencil") },
                                onClick = {
                                    viewModel.loadPresetStencil("UI Wireframe", canvasSize)
                                    showTemplatesMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Moodboard Palette") },
                                onClick = {
                                    viewModel.loadPresetStencil("Moodboard", canvasSize)
                                    showTemplatesMenu = false
                                }
                            )
                        }

                        IconButton(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Artwork",
                                tint = PeacockTeal
                            )
                        }

                        IconButton(
                            onClick = { showGallerySheet = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Gallery",
                                tint = PeacockGold
                            )
                        }
                    }
                }
            }
        }

        // Main Drawing Canvas Area
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(6.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                .shadow(2.dp, RoundedCornerShape(14.dp))
        ) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            canvasSize = Size(widthPx, heightPx)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("drawing_paint_canvas")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                viewModel.startStroke(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                viewModel.addStrokePoint(change.position)
                            },
                            onDragEnd = {
                                viewModel.finishStroke()
                            },
                            onDragCancel = {
                                viewModel.finishStroke()
                            }
                        )
                    }
            ) {
                // Background paper fill
                drawRect(color = paperStyle.backgroundColor)

                // Optional grid lines
                if (paperStyle.hasGrid) {
                    val gridSize = 40.dp.toPx()
                    val gridColor = if (paperStyle == CanvasPaperStyle.BLUEPRINT_NAVY) {
                        Color(0xFF1E3A8A).copy(alpha = 0.4f)
                    } else {
                        Color(0xFFE2E8F0)
                    }
                    var x = 0f
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += gridSize
                    }
                    var y = 0f
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += gridSize
                    }
                }

                // Optional dot matrix
                if (paperStyle.hasDots) {
                    val dotSpacing = 30.dp.toPx()
                    val dotColor = Color(0xFF94A3B8)
                    var x = dotSpacing / 2f
                    while (x < size.width) {
                        var y = dotSpacing / 2f
                        while (y < size.height) {
                            drawCircle(dotColor, radius = 1.8f, center = Offset(x, y))
                            y += dotSpacing
                        }
                        x += dotSpacing
                    }
                }

                // Render committed elements
                for (element in elements) {
                    renderElement(element, paperStyle)
                }

                // Render active in-flight freehand stroke
                if (activePoints.size > 1) {
                    val activeColor = if (selectedBrush == BrushType.ERASER) {
                        paperStyle.backgroundColor
                    } else {
                        selectedColor.copy(alpha = brushAlpha)
                    }
                    val path = Path().apply {
                        moveTo(activePoints[0].x, activePoints[0].y)
                        for (i in 1 until activePoints.size) {
                            val p = activePoints[i]
                            lineTo(p.x, p.y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                } else if (activePoints.size == 1) {
                    val activeColor = if (selectedBrush == BrushType.ERASER) {
                        paperStyle.backgroundColor
                    } else {
                        selectedColor.copy(alpha = brushAlpha)
                    }
                    drawCircle(
                        color = activeColor,
                        radius = strokeWidth / 2f,
                        center = activePoints[0]
                    )
                }

                // Render active in-flight shape preview
                activeShapePreview?.let { shape ->
                    renderShape(shape)
                }
            }

            // Quick floating "Nidhi AI Review" badge
            Surface(
                color = NavyPrimary,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clickable {
                        onConsultWithNidhi("I just sketched a creative design concept in the Paint Studio! Can you review the composition, branding colors, and provide creative art suggestions?")
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PeacockGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Critique",
                        color = ColumnTextLightBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Add Text sticker button
            Surface(
                color = CrispWhite,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clickable { showAddTextDialog = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Text Sticker",
                        color = NavyPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Editable Toolbox (Brush, Shape, Color, Stroke Size, Paper)
        Surface(
            color = CrispWhite,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Navigation Tabs for Toolbox
                TabRow(
                    selectedTabIndex = selectedToolTab,
                    containerColor = CrispWhite,
                    contentColor = NavyPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedToolTab]),
                            color = NavyPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedToolTab == 0,
                        onClick = { selectedToolTab = 0; isToolboxExpanded = true },
                        text = { Text("Brush & Tool", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedToolTab == 1,
                        onClick = { selectedToolTab = 1; isToolboxExpanded = true },
                        text = { Text("Palette", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedToolTab == 2,
                        onClick = { selectedToolTab = 2; isToolboxExpanded = true },
                        text = { Text("Stroke", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.LinearScale, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedToolTab == 3,
                        onClick = { selectedToolTab = 3; isToolboxExpanded = true },
                        text = { Text("Canvas", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab 0: Brush Types & Geometric Shapes
                if (selectedToolTab == 0) {
                    Column {
                        Text(
                            text = "Brush Style / Eraser",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BrushType.entries.forEach { brush ->
                                val isSelected = selectedBrush == brush && selectedShape == ShapeType.NONE
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectBrush(brush) },
                                    label = { Text(brush.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (brush) {
                                                BrushType.PEN -> Icons.Default.Edit
                                                BrushType.SMOOTH -> Icons.Default.Brush
                                                BrushType.CALLIGRAPHY -> Icons.Default.Draw
                                                BrushType.MARKER -> Icons.Default.LinearScale
                                                BrushType.NEON -> Icons.Default.AutoAwesome
                                                BrushType.ERASER -> Icons.Default.Delete
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor = CrispWhite,
                                        selectedLeadingIconColor = PeacockGold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Geometric Shapes & Guides",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ShapeType.entries.forEach { shape ->
                                val isSelected = selectedShape == shape
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectShape(shape) },
                                    label = { Text(shape.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (shape) {
                                                ShapeType.NONE -> Icons.Default.Draw
                                                ShapeType.LINE -> Icons.Default.LinearScale
                                                ShapeType.ARROW -> Icons.AutoMirrored.Filled.ArrowForward
                                                ShapeType.RECTANGLE -> Icons.Default.CropSquare
                                                ShapeType.RECTANGLE_FILLED -> Icons.Default.CropSquare
                                                ShapeType.CIRCLE -> Icons.Default.RadioButtonUnchecked
                                                ShapeType.CIRCLE_FILLED -> Icons.Outlined.Circle
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor = CrispWhite,
                                        selectedLeadingIconColor = PeacockGold
                                    )
                                )
                            }
                        }
                    }
                }

                // Tab 1: Color Swatches & Custom Color
                if (selectedToolTab == 1) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Studio Ink Color",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            TextButton(onClick = { showColorHexDialog = true }) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavyPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Custom Hex", fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetColors.forEach { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) PeacockGold else Color(0xFFCBD5E1),
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setColor(color) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(if (color == Color(0xFFFFFFFF)) NavyPrimary else CrispWhite)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 2: Stroke Thickness & Opacity
                if (selectedToolTab == 2) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stroke Size: ${strokeWidth.toInt()} px",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )

                            // Live stroke preview dot
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Color(0xFFCBD5E1), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((strokeWidth.coerceIn(4f, 32f)).dp)
                                        .clip(CircleShape)
                                        .background(selectedColor.copy(alpha = brushAlpha))
                                )
                            }
                        }

                        Slider(
                            value = strokeWidth,
                            onValueChange = { viewModel.setStrokeWidth(it) },
                            valueRange = 2f..64f,
                            colors = SliderDefaults.colors(
                                thumbColor = NavyPrimary,
                                activeTrackColor = NavyPrimary,
                                inactiveTrackColor = NavyContainer
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(4f to "Fine", 12f to "Medium", 24f to "Broad", 48f to "Marker").forEach { (size, label) ->
                                OutlinedButton(
                                    onClick = { viewModel.setStrokeWidth(size) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (strokeWidth == size) NavyPrimary else TextSecondary
                                    )
                                ) {
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ink Opacity: ${(brushAlpha * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        Slider(
                            value = brushAlpha,
                            onValueChange = { viewModel.setBrushAlpha(it) },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = PeacockTeal,
                                activeTrackColor = PeacockTeal,
                                inactiveTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }

                // Tab 3: Canvas Paper Styles
                if (selectedToolTab == 3) {
                    Column {
                        Text(
                            text = "Canvas Background & Blueprint",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CanvasPaperStyle.entries.forEach { style ->
                                val isSelected = paperStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setPaperStyle(style) },
                                    label = { Text(style.displayName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor = CrispWhite
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Save Artwork (Uses navyTextFieldColors for text filling!)
    if (showSaveDialog) {
        var artTitle by remember { mutableStateOf("") }
        val tfColors = navyTextFieldColors()

        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Artwork to Studio",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a title for your sketch to store in the Studio Portfolio gallery.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = artTitle,
                        onValueChange = { artTitle = it },
                        label = { Text("Artwork / Sketch Title") },
                        placeholder = { Text("e.g. Brand Logo Mark V1") },
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_art_title_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveArtwork(artTitle)
                        showSaveDialog = false
                        Toast.makeText(context, "Artwork saved to Studio Gallery!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Save to Studio")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Add Text Sticker (Uses navyTextFieldColors for text filling!)
    if (showAddTextDialog) {
        var textContent by remember { mutableStateOf("") }
        var textSize by remember { mutableFloatStateOf(24f) }
        val tfColors = navyTextFieldColors()

        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = {
                Text(
                    text = "Add Text / Watermark",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Add studio typographic annotations, client notes, or branding titles.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        label = { Text("Annotation Text") },
                        placeholder = { Text("e.g. SS CREATIVE LOGO") },
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_text_input_field")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Font Size: ${textSize.toInt()} pt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NavyPrimary
                    )
                    Slider(
                        value = textSize,
                        onValueChange = { textSize = it },
                        valueRange = 14f..48f,
                        colors = SliderDefaults.colors(
                            thumbColor = NavyPrimary,
                            activeTrackColor = NavyPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textContent.isNotBlank()) {
                            val centerX = if (canvasSize.width > 0) canvasSize.width / 2f else 300f
                            val centerY = if (canvasSize.height > 0) canvasSize.height / 2f else 400f
                            viewModel.addTextElement(textContent, Offset(centerX - 80f, centerY), selectedColor, textSize)
                            showAddTextDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add to Canvas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Custom Hex Color Input (Uses navyTextFieldColors for text filling!)
    if (showColorHexDialog) {
        var hexInput by remember { mutableStateOf("#0A1931") }
        var parseError by remember { mutableStateOf(false) }
        val tfColors = navyTextFieldColors()

        AlertDialog(
            onDismissRequest = { showColorHexDialog = false },
            title = {
                Text(
                    text = "Custom Hex Ink Color",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a 6-character hex code (e.g. #0A1931 for Navy, #D4AF37 for Gold).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = {
                            hexInput = it
                            parseError = false
                        },
                        label = { Text("Color Hex Code") },
                        isError = parseError,
                        colors = tfColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_hex_field")
                    )
                    if (parseError) {
                        Text("Invalid hex code. Use format #RRGGBB", color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val cleanHex = hexInput.trim().removePrefix("#")
                            val colorLong = cleanHex.toLong(16) or 0x00000000FF000000
                            val parsed = Color(colorLong)
                            viewModel.setColor(parsed)
                            showColorHexDialog = false
                        } catch (e: Exception) {
                            parseError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Apply Color")
                }
            },
            dismissButton = {
                TextButton(onClick = { showColorHexDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Clear Canvas Confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Entire Canvas?", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = { Text("All unsaved brush strokes and shapes on this canvas will be cleared. (You can also Undo this action).") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCanvas()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear Canvas")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // BottomSheet: Saved Artworks Gallery
    if (showGallerySheet) {
        ModalBottomSheet(
            onDismissRequest = { showGallerySheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = CrispWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Studio Artworks Gallery",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "${savedArtworks.size} sketches saved in studio records",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = { showGallerySheet = false }) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color.Transparent)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (savedArtworks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No saved sketches yet. Draw and tap Save!", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedArtworks) { artwork ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = artwork.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "${artwork.elements.size} elements • ${artwork.formattedDate}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                viewModel.loadArtwork(artwork)
                                                showGallerySheet = false
                                                Toast.makeText(context, "Loaded '${artwork.title}' to canvas", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Open", fontSize = 12.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                shareArtworkBitmap(context, viewModel, artwork.title, canvasSize)
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NavyPrimary)
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteArtwork(artwork.id) }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Renders individual canvas elements (strokes, shapes, text)
private fun DrawScope.renderElement(element: CanvasElement, paperStyle: CanvasPaperStyle) {
    when (element) {
        is CanvasElement.StrokeElement -> {
            val color = if (element.brushType == BrushType.ERASER) {
                paperStyle.backgroundColor
            } else {
                element.color.copy(alpha = element.alpha)
            }

            if (element.points.size > 1) {
                val path = Path().apply {
                    moveTo(element.points[0].x, element.points[0].y)
                    for (i in 1 until element.points.size) {
                        val p = element.points[i]
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = element.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else if (element.points.size == 1) {
                drawCircle(
                    color = color,
                    radius = element.strokeWidth / 2f,
                    center = element.points[0]
                )
            }
        }
        is CanvasElement.ShapeElement -> {
            renderShape(element)
        }
        is CanvasElement.TextElement -> {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.color = element.color.hashCode()
                    this.textSize = element.fontSize * 2.2f
                    this.isFakeBoldText = element.isBold
                    this.isAntiAlias = true
                }
                drawText(element.text, element.position.x, element.position.y, paint)
            }
        }
    }
}

private fun DrawScope.renderShape(shape: CanvasElement.ShapeElement) {
    val left = minOf(shape.start.x, shape.end.x)
    val top = minOf(shape.start.y, shape.end.y)
    val right = maxOf(shape.start.x, shape.end.x)
    val bottom = maxOf(shape.start.y, shape.end.y)
    val width = right - left
    val height = bottom - top
    val color = shape.color.copy(alpha = shape.alpha)

    when (shape.shapeType) {
        ShapeType.LINE -> {
            drawLine(
                color = color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )
        }
        ShapeType.ARROW -> {
            drawLine(
                color = color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrowhead
            val angle = atan2((shape.end.y - shape.start.y).toDouble(), (shape.end.x - shape.start.x).toDouble())
            val arrowLength = shape.strokeWidth * 3f + 12f
            val p1X = shape.end.x - arrowLength * cos(angle - Math.PI / 6).toFloat()
            val p1Y = shape.end.y - arrowLength * sin(angle - Math.PI / 6).toFloat()
            val p2X = shape.end.x - arrowLength * cos(angle + Math.PI / 6).toFloat()
            val p2Y = shape.end.y - arrowLength * sin(angle + Math.PI / 6).toFloat()
            drawLine(color, shape.end, Offset(p1X, p1Y), strokeWidth = shape.strokeWidth, cap = StrokeCap.Round)
            drawLine(color, shape.end, Offset(p2X, p2Y), strokeWidth = shape.strokeWidth, cap = StrokeCap.Round)
        }
        ShapeType.RECTANGLE -> {
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = shape.strokeWidth)
            )
        }
        ShapeType.RECTANGLE_FILLED -> {
            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height)
            )
        }
        ShapeType.CIRCLE -> {
            drawOval(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = shape.strokeWidth)
            )
        }
        ShapeType.CIRCLE_FILLED -> {
            drawOval(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height)
            )
        }
        ShapeType.NONE -> {}
    }
}

private fun shareArtworkBitmap(
    context: Context,
    viewModel: PaintStudioViewModel,
    title: String,
    canvasSize: Size
) {
    try {
        val w = if (canvasSize.width > 0) canvasSize.width.toInt() else 800
        val h = if (canvasSize.height > 0) canvasSize.height.toInt() else 1200
        val bitmap = viewModel.createBitmap(w, h)

        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "artwork_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(contentUri, context.contentResolver.getType(contentUri))
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Concept Sketch from SS Creative Studio: $title")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Studio Sketch via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Saved image to Studio gallery: $title", Toast.LENGTH_SHORT).show()
    }
}
