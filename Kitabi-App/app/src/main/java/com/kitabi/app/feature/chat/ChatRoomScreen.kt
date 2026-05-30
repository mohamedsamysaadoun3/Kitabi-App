package com.kitabi.app.feature.chat

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.ChatMessage
import com.kitabi.app.domain.model.MessageType
import com.kitabi.app.feature.chat.components.ChatInputBar
import com.kitabi.app.feature.chat.components.ChatMessageBubble

/**
 * شاشة غرفة المحادثة
 * تصميم مشابه لواتساب لكن للكتب
 * تدعم الرسائل النصية واقتباسات الكتب والتفاعلات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    roomId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.getMessages(roomId).collectAsState(emptyList())
    val onlineCount by viewModel.onlineCount.collectAsState()
    val roomInfo by viewModel.getRoomInfo(roomId).collectAsState(null)

    val listState = rememberLazyListState()

    // التمرير التلقائي لآخر رسالة
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatRoomTopBar(
                roomName = roomInfo?.name ?: "غرفة محادثة",
                onlineCount = onlineCount,
                onBack = onBack
            )
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // منطقة الرسائل
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyMessagesState()
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(
                        message = message,
                        isFromCurrentUser = viewModel.isCurrentUser(message.senderId)
                    )
                }
            }

            // شريط الإدخال
            ChatInputBar(
                onSendMessage = { text ->
                    viewModel.sendMessage(roomId, text)
                },
                onQuoteClick = { /* فتح نافذة الاقتباس */ }
            )
        }
    }
}

/**
 * شريط العنوان لغرفة المحادثة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatRoomTopBar(
    roomName: String,
    onlineCount: Int,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // نقطة خضراء نابضة
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$onlineCount متصل",
                        style = MaterialTheme.typography.labelSmall,
                        color = KitabiTheme.colors.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = KitabiTheme.colors.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KitabiTheme.colors.surface
        )
    )
}

/**
 * حالة عدم وجود رسائل
 */
@Composable
private fun EmptyMessagesState() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 48.dp)
        ) {
            Text(
                text = "ابدأ المحادثة! 💬",
                style = MaterialTheme.typography.titleMedium,
                color = KitabiTheme.colors.onSurfaceVariant
            )
            Text(
                text = "شارك آراءك وناقش الكتب مع الآخرين",
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
