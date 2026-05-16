package com.bananchiki.wakeup.ui.alarm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananchiki.wakeup.data.tasks.generateMathTask
import com.bananchiki.wakeup.ui.theme.Amber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.chunked

@Composable
fun AiGreetingTaskScreen(onComplete: () -> Unit, greetingText: String) {
    var userInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = greetingText,
            color = Color.Black.copy(alpha = 0.9f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Введите текст выше") },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.9f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.7f),
                errorContainerColor = Color(0xFFFFEBEB),

                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Red,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Black,
                cursorColor = Color.Black,

                focusedLabelColor = Color.DarkGray,
                unfocusedLabelColor = Color.DarkGray,
                errorLabelColor = Color.Red
            )
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (userInput.trim().equals(greetingText.trim(), ignoreCase = true)) {
                    onComplete()
                } else {
                    isError = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Amber,
                contentColor = Color(0xFF1A1A1A)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                text = "Выключить",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}