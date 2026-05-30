package com.kitabi.app.feature.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * عناصر التحكم بالنطق
 * تظهر عند تفعيل ميزة تحويل النص إلى كلام
 * تحتوي على أزرار التشغيل والإيقاف والسرعة
 */
@Composable
fun TtsControls(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit,
    speechRate: Float,
    onSpeedChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // عنوان وعنصر إغلاق
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔊 النطق",
                    style = MaterialTheme.typography.titleSmall,
                    color = KitabiTheme.colors.onSurface
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = KitabiTheme.colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // أزرار التحكم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر الإيقاف
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            KitabiTheme.colors.errorContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "إيقاف",
                        tint = KitabiTheme.colors.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // زر التشغيل/الإيقاف المؤقت
                IconButton(
                    onClick = if (isPlaying) onPause else onPlay,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            KitabiTheme.colors.primary,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = KitabiTheme.colors.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // زر الترجيع السريع (إبطاء)
                IconButton(
                    onClick = { onSpeedChange((speechRate - 0.25f).coerceIn(0.5f, 2f)) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            KitabiTheme.colors.secondaryContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "إبطاء",
                        tint = KitabiTheme.colors.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // شريط السرعة
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "السرعة",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = speechRate,
                    onValueChange = onSpeedChange,
                    valueRange = 0.5f..2f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = KitabiTheme.colors.primary,
                        activeTrackColor = KitabiTheme.colors.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("%.1fx", speechRate),
                    style = MaterialTheme.typography.labelMedium,
                    color = KitabiTheme.colors.primary
                )
            }
        }
    }
}
