package com.bananchiki.wakeup.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.ui.theme.DarkText
import com.bananchiki.wakeup.ui.theme.GrayMedium
import com.bananchiki.wakeup.ui.theme.White

@Composable
fun AlarmGoalsTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Alarm", "Goals")

    Row(
        modifier = Modifier
            .padding(horizontal = 60.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTab
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) White else Color.Transparent,
                animationSpec = tween(200),
                label = "tabBg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) DarkText else GrayMedium
                )
            }
        }
    }
}
