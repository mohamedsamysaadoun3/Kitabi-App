package com.kitabi.app.feature.reader.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * إجراءات سريعة للذكاء الاصطناعي
 * تظهر عند تحديد نص في القارئ
 * تحتوي على أزرار: تلخيص، شرح، ترجمة، سؤال
 */
@Composable
fun AiQuickActions(
    selectedText: String,
    onSummarize: () -> Unit,
    onExplain: () -> Unit,
    onTranslate: () -> Unit,
    onQuestion: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.AutoStories,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = KitabiTheme.colors.primary
        )
        Spacer(modifier = Modifier.width(8.dp))

        AssistChip(
            onClick = onSummarize,
            label = { Text(text = "تلخيص", style = MaterialTheme.typography.labelMedium) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        AssistChip(
            onClick = onExplain,
            label = { Text(text = "شرح", style = MaterialTheme.typography.labelMedium) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        AssistChip(
            onClick = onTranslate,
            label = { Text(text = "ترجمة", style = MaterialTheme.typography.labelMedium) }
        )
        Spacer(modifier = Modifier.width(4.dp))
        AssistChip(
            onClick = onQuestion,
            label = { Text(text = "سؤال", style = MaterialTheme.typography.labelMedium) }
        )
    }
}
