package com.kitabi.app.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * منتقي التفاعلات بالرموز التعبيرية
 * يظهر فوق الرسالة عند الضغط المطول
 */
@Composable
fun ReactionPicker(
    onReactionSelected: (String) -> Unit
) {
    val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "👏")

    Row(
        modifier = Modifier
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { onReactionSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
