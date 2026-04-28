package com.bananchiki.wakeup.ui.paywall

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onMonthlyClick: () -> Unit,
    onYearlyClick: () -> Unit,
    onRestoreClick: () -> Unit,
    monthlyPrice: String = "149 ₽/мес",
    yearlyPrice: String = "999 ₽/год"
) {
    var selectedPlan by remember { mutableStateOf("yearly") }

    val infiniteTransition = rememberInfiniteTransition(label = "starPulse")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    val scrollState = rememberScrollState()

    // Use theme-aware colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Star icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(starScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFC107),
                                Color(0xFFFF9800)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "WakeUp Pro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = onBackgroundColor
            )

            Text(
                text = "Просыпайся эффективнее",
                fontSize = 16.sp,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Features list
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val checkColor = Color(0xFF4CAF50)
                    FeatureRow("Безлимитные будильники", checkColor, onBackgroundColor)
                    FeatureRow("Все задания для пробуждения", checkColor, onBackgroundColor)
                    FeatureRow("Полная статистика и графики", checkColor, onBackgroundColor)
                    FeatureRow("Все темы оформления", checkColor, onBackgroundColor)
                    FeatureRow("Премиум мелодии", checkColor, onBackgroundColor)
                    FeatureRow("Все достижения", checkColor, onBackgroundColor)
                    FeatureRow("Без рекламы", checkColor, onBackgroundColor)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Plan selector
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly plan
                PlanCard(
                    title = "Месяц",
                    price = monthlyPrice,
                    subtitle = null,
                    isSelected = selectedPlan == "monthly",
                    onClick = { selectedPlan = "monthly" },
                    modifier = Modifier.weight(1f),
                    accentColor = primaryColor,
                    textColor = onBackgroundColor,
                    subtextColor = onSurfaceVariant,
                    cardColor = surfaceColor
                )

                // Yearly plan
                PlanCard(
                    title = "Год",
                    price = yearlyPrice,
                    subtitle = "Экономия 44%",
                    isSelected = selectedPlan == "yearly",
                    onClick = { selectedPlan = "yearly" },
                    modifier = Modifier.weight(1f),
                    isBestValue = true,
                    accentColor = primaryColor,
                    textColor = onBackgroundColor,
                    subtextColor = onSurfaceVariant,
                    cardColor = surfaceColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA button
            Button(
                onClick = {
                    if (selectedPlan == "monthly") onMonthlyClick()
                    else onYearlyClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Попробовать бесплатно",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "3 дня бесплатно",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore purchases
            Text(
                text = "Восстановить покупку",
                fontSize = 14.sp,
                color = onSurfaceVariant,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onRestoreClick() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Legal links
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Условия",
                    fontSize = 12.sp,
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* TODO: Terms */ }
                )
                Text(
                    text = "Политика конфиденц.",
                    fontSize = 12.sp,
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { /* TODO: Privacy */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureRow(text: String, accentColor: Color, textColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            fontSize = 15.sp,
            color = textColor
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    subtitle: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBestValue: Boolean = false,
    accentColor: Color,
    textColor: Color,
    subtextColor: Color,
    cardColor: Color
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.border(
                        2.dp,
                        accentColor,
                        RoundedCornerShape(16.dp)
                    ) else Modifier
                )
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 1.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = subtextColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
        }

        // "Best value" badge
        if (isBestValue) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "ВЫГОДНО",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
