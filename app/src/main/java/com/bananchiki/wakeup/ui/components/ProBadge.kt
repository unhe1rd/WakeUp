package com.bananchiki.wakeup.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProBadge(
    isPremium: Boolean,
    onClick: () -> Unit
) {
    if (isPremium) {
        // Static premium badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF7C4DFF),
                            Color(0xFF536DFE)
                        )
                    )
                )
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Premium active",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "PRO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    } else {
        // Animated pulsing badge for free users
        val infiniteTransition = rememberInfiniteTransition(label = "proPulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "proScale"
        )

        Row(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFFC107),
                            Color(0xFFFF9800)
                        )
                    )
                )
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Upgrade to PRO",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "PRO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
