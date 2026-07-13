package br.com.ysenerbyte.comandospro.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ysenerbyte.comandospro.ui.theme.DeepNavy
import br.com.ysenerbyte.comandospro.ui.theme.ElectricBlue
import br.com.ysenerbyte.comandospro.ui.theme.SignalGreen

@Composable
fun SplashCover() {
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.82f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "splashScale"
    )
    LaunchedEffect(Unit) { started = true }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF123A55), DeepNavy),
                    radius = 1_100f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(Modifier.size((180 * scale).dp)) {
                val w = size.width
                val h = size.height
                drawRoundRect(
                    color = Color(0xFF263C49),
                    topLeft = Offset(w * 0.18f, h * 0.12f),
                    size = Size(w * 0.64f, h * 0.76f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFF405967), Color(0xFF1C2B34))),
                    topLeft = Offset(w * 0.27f, h * 0.25f),
                    size = Size(w * 0.46f, h * 0.40f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.045f)
                )
                drawRoundRect(
                    color = ElectricBlue,
                    topLeft = Offset(w * 0.36f, h * 0.34f),
                    size = Size(w * 0.28f, h * 0.18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.025f)
                )
                repeat(3) { index ->
                    val x = w * (0.32f + index * 0.18f)
                    drawCircle(Color(0xFFE6C46A), w * 0.035f, Offset(x, h * 0.18f))
                    drawCircle(Color(0xFFE6C46A), w * 0.035f, Offset(x, h * 0.82f))
                    drawLine(
                        color = Color(0xFF91A7B2),
                        start = Offset(x, h * 0.20f),
                        end = Offset(x, h * 0.26f),
                        strokeWidth = w * 0.018f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF91A7B2),
                        start = Offset(x, h * 0.64f),
                        end = Offset(x, h * 0.80f),
                        strokeWidth = w * 0.018f,
                        cap = StrokeCap.Round
                    )
                }
                val bolt = Path().apply {
                    moveTo(w * 0.53f, h * 0.34f)
                    lineTo(w * 0.43f, h * 0.51f)
                    lineTo(w * 0.51f, h * 0.51f)
                    lineTo(w * 0.45f, h * 0.66f)
                    lineTo(w * 0.61f, h * 0.45f)
                    lineTo(w * 0.53f, h * 0.45f)
                    close()
                }
                drawPath(bolt, SignalGreen)
                drawRoundRect(
                    color = ElectricBlue.copy(alpha = 0.5f),
                    topLeft = Offset(w * 0.08f, h * 0.04f),
                    size = Size(w * 0.84f, h * 0.92f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.14f),
                    style = Stroke(width = w * 0.015f)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "COMANDOS PRO",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.8.sp
            )
            Text(
                "3D",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = ElectricBlue,
                letterSpacing = 6.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Treinamento industrial virtual",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
