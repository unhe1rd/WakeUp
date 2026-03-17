package com.bananchiki.wakeup.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.ui.theme.DarkText
import com.bananchiki.wakeup.ui.theme.GrayText
import java.util.Calendar

@Composable
fun GreetingHeader() {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> "Good Night"
            hour < 12 -> "Good Morning"
            hour < 18 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "☀️", fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Mikhail Ulanov",
            style = MaterialTheme.typography.headlineLarge,
            color = DarkText
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Let's build your habits",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText
        )
    }
}
