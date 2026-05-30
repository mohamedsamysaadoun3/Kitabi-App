package com.kitabi.app.feature.review.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * شريط التقييم التفاعلي
 * يتيح للمستخدم اختيار تقييم من 1 إلى 5 نجوم
 */
@Composable
fun RatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    starSize: Dp = 32.dp,
    enabled: Boolean = true
) {
    Row {
        repeat(5) { index ->
            val starRating = index + 1
            Icon(
                imageVector = if (starRating <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "$starRating نجوم",
                tint = if (starRating <= rating) KitabiTheme.colors.ratingStar else KitabiTheme.colors.outlineVariant,
                modifier = Modifier
                    .size(starSize)
                    .clickable(enabled = enabled) { onRatingChange(starRating) }
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

/**
 * شريط التقييم للعرض فقط
 */
@Composable
fun ReadOnlyRatingBar(
    rating: Float,
    starSize: Dp = 16.dp
) {
    Row {
        repeat(5) { index ->
            val starRating = index + 1
            Icon(
                imageVector = if (starRating <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (starRating <= rating) KitabiTheme.colors.ratingStar else KitabiTheme.colors.outlineVariant,
                modifier = Modifier.size(starSize)
            )
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}
