package com.bananchiki.wakeup.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.bananchiki.wakeup.ui.theme.*

@Composable
fun BottomNavBar(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Background bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .align(Alignment.BottomCenter),
            color = White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(Icons.Filled.Home, contentDescription = "Home", tint = Amber, modifier = Modifier.size(24.dp))
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(width = 16.dp, height = 3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Amber)
                    )
                }

                // Mail
                Icon(
                    Icons.Outlined.MailOutline,
                    contentDescription = "Messages",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )

                // Spacer for FAB
                Spacer(modifier = Modifier.width(56.dp))

                // Clock
                Icon(
                    Icons.Outlined.Alarm,
                    contentDescription = "Clock",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )

                // Settings
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = NavIconInactive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // FAB in center
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(56.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            containerColor = Amber,
            contentColor = White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Alarm",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
