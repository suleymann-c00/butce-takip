package com.example.butcetakip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.butcetakip.theme.ButceTakipTheme
import com.example.butcetakip.ui.main.FinanceViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.sin

@Composable
fun SplashWaveBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val waveColor = Color(0xFF00E676).copy(alpha = 0.15f)

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        val width = size.width
        val height = size.height
        
        for (i in 0..2) {
            val path = Path()
            val yOffset = height * (0.5f + (i * 0.15f))
            val amplitude = height * 0.08f * (1f - i * 0.2f)
            val frequency = 1f + i * 0.3f
            
            path.moveTo(0f, yOffset)
            
            val step = 15f
            var x = 0f
            while (x <= width) {
                val normalizedX = x / width
                val y = yOffset + sin((normalizedX * frequency * 2 * Math.PI) + phase * (1f + i * 0.3f)) * amplitude
                path.lineTo(x, y.toFloat())
                x += step
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            
            drawPath(
                path = path,
                color = waveColor,
                style = Fill
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        SplashWaveBackground()
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(100.dp).scale(scale.value),
                tint = Color(0xFF00E676)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Bütçe Takip",
                color = Color.White.copy(alpha = alpha.value),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

class MainActivity : ComponentActivity() {
  private val viewModel: FinanceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    setContent {
      val state by viewModel.uiState.collectAsStateWithLifecycle()
      ButceTakipTheme(appTheme = state.appTheme) { 
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
              var showSplash by remember { mutableStateOf(true) }
              if (showSplash) {
                  SplashScreen(onSplashFinished = { showSplash = false })
              } else {
                  AppNavigation(viewModel) 
              }
          } 
      }
    }
  }

  override fun onResume() {
    super.onResume()
    com.example.butcetakip.data.DataRepository.syncFromFirestore()
  }
}
