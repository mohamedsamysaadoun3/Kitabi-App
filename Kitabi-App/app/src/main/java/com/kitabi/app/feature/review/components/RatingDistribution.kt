package com.kitabi.app.feature.review.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * رسم بياني لتوزيع التقييمات
 * يعرض نسبة كل تقييم (من 1 إلى 5 نجوم)
 */
@Composable
fun RatingDistribution(
    distribution: Map<Int, Int>,
    totalRatings: Int
) {
    Column(
        modifier = Modifier.width(180.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
    ) {
        // عرض من 5 إلى 1
        for (star in 5 downTo 1) {
            val count = distribution[star] ?: 0
            val percentage = if (totalRatings > 0) count.toFloat() / totalRatings else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$star",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))

                // شريط التقدم
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            KitabiTheme.colors.outlineVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .height(8.dp)
                            .background(
                                KitabiTheme.colors.ratingStar,
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.width(24.dp)
                )
            }
        }
    }
}
