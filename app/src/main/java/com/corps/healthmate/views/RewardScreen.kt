package com.corps.healthmate.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RewardScreen(
    healthScore: Float = 75f,
    stepsProgress: Float = 60f,
    waterProgress: Float = 80f,
    sleepProgress: Float = 45f,
    activityProgress: Float = 90f
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Health Score Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Health Score",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgress(
                    percentage = healthScore,
                    size = 180.dp,
                    strokeWidth = 16.dp,
                    foregroundColor = Color(0xFF6200EE),
                    backgroundColor = Color(0xFFBB86FC)
                )
            }
        }

        // Metrics Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Today's Metrics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Steps",
                        progress = stepsProgress,
                        color = Color(0xFF03DAC5)
                    )
                    MetricItem(
                        title = "Water",
                        progress = waterProgress,
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(
                        title = "Sleep",
                        progress = sleepProgress,
                        color = Color(0xFF6200EE)
                    )
                    MetricItem(
                        title = "Activity",
                        progress = activityProgress,
                        color = Color(0xFFFF5722)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    title: String,
    progress: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgress(
            percentage = progress,
            size = 100.dp,
            strokeWidth = 10.dp,
            foregroundColor = color,
            backgroundColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
