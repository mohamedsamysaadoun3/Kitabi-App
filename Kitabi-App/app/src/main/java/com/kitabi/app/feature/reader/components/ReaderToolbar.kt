package com.kitabi.app.feature.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kitabi.app.core.designsystem.KitabiTheme

/**
 * كائن سمات القارئ
 */
object ReaderTheme {
    const val LIGHT = "light"
    const val SEPIA = "sepia"
    const val DARK = "dark"
    const val GREEN = "green"
}

/**
 * شريط أدوات القارئ
 * يظهر عند النقر على منطقة القراءة
 * يحتوي على أزرار التنقل والتحكم والإعدادات
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderToolbar(
    bookTitle: String,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
    isBookmarked: Boolean,
    onFontSizeIncrease: () -> Unit,
    onFontSizeDecrease: () -> Unit,
    onThemeChange: (String) -> Unit,
    currentTheme: String,
    onAiClick: () -> Unit,
    onTtsClick: () -> Unit,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit
) {
    var showThemeMenu by remember { mutableStateOf(false) }
    var showFontControls by remember { mutableStateOf(false) }
    var showBrightnessControl by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .statusBarsPadding()
    ) {
        // الصف الأول: العنوان وأزرار التنقل
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر الرجوع
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = KitabiTheme.colors.onSurface
                )
            }

            // عنوان الكتاب
            Text(
                text = bookTitle,
                style = MaterialTheme.typography.titleMedium,
                color = KitabiTheme.colors.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // زر الإشارة المرجعية
            IconButton(onClick = onBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "إشارة مرجعية",
                    tint = if (isBookmarked) KitabiTheme.colors.bookmark else KitabiTheme.colors.onSurfaceVariant
                )
            }
        }

        // الصف الثاني: أزرار التحكم
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolbarActionItem(
                icon = Icons.Filled.TextDecrease,
                label = "تصغير",
                onClick = onFontSizeDecrease
            )
            ToolbarActionItem(
                icon = Icons.Filled.TextIncrease,
                label = "تكبير",
                onClick = onFontSizeIncrease
            )

            // زر السمة
            Box {
                ToolbarActionItem(
                    icon = Icons.Filled.Palette,
                    label = "السمة",
                    onClick = { showThemeMenu = true }
                )
                DropdownMenu(
                    expanded = showThemeMenu,
                    onDismissRequest = { showThemeMenu = false }
                ) {
                    ThemeMenuItem("أبيض", ReaderTheme.LIGHT, currentTheme) { onThemeChange(it); showThemeMenu = false }
                    ThemeMenuItem("سيبيا", ReaderTheme.SEPIA, currentTheme) { onThemeChange(it); showThemeMenu = false }
                    ThemeMenuItem("داكن", ReaderTheme.DARK, currentTheme) { onThemeChange(it); showThemeMenu = false }
                    ThemeMenuItem("أخضر", ReaderTheme.GREEN, currentTheme) { onThemeChange(it); showThemeMenu = false }
                }
            }

            ToolbarActionItem(
                icon = Icons.Filled.Brightness6,
                label = "سطوع",
                onClick = { showBrightnessControl = !showBrightnessControl }
            )
            ToolbarActionItem(
                icon = Icons.Filled.Psychology,
                label = "ذكاء",
                onClick = onAiClick
            )
            ToolbarActionItem(
                icon = Icons.Filled.PlayArrow,
                label = "نطق",
                onClick = onTtsClick
            )
        }

        // شريط السطوع
        if (showBrightnessControl) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Brightness6,
                    contentDescription = null,
                    tint = KitabiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = brightness,
                    onValueChange = onBrightnessChange,
                    colors = SliderDefaults.colors(
                        thumbColor = KitabiTheme.colors.primary,
                        activeTrackColor = KitabiTheme.colors.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * عنصر إجراء في شريط الأدوات
 */
@Composable
private fun ToolbarActionItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = KitabiTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = KitabiTheme.colors.onSurfaceVariant
        )
    }
}

/**
 * عنصر قائمة السمة
 */
@Composable
private fun ThemeMenuItem(
    label: String,
    themeValue: String,
    currentTheme: String,
    onSelect: (String) -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentTheme == themeValue) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(KitabiTheme.colors.primary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = label)
            }
        },
        onClick = { onSelect(themeValue) }
    )
}
