package com.kitabi.app.feature.chat

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.domain.model.ChatRoom

/**
 * شاشة قائمة غرف المحادثة
 * تعرض غرف المحادثة المتاحة مع آخر رسالة وعدد الأعضاء
 * تشبه تطبيق واتساب بتصميم عربي أنيق
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(
    onRoomClick: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "غرف المحادثة",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KitabiTheme.colors.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* إنشاء غرفة جديدة */ },
                containerColor = KitabiTheme.colors.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "غرفة جديدة",
                    tint = KitabiTheme.colors.onPrimary
                )
            }
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        if (chatRooms.isEmpty()) {
            // حالة القائمة الفارغة
            EmptyChatRoomsState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(chatRooms, key = { it.id }) { room ->
                    ChatRoomItem(
                        room = room,
                        onClick = { onRoomClick(room.id) }
                    )
                }
            }
        }
    }
}

/**
 * عنصر غرفة محادثة
 */
@Composable
private fun ChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(KitabiTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // صورة الغرفة
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    KitabiTheme.colors.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                tint = KitabiTheme.colors.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // معلومات الغرفة
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = room.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = KitabiTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = room.lastMessage.ifEmpty { "لا توجد رسائل بعد" },
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // عدد الأعضاء
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = KitabiTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${room.memberCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }

            if (room.lastMessageAt > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTime(room.lastMessageAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * حالة القائمة الفارغة
 */
@Composable
private fun EmptyChatRoomsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "لا توجد غرف محادثة",
                style = MaterialTheme.typography.titleMedium,
                color = KitabiTheme.colors.onSurfaceVariant
            )
            Text(
                text = "انضم لغرفة أو أنشئ واحدة جديدة",
                style = MaterialTheme.typography.bodyMedium,
                color = KitabiTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * تنسيق الوقت
 */
private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "الآن"
        diff < 3_600_000 -> "${diff / 60_000} د"
        diff < 86_400_000 -> "${diff / 3_600_000} س"
        else -> "${diff / 86_400_000} ي"
    }
}
