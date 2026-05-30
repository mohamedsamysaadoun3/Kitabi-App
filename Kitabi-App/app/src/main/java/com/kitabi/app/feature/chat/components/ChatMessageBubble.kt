package com.kitabi.app.feature.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.ChatMessage
import com.kitabi.app.domain.model.MessageType

/**
 * فقاعة رسالة المحادثة
 * تصميم مشابه لواتساب مع دعم RTL
 * تدعم أنواع رسائل مختلفة (نص، اقتباس، اقتراح كتاب)
 */
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean
) {
    val alignment = if (isFromCurrentUser) Alignment.Start else Alignment.End
    val bubbleColor = if (isFromCurrentUser) KitabiTheme.colors.primary else KitabiTheme.colors.surfaceVariant
    val textColor = if (isFromCurrentUser) KitabiTheme.colors.onPrimary else KitabiTheme.colors.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .background(
                    bubbleColor,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 4.dp else 16.dp,
                        bottomEnd = if (isFromCurrentUser) 16.dp else 4.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                // اسم المرسل (للرسائل غير المستخدم)
                if (!isFromCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                }

                // اقتباس من كتاب (نوع خاص)
                if (message.messageType == MessageType.BOOK_QUOTE && message.bookQuote.isNotEmpty()) {
                    BookQuoteCard(
                        quote = message.bookQuote,
                        isFromCurrentUser = isFromCurrentUser
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }

                // نص الرسالة
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                // وقت الإرسال
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * بطاقة اقتباس من كتاب
 */
@Composable
private fun BookQuoteCard(
    quote: String,
    isFromCurrentUser: Boolean
) {
    val cardColor = if (isFromCurrentUser) {
        KitabiTheme.colors.onPrimary.copy(alpha = 0.15f)
    } else {
        KitabiTheme.colors.primary.copy(alpha = 0.1f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Filled.FormatQuote,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isFromCurrentUser) KitabiTheme.colors.onPrimary else KitabiTheme.colors.primary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = quote,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = if (isFromCurrentUser) KitabiTheme.colors.onPrimary else KitabiTheme.colors.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * تنسيق وقت الرسالة
 */
private fun formatMessageTime(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
