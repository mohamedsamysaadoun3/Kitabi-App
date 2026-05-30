package com.kitabi.app.feature.reader

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.feature.reader.components.AiAssistantSheet
import com.kitabi.app.feature.reader.components.ReaderTheme
import com.kitabi.app.feature.reader.components.ReaderToolbar
import com.kitabi.app.feature.reader.components.TtsControls

/**
 * شاشة قراءة الكتاب
 * تجربة قراءة فاخرة مع أدوات متقدمة
 * تدعم التحكم بحجم الخط والسطوع والسمة
 * تتضمن مساعد الذكاء الاصطناعي والتحكم بالنطق
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // حالة شريط الأدوات
    var isToolbarVisible by remember { mutableStateOf(false) }

    // حالة التحكم بالنطق
    var showTtsControls by remember { mutableStateOf(false) }

    // حالة مساعد الذكاء الاصطناعي
    var showAiSheet by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }

    // إعدادات القارئ
    var fontSize by remember { mutableIntStateOf(uiState.fontSize) }
    var brightness by remember { mutableFloatStateOf(uiState.brightness) }
    var readerTheme by remember { mutableStateOf(uiState.readerTheme) }

    // التحكم بسطوع الشاشة
    val activity = LocalContext.current as? Activity
    DisposableEffect(brightness) {
        activity?.window?.attributes = activity?.window?.attributes?.apply {
            screenBrightness = brightness
        }
        onDispose { }
    }

    // ألوان السمة
    val themeColors = when (readerTheme) {
        ReaderTheme.SEPIA -> ReaderThemeColors(
            background = Color(0xFFF4ECD8),
            text = Color(0xFF5B4636),
            surface = Color(0xFFEDE3CC)
        )
        ReaderTheme.DARK -> ReaderThemeColors(
            background = Color(0xFF1A1A2E),
            text = Color(0xFFE0E0E0),
            surface = Color(0xFF252540)
        )
        ReaderTheme.GREEN -> ReaderThemeColors(
            background = Color(0xFFE8F5E9),
            text = Color(0xFF1B5E20),
            surface = Color(0xFFC8E6C9)
        )
        else -> ReaderThemeColors(
            background = Color(0xFFFFFBF0),
            text = Color(0xFF2D2D2D),
            surface = Color(0xFFF5F0E8)
        )
    }

    // تحديث الإعدادات
    DisposableEffect(fontSize, readerTheme) {
        viewModel.updateSettings(fontSize, readerTheme)
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // منطقة القراءة الرئيسية
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isToolbarVisible = !isToolbarVisible
                }
        ) {
            // محتوى الكتاب
            SelectionContainer {
                Text(
                    text = uiState.bookContent,
                    style = TextStyle(
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.7).sp,
                        color = themeColors.text,
                        textAlign = TextAlign.Justify,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 56.dp,
                            bottom = 80.dp
                        )
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        // شريط الأدوات العلوي
        AnimatedVisibility(
            visible = isToolbarVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(2f)
        ) {
            ReaderToolbar(
                bookTitle = uiState.bookTitle,
                onBack = onBack,
                onBookmark = { viewModel.toggleBookmark() },
                isBookmarked = uiState.isBookmarked,
                onFontSizeIncrease = { fontSize = (fontSize + 2).coerceAtMost(36) },
                onFontSizeDecrease = { fontSize = (fontSize - 2).coerceAtLeast(12) },
                onThemeChange = { readerTheme = it },
                currentTheme = readerTheme,
                onAiClick = { showAiSheet = true },
                onTtsClick = { showTtsControls = !showTtsControls },
                brightness = brightness,
                onBrightnessChange = { brightness = it }
            )
        }

        // شريط الصفحات السفلي
        AnimatedVisibility(
            visible = isToolbarVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f)
        ) {
            BottomPageBar(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                chapterName = uiState.currentChapter,
                onPageChange = { viewModel.goToPage(it) }
            )
        }

        // علامات النص المحدد للذكاء الاصطناعي
        AnimatedVisibility(
            visible = selectedText.isNotEmpty() && isToolbarVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .zIndex(3f)
        ) {
            AiQuickActionsBar(
                selectedText = selectedText,
                onSummarize = {
                    showAiSheet = true
                    viewModel.aiAction("تلخيص", selectedText)
                },
                onExplain = {
                    showAiSheet = true
                    viewModel.aiAction("شرح", selectedText)
                },
                onTranslate = {
                    showAiSheet = true
                    viewModel.aiAction("ترجمة", selectedText)
                },
                onQuestion = {
                    showAiSheet = true
                    viewModel.aiAction("سؤال", selectedText)
                }
            )
        }

        // التحكم بالنطق
        AnimatedVisibility(
            visible = showTtsControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .zIndex(4f)
        ) {
            TtsControls(
                isPlaying = uiState.isTtsPlaying,
                onPlay = { viewModel.startTts() },
                onPause = { viewModel.pauseTts() },
                onStop = { viewModel.stopTts() },
                onClose = { showTtsControls = false },
                speechRate = uiState.ttsSpeed,
                onSpeedChange = { viewModel.updateTtsSpeed(it) }
            )
        }
    }

    // شاشة مساعد الذكاء الاصطناعي
    if (showAiSheet) {
        AiAssistantSheet(
            bookId = bookId,
            bookTitle = uiState.bookTitle,
            selectedText = selectedText,
            onDismiss = {
                showAiSheet = false
                selectedText = ""
            },
            onSendMessage = { message ->
                viewModel.sendAiMessage(message)
            },
            aiMessages = uiState.aiMessages,
            isLoading = uiState.isAiLoading
        )
    }
}

/**
 * ألوان سمة القارئ
 */
data class ReaderThemeColors(
    val background: Color,
    val text: Color,
    val surface: Color
)

/**
 * شريط الصفحات السفلي
 */
@Composable
private fun BottomPageBar(
    currentPage: Int,
    totalPages: Int,
    chapterName: String,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                KitabiTheme.colors.surface,
                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // اسم الفصل
        Text(
            text = chapterName,
            style = MaterialTheme.typography.labelSmall,
            color = KitabiTheme.colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        // رقم الصفحة
        Text(
            text = "$currentPage / $totalPages",
            style = MaterialTheme.typography.labelMedium,
            color = KitabiTheme.colors.primary
        )

        // شريط التقدم البسيط
        Spacer(modifier = Modifier.width(12.dp))

        val progress = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .background(
                    KitabiTheme.colors.outlineVariant,
                    RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(
                        KitabiTheme.colors.primary,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * شريط الإجراءات السريعة للذكاء الاصطناعي
 */
@Composable
private fun AiQuickActionsBar(
    selectedText: String,
    onSummarize: () -> Unit,
    onExplain: () -> Unit,
    onTranslate: () -> Unit,
    onQuestion: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(
                KitabiTheme.colors.surfaceVariant,
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AiActionChip(text = "تلخيص", onClick = onSummarize)
        AiActionChip(text = "شرح", onClick = onExplain)
        AiActionChip(text = "ترجمة", onClick = onTranslate)
        AiActionChip(text = "سؤال", onClick = onQuestion)
    }
}

/**
 * شريحة إجراء ذكاء اصطناعي
 */
@Composable
private fun AiActionChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                KitabiTheme.colors.primaryContainer,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = KitabiTheme.colors.onPrimaryContainer
        )
    }
}
