package com.kitabi.app.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * شريط إدخال المحادثة
 * يحتوي على حقل النص وزر الإرسال وزر الاقتباس
 */
@Composable
fun ChatInputBar(
    onSendMessage: (String) -> Unit,
    onQuoteClick: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KitabiTheme.colors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // زر الاقتباس
        IconButton(
            onClick = onQuoteClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FormatQuote,
                contentDescription = "اقتباس من كتاب",
                tint = KitabiTheme.colors.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // حقل النص
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = "اكتب رسالة...",
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KitabiTheme.colors.primary,
                unfocusedBorderColor = KitabiTheme.colors.outlineVariant,
                cursorColor = KitabiTheme.colors.primary
            ),
            maxLines = 3
        )

        Spacer(modifier = Modifier.width(4.dp))

        // زر الإرسال
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text.trim())
                    text = ""
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (text.isNotBlank()) KitabiTheme.colors.primary else KitabiTheme.colors.outlineVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "إرسال",
                tint = if (text.isNotBlank()) KitabiTheme.colors.onPrimary else KitabiTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
