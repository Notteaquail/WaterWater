package com.example.waterwater.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.waterwater.R
import com.example.waterwater.model.CatBreed
import com.example.waterwater.model.toEmoji
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 纯净的背景场景：仅包含吉卜力背景图和阳光呼吸效果
 */
@Composable
fun MainScene(
    scrollOffsetProvider: () -> Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SunlightEffect")
    val sunAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "SunAlpha"
    )

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFD7CCC8))) {
        // 1. 背景层 (视差系数 0.3)
        // 取消了之前的 scale 缩放，使其保持原图比例
        Image(
            painter = painterResource(id = R.drawable.bg_living_room),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                translationY = -scrollOffsetProvider() * 0.3f
            },
            contentScale = ContentScale.Crop
        )

        // 2. 阳光层 (视差系数 0.3，与背景同步)
        Box(modifier = Modifier.fillMaxSize().graphicsLayer {
            translationY = -scrollOffsetProvider() * 0.3f
            alpha = sunAlpha
        }.background(
            Brush.linearGradient(
                colors = listOf(Color(0xFFFFE0B2), Color.Transparent),
                start = Offset(1200f, 0f), end = Offset(0f, 1500f)
            )
        ))
    }
}

/**
 * 动态猫咪组件定义 (仅作为定义，供 HomeScreen 顶层调用)
 */
@Composable
fun DynamicCat(
    breed: CatBreed, 
    catScale: Float = 1.0f,
    isThinkingEnabled: Boolean = true
) {
    var isThinking by remember { mutableStateOf(false) }
    var thoughtEmoji by remember { mutableStateOf("🐱") }
    val thoughtIcons = listOf("🐟", "🧶", "🖱️", "🥛", "☀️", "🦋", "📦", "🍗", "✨")

    if (isThinkingEnabled) {
        LaunchedEffect(Unit) {
            while (true) {
                delay(Random.nextLong(10000, 25000))
                thoughtEmoji = thoughtIcons.random()
                isThinking = true
                delay(4000) 
                isThinking = false
            }
        }
    }

    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        // 加载动画
        val lottieRes = when(breed) {
            CatBreed.BLACK_WHITE_LONG -> R.raw.cat_sleepy
            CatBreed.GOLDEN_LONG -> R.raw.cat_idle
            CatBreed.CREAM_BRITISH -> R.raw.cat_hungry
            CatBreed.MUNCHKIN_SHORT -> R.raw.cat_playful
            CatBreed.ONE_EYE_GOLDEN -> R.raw.spooky_cat
        }
        
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                scaleX = catScale
                scaleY = catScale
            },
            contentAlignment = Alignment.Center
        ) {
            if (composition != null) {
                LottieAnimation(composition, progress = { progress }, modifier = Modifier.fillMaxSize())
            } else {
                Text(text = "🐱", fontSize = 44.sp)
            }
        }

        // 思考气泡
        if (isThinking && isThinkingEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = ((-25) * catScale).dp) 
                    .shadow(1.dp, CircleShape)
                    .background(Color(0xFFFFF9F0).copy(alpha = 0.95f), CircleShape)
                    .border(1.dp, Color(0xFFD7CCC8), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = thoughtEmoji, fontSize = 16.sp)
            }
        }
    }
}
