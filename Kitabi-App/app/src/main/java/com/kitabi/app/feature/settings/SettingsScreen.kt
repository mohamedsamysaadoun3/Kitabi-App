package com.kitabi.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.feature.settings.components.AiProviderSettings
import com.kitabi.app.feature.settings.components.UserCounterCard

/**
 * شاشة الإعدادات
 * منظمة ونظيفة مع قسم الملف الشخصي وإعدادات الذكاء الاصطناعي والقارئ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val readerTheme by viewModel.readerTheme.collectAsState()
    val activeUserCount by viewModel.activeUserCount.collectAsState()
    val aiProvider by viewModel.aiProvider.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الإعدادات",
                        fontWeight = FontWeight.Bold,
                        color = KitabiTheme.colors.onSurface
                    )
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
        },
        containerColor = KitabiTheme.colors.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // قسم الملف الشخصي
            item {
                ProfileSection(
                    userName = currentUser?.displayName ?: "مستخدم",
                    email = currentUser?.email ?: "ضيف",
                    photoUrl = currentUser?.photoUrl ?: ""
                )
            }

            // عداد المستخدمين
            item {
                UserCounterCard(activeUserCount = activeUserCount)
            }

            // إعدادات مزود الذكاء الاصطناعي
            item {
                SettingsSectionHeader(title = "الذكاء الاصطناعي", icon = Icons.Filled.Psychology)
                AiProviderSettings(
                    currentProvider = aiProvider,
                    onProviderChange = { viewModel.setAiProvider(it) }
                )
            }

            // إعدادات القارئ
            item {
                SettingsSectionHeader(title = "القارئ", icon = Icons.Filled.FormatSize)
                ReaderSettingsSection(
                    fontSize = fontSize,
                    onFontSizeChange = { viewModel.setFontSize(it) },
                    readerTheme = readerTheme,
                    onThemeChange = { viewModel.setReaderTheme(it) }
                )
            }

            // إعدادات العرض
            item {
                SettingsSectionHeader(title = "العرض", icon = Icons.Filled.DarkMode)
                DarkModeSetting(
                    isDarkMode = isDarkMode,
                    onToggle = { viewModel.setDarkMode(it) }
                )
            }

            // معلومات التطبيق
            item {
                SettingsSectionHeader(title = "عن التطبيق", icon = Icons.Filled.Info)
                AppInfoSection()
            }

            // زر تسجيل الخروج
            item {
                SignOutButton(onClick = onSignOut)
            }
        }
    }
}

/**
 * قسم الملف الشخصي
 */
@Composable
private fun ProfileSection(
    userName: String,
    email: String,
    photoUrl: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // صورة المستخدم
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    KitabiTheme.colors.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = KitabiTheme.colors.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(
                    text = userName.take(1),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = KitabiTheme.colors.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = KitabiTheme.colors.onSurface
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}

/**
 * رأس قسم الإعدادات
 */
@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KitabiTheme.colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = KitabiTheme.colors.primary
        )
    }
}

/**
 * إعدادات القارئ
 */
@Composable
private fun ReaderSettingsSection(
    fontSize: Int,
    onFontSizeChange: (Int) -> Unit,
    readerTheme: String,
    onThemeChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            // حجم الخط
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.FormatSize,
                    contentDescription = null,
                    tint = KitabiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حجم الخط",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$fontSize",
                    style = MaterialTheme.typography.labelLarge,
                    color = KitabiTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = fontSize.toFloat(),
                onValueChange = { onFontSizeChange(it.toInt()) },
                valueRange = 12f..36f,
                colors = SliderDefaults.colors(
                    thumbColor = KitabiTheme.colors.primary,
                    activeTrackColor = KitabiTheme.colors.primary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // سمة القارئ
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = KitabiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سمة القارئ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KitabiTheme.colors.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val themes = listOf("أبيض" to "light", "سيبيا" to "sepia", "داكن" to "dark", "أخضر" to "green")
                themes.forEach { (label, value) ->
                    ThemeOption(
                        label = label,
                        isSelected = readerTheme == value,
                        onClick = { onThemeChange(value) },
                        themeValue = value
                    )
                }
            }
        }
    }
}

/**
 * خيار السمة
 */
@Composable
private fun RowScope.ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    themeValue: String
) {
    val backgroundColor = when (themeValue) {
        "sepia" -> androidx.compose.ui.graphics.Color(0xFFF4ECD8)
        "dark" -> androidx.compose.ui.graphics.Color(0xFF1A1A2E)
        "green" -> androidx.compose.ui.graphics.Color(0xFFE8F5E9)
        else -> androidx.compose.ui.graphics.Color(0xFFFFFBF0)
    }
    val borderColor = if (isSelected) KitabiTheme.colors.primary else KitabiTheme.colors.outlineVariant

    Box(
        modifier = Modifier
            .weight(1f)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(backgroundColor, RoundedCornerShape(4.dp))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) KitabiTheme.colors.primary else KitabiTheme.colors.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * إعداد الوضع الداكن - تصميم حديث مع أيقونات شمس/قمر
 */
@Composable
private fun DarkModeSetting(
    isDarkMode: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KitabiTheme.colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isDarkMode) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // أيقونة الشمس أو القمر
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isDarkMode) KitabiTheme.colors.primaryContainer
                        else KitabiTheme.colors.secondaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDarkMode) "\uD83C\uDF19" else "\u2600\uFE0F",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isDarkMode) "\u0627\u0644\u0648\u0636\u0639 \u0627\u0644\u062F\u0627\u0643\u0646" else "\u0627\u0644\u0648\u0636\u0639 \u0627\u0644\u0641\u0627\u062A\u062D",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = KitabiTheme.colors.onSurface
                )
                Text(
                    text = if (isDarkMode) "\u0646\u0638\u0627\u0645 \u0627\u0644\u0623\u0644\u0648\u0627\u0646 \u0627\u0644\u062F\u0627\u0643\u0646 \u0646\u0634\u0637" else "\u0627\u0636\u063A\u0637 \u0644\u0644\u062A\u0628\u062F\u064A\u0644 \u0644\u0644\u0648\u0636\u0639 \u0627\u0644\u062F\u0627\u0643\u0646",
                    style = MaterialTheme.typography.labelSmall,
                    color = KitabiTheme.colors.onSurfaceVariant
                )
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = KitabiTheme.colors.primary,
                    checkedThumbColor = KitabiTheme.colors.onPrimary,
                    uncheckedTrackColor = KitabiTheme.colors.surfaceVariant,
                    uncheckedThumbColor = KitabiTheme.colors.outline
                )
            )
        }
    }
}

/**
 * معلومات التطبيق
 */
@Composable
private fun AppInfoSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "كتبي - قارئ الكتب الذكي",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KitabiTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الإصدار 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = KitabiTheme.colors.onSurfaceVariant
            )
        }
    }
}

/**
 * زر تسجيل الخروج
 */
@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.errorContainer,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Logout,
            contentDescription = null,
            tint = KitabiTheme.colors.onErrorContainer,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "تسجيل الخروج",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = KitabiTheme.colors.onErrorContainer
        )
    }
}
