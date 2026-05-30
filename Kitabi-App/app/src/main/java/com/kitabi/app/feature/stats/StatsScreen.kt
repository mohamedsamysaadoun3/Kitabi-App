package com.kitabi.app.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * شاشة إحصائيات القراءة
 * تعرض ملخص شامل لنشاط القراءة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val totalPages by viewModel.totalPagesRead.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val totalReadingTime by viewModel.totalReadingTime.collectAsStateWithLifecycle()
    val booksCompleted by viewModel.booksCompleted.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إحصائيات القراءة",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface
                )
            )
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // بطاقة التابع
            item {
                StreakCard(streak = currentStreak)
            }

            // بطاقات الإحصائيات الرئيسية
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.MenuBook,
                        value = "$totalPages",
                        label = "صفحة مقروءة",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Filled.AutoStories,
                        value = "$booksCompleted",
                        label = "كتاب مكتمل",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Filled.Timer,
                        value = formatReadingTime(totalReadingTime),
                        label = "وقت القراءة",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Filled.Speed,
                        value = calculateReadingSpeed(totalPages, totalReadingTime),
                        label = "صفحة/ساعة",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // النشاط الأسبوعي
            item {
                WeeklyActivitySection(weeklyStats = weeklyStats)
            }
        }
    }
}

/**
 * بطاقة التابع
 */
@Composable
private fun StreakCard(streak: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        KitabiTheme.colors.primary,
                        KitabiTheme.colors.tertiary
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                Text(
                    text = "يوم متتالي",
                    style = MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * بطاقة إحصائية
 */
@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KitabiTheme.colors.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}

/**
 * قسم النشاط الأسبوعي
 */
@Composable
private fun WeeklyActivitySection(weeklyStats: List<com.kitabi.app.data.local.entity.ReadingStatsEntity>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = KitabiTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "النشاط الأسبوعي",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // رسم بياني بسيط
            if (weeklyStats.isNotEmpty()) {
                val maxPages = weeklyStats.maxOf { it.pagesRead }.coerceAtLeast(1)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyStats.take(7).forEach { stat ->
                        val height = (stat.pagesRead.toFloat() / maxPages * 120).dp
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${stat.pagesRead}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(height)
                                    .background(
                                        KitabiTheme.colors.primary,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stat.date.takeLast(2),
                                style = MaterialTheme.typography.labelSmall,
                                color = KitabiTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "لا توجد بيانات بعد. ابدأ القراءة!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

/**
 * تنسيق وقت القراءة
 */
private fun formatReadingTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}س ${minutes}د"
        minutes > 0 -> "${minutes}د"
        else -> "${seconds}ث"
    }
}

/**
 * حساب سرعة القراءة
 */
private fun calculateReadingSpeed(totalPages: Int, totalSeconds: Long): String {
    if (totalSeconds == 0L) return "0"
    val hours = totalSeconds / 3600f
    val speed = if (hours > 0) totalPages / hours else 0f
    return String.format("%.0f", speed)
}
