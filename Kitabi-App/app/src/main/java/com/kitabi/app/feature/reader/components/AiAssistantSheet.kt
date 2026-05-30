package com.kitabi.app.feature.reader.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.AiMessage

/**
 * شاشة مساعد الذكاء الاصطناعي المنبثقة
 * تظهر كشاشة سفلية أنيقة مع مؤشر متوهج
 * تدعم المحادثة والإجراءات السريعة على النص المحدد
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantSheet(
    bookId: String,
    bookTitle: String,
    selectedText: String,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    aiMessages: List<AiMessage>,
    isLoading: Boolean
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = KitabiTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // الرأس
            AiSheetHeader(
                bookTitle = bookTitle,
                onClose = onDismiss
            )

            // النص المحدد
            if (selectedText.isNotEmpty()) {
                SelectedTextCard(selectedText = selectedText)
            }

            // الإجراءات السريعة
            if (selectedText.isNotEmpty()) {
                AiQuickActionChips(
                    onActionClick = { action ->
                        onSendMessage("$action: $selectedText")
                    }
                )
            }

            // منطقة الرسائل
            AiMessagesList(
                messages = aiMessages,
                isLoading = isLoading,
                modifier = Modifier.weight(1f)
            )

            // حقل الإدخال
            AiInputField(
                onSend = onSendMessage,
                isEnabled = !isLoading
            )
        }
    }
}

/**
 * رأس الشاشة المنبثقة
 */
@Composable
private fun AiSheetHeader(
    bookTitle: String,
    onClose: () -> Unit
) {
    // تأثير التوهج
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // أيقونة الدماغ المتوهجة
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            KitabiTheme.colors.primaryContainer,
                            KitabiTheme.colors.primary.copy(alpha = 0.3f)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = KitabiTheme.colors.primary,
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "كتبي AI",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.primary
            )
            Text(
                text = bookTitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant
            )
        }

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "إغلاق",
                tint = KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}

/**
 * بطاقة النص المحدد
 */
@Composable
private fun SelectedTextCard(selectedText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(
                KitabiTheme.colors.primaryContainer.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "\"$selectedText\"",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = KitabiTheme.colors.onPrimaryContainer,
            maxLines = 4
        )
    }
}

/**
 * شرائح الإجراءات السريعة
 */
@Composable
private fun AiQuickActionChips(
    onActionClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val actions = listOf("تلخيص" to "📝", "شرح" to "💡", "ترجمة" to "🌐", "أسئلة" to "❓")
        actions.forEach { (action, emoji) ->
            Box(
                modifier = Modifier
                    .background(
                        KitabiTheme.colors.secondaryContainer,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onActionClick(action) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$emoji $action",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = KitabiTheme.colors.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * قائمة رسائل الذكاء الاصطناعي
 */
@Composable
private fun AiMessagesList(
    messages: List<AiMessage>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // التمرير التلقائي لآخر رسالة
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // رسالة الترحيب
        if (messages.isEmpty()) {
            item {
                WelcomeMessage()
            }
        }

        // الرسائل
        items(messages, key = { it.id }) { message ->
            AiMessageBubble(message = message)
        }

        // مؤشر الكتابة
        if (isLoading) {
            item {
                TypingIndicator()
            }
        }
    }
}

/**
 * رسالة الترحيب
 */
@Composable
private fun WelcomeMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Psychology,
            contentDescription = null,
            tint = KitabiTheme.colors.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "مرحباً! أنا مساعدك الذكي 🤖",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = KitabiTheme.colors.onBackground
        )
        Text(
            text = "يمكنني مساعدتك في تلخيص النصوص، شرحها، ترجمتها، والإجابة على أسئلتك",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = KitabiTheme.colors.onSurfaceVariant
        )
    }
}

/**
 * فقاعة رسالة الذكاء الاصطناعي
 */
@Composable
private fun AiMessageBubble(message: AiMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.Start else Arrangement.End
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        KitabiTheme.colors.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = null,
                    tint = KitabiTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .width(280.dp)
                .background(
                    if (isUser) KitabiTheme.colors.primary else KitabiTheme.colors.surfaceVariant,
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 4.dp else 16.dp,
                        bottomEnd = if (isUser) 16.dp else 4.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = if (isUser) KitabiTheme.colors.onPrimary else KitabiTheme.colors.onSurfaceVariant
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

/**
 * مؤشر الكتابة مع نقاط متحركة
 */
@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot1Scale)
                .background(KitabiTheme.colors.primary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot2Scale)
                .background(KitabiTheme.colors.primary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot3Scale)
                .background(KitabiTheme.colors.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "يفكر...",
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = KitabiTheme.colors.onSurfaceVariant
        )
    }
}

/**
 * حقل إدخال الرسائل
 */
@Composable
private fun AiInputField(
    onSend: (String) -> Unit,
    isEnabled: Boolean
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    text = "اسأل مساعدك الذكي...",
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
            maxLines = 3,
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text.trim())
                    text = ""
                }
            },
            enabled = isEnabled && text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (text.isNotBlank()) KitabiTheme.colors.primary else KitabiTheme.colors.outlineVariant,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "إرسال",
                tint = if (text.isNotBlank()) KitabiTheme.colors.onPrimary else KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}
