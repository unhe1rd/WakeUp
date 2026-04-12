package com.bananchiki.wakeup.ui.alarm

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.data.tasks.generateMathTask
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AlarmScreen(onDismiss: () -> Unit) {
    var currentTask by remember{ mutableStateOf(generateMathTask())}
    val scope = rememberCoroutineScope()
    var wrongClickedNumber by remember {mutableStateOf<Int?>(null)}
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTask.question,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentTask.cards.size){
                    Cards(
                        number = currentTask.cards[it],
                        isError = (currentTask.cards[it] == wrongClickedNumber),
                        onCardClick = {
                            if (currentTask.cards[it] == currentTask.answer){
                                onDismiss()
                            } else {
                                scope.launch{
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    wrongClickedNumber = currentTask.cards[it]
                                    delay(700)
                                    currentTask = generateMathTask()
                                    wrongClickedNumber = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Cards(number: Int, isError: Boolean = false, onCardClick: () -> Unit){
    Card(
        onClick = { onCardClick() },
        shape = RoundedCornerShape(40.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if(!isError){
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.error
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)

    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .size(30.dp)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

@Preview
@Composable
fun Preview(){
    AlarmScreen({})
}
