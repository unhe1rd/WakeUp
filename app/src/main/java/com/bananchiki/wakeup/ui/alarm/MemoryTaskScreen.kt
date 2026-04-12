package com.bananchiki.wakeup.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val memoryColors = listOf(
    Color(0xFFE57373), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD),
    Color(0xFF7986CB), Color(0xFF64B5F6), Color(0xFF4FC3F7), Color(0xFF4DD0E1)
)

@Composable
fun MemoryTaskScreen(onComplete: () -> Unit) {
    val cards = remember { (memoryColors + memoryColors).shuffled() }
    val revealed = remember { mutableStateListOf<Boolean>().apply { repeat(16) { add(true) } } }
    val matched = remember { mutableStateListOf<Boolean>().apply { repeat(16) { add(false) } } }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var disabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000)
        for (i in revealed.indices) {
            revealed[i] = false
        }
        disabled = false
    }

    LaunchedEffect(matched.toList()) {
        if (matched.all { it }) {
            delay(500)
            onComplete()
        }
    }

    val chunkedCards = cards.chunked(4)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var overallIndex = 0
        for (rowArgs in chunkedCards) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (color in rowArgs) {
                    val index = overallIndex
                    val isRevealed = revealed[index]
                    val isMatched = matched[index]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .alpha(if (isMatched) 0.5f else 1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRevealed || isMatched) color else Color.DarkGray)
                            .clickable(enabled = !isMatched && !isRevealed && !disabled) {
                                revealed[index] = true
                                if (selectedIndex == null) {
                                    selectedIndex = index
                                } else {
                                    val firstIndex = selectedIndex!!
                                    if (cards[firstIndex] == cards[index]) {
                                        matched[firstIndex] = true
                                        matched[index] = true
                                        selectedIndex = null
                                    } else {
                                        disabled = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isMatched) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Matched", tint = Color.White)
                        }
                    }
                    overallIndex++
                }
            }
        }
    }

    if (disabled) {
        LaunchedEffect(Unit) {
            delay(800)
            val first = selectedIndex
            if (first != null) {
                revealed[first] = false
                revealed[revealed.indexOfLast { it && !matched[revealed.indexOf(it)] }] = false
                // Find all that are revealed but not matched
                for (i in revealed.indices) {
                    if (revealed[i] && !matched[i]) {
                        revealed[i] = false
                    }
                }
            }
            selectedIndex = null
            disabled = false
        }
    }
}
