package com.example.upisplitter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Int = 96) {
    Surface(
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                val blueBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0052D4), Color(0xFF0072FF), Color(0xFF00C6FF))
                )
                val greenBrush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00C853), Color(0xFF00E676), Color(0xFF69F0AE))
                )

                // --- TOP BLUE ARROW (S-Top) ---
                val blueArrow = Path().apply {
                    // Center fold
                    moveTo(w * 0.52f, h * 0.52f)
                    lineTo(w * 0.36f, h * 0.36f)
                    
                    // Outer curve around top-left
                    cubicTo(w * 0.08f, h * 0.22f, w * 0.22f, h * 0.06f, w * 0.54f, h * 0.06f)
                    cubicTo(w * 0.70f, h * 0.06f, w * 0.76f, h * 0.10f, w * 0.78f, h * 0.12f)
                    
                    // Arrowhead at top-right
                    lineTo(w * 0.72f, h * 0.02f)
                    lineTo(w * 0.94f, h * 0.18f)
                    lineTo(w * 0.78f, h * 0.32f)
                    lineTo(w * 0.76f, h * 0.22f)
                    
                    // Inner curve sweeping back
                    cubicTo(w * 0.62f, h * 0.20f, w * 0.36f, h * 0.22f, w * 0.36f, h * 0.36f)
                    cubicTo(w * 0.36f, h * 0.44f, w * 0.44f, h * 0.50f, w * 0.52f, h * 0.52f)
                    close()
                }
                drawPath(path = blueArrow, brush = blueBrush)

                // --- BOTTOM GREEN ARROW (S-Bottom) ---
                val greenArrow = Path().apply {
                    // Center fold
                    moveTo(w * 0.48f, h * 0.48f)
                    lineTo(w * 0.64f, h * 0.64f)
                    
                    // Outer curve around bottom-right
                    cubicTo(w * 0.92f, h * 0.78f, w * 0.78f, h * 0.94f, w * 0.46f, h * 0.94f)
                    cubicTo(w * 0.30f, h * 0.94f, w * 0.24f, h * 0.90f, w * 0.22f, h * 0.88f)
                    
                    // Arrowhead at bottom-left
                    lineTo(w * 0.28f, h * 0.98f)
                    lineTo(w * 0.06f, h * 0.82f)
                    lineTo(w * 0.22f, h * 0.68f)
                    lineTo(w * 0.24f, h * 0.78f)
                    
                    // Inner curve sweeping back
                    cubicTo(w * 0.38f, h * 0.80f, w * 0.64f, h * 0.78f, w * 0.64f, h * 0.64f)
                    cubicTo(w * 0.64f, h * 0.56f, w * 0.56f, h * 0.50f, w * 0.48f, h * 0.48f)
                    close()
                }
                drawPath(path = greenArrow, brush = greenBrush)
            }
        }
    }
}

@Preview
@Composable
fun AppLogoPreview() {
    AppLogo(size = 120)
}
