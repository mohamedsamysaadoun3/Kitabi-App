package com.kitabi.app.feature.reader.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitabi.app.domain.model.BookFormat
import com.kitabi.app.provider.book.BookContent
import com.kitabi.app.provider.book.BookProviderException
import com.kitabi.app.provider.book.ContentProviderFactory
import com.kitabi.app.provider.book.PageContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ألوان سمة عرض القارئ
 * تُستخدم داخل مكون ReaderView فقط
 */
data class ReaderViewThemeColors(
    /** لون الخلفية */
    val background: Color,
    /** لون النص */
    val text: Color
)

/**
 * عرض القارئ - المكون الرئيسي لعرض محتوى الكتاب
 * يدعم PDF و EPUB و TXT
 * يوفر التنقل بين الصفحات والفصول
 * يدعم اختيار النص والتحكم بحجم الخط والسمة
 * @param bookUri مسار ملف الكتاب
 * @param format صيغة الكتاب
 * @param contentProviderFactory مصنع مزودي المحتوى
 * @param fontSize حجم الخط
 * @param readerTheme سمة القارئ
 * @param onPageChange دالة استدعاء عند تغيير الصفحة
 * @param onTextSelected دالة استدعاء عند اختيار نص
 * @param modifier المعدل
 */
@Composable
fun ReaderView(
    bookUri: Uri,
    format: BookFormat,
    contentProviderFactory: ContentProviderFactory,
    fontSize: Int = 18,
    readerTheme: String = ReaderTheme.LIGHT,
    onPageChange: (currentPage: Int, totalPages: Int) -> Unit = { _, _ -> },
    onTextSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    // حالة الكتاب
    var bookContent by remember { mutableStateOf<BookContent?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var pageContent by remember { mutableStateOf<PageContent?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // ألوان السمة
    val themeColors = when (readerTheme) {
        ReaderTheme.SEPIA -> ReaderViewThemeColors(
            background = Color(0xFFF4ECD8),
            text = Color(0xFF5B4636)
        )
        ReaderTheme.DARK -> ReaderViewThemeColors(
            background = Color(0xFF1A1A2E),
            text = Color(0xFFE0E0E0)
        )
        ReaderTheme.GREEN -> ReaderViewThemeColors(
            background = Color(0xFFE8F5E9),
            text = Color(0xFF1B5E20)
        )
        else -> ReaderViewThemeColors(
            background = Color(0xFFFFFBF0),
            text = Color(0xFF2D2D2D)
        )
    }

    // فتح الكتاب عند التحميل
    LaunchedEffect(bookUri) {
        isLoading = true
        error = null

        try {
            val provider = contentProviderFactory.getProvider(format)
            val content = withContext(Dispatchers.IO) {
                provider.openBook(bookUri)
            }
            bookContent = content

            if (content.pageCount > 0) {
                currentPage = 0
                pageContent = withContext(Dispatchers.IO) {
                    provider.getPage(0)
                }
                onPageChange(1, content.pageCount)
            }

            isLoading = false
        } catch (e: BookProviderException) {
            error = "فشل فتح الكتاب: ${e.message}"
            isLoading = false
        } catch (e: Exception) {
            error = "حدث خطأ غير متوقع: ${e.message}"
            isLoading = false
        }
    }

    // تنظيف الموارد عند الإزالة
    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val provider = contentProviderFactory.getProvider(format)
                    provider.closeBook()
                } catch (_: Exception) {
                    // تجاهل أخطاء الإغلاق
                }
            }
        }
    }

    // تحديث محتوى الصفحة عند تغيير الصفحة
    LaunchedEffect(currentPage) {
        bookContent?.let { content ->
            try {
                val provider = contentProviderFactory.getProvider(format)
                pageContent = withContext(Dispatchers.IO) {
                    provider.getPage(currentPage)
                }
                onPageChange(currentPage + 1, content.pageCount)
            } catch (e: Exception) {
                error = "فشل تحميل الصفحة: ${e.message}"
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        when {
            isLoading -> {
                // حالة التحميل
                ReaderLoadingView(themeColors = themeColors)
            }
            error != null -> {
                // حالة الخطأ
                ReaderErrorView(
                    error = error!!,
                    themeColors = themeColors
                )
            }
            pageContent != null -> {
                // عرض المحتوى حسب نوع الصفحة
                when (format) {
                    BookFormat.PDF -> {
                        PdfPageView(
                            pageContent = pageContent!!,
                            fontSize = fontSize,
                            themeColors = themeColors,
                            onTextSelected = onTextSelected
                        )
                    }
                    else -> {
                        TextPageView(
                            pageContent = pageContent!!,
                            fontSize = fontSize,
                            themeColors = themeColors,
                            onTextSelected = onTextSelected,
                            currentPage = currentPage,
                            totalPages = bookContent?.pageCount ?: 1,
                            onPreviousPage = {
                                if (currentPage > 0) currentPage--
                            },
                            onNextPage = {
                                if (bookContent != null && currentPage < bookContent!!.pageCount - 1) currentPage++
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * عرض صفحة PDF
 * يعرض نص صفحة PDF مع إمكانية التحديد
 */
@Composable
private fun PdfPageView(
    pageContent: PageContent,
    fontSize: Int,
    themeColors: ReaderViewThemeColors,
    onTextSelected: (String) -> Unit
) {
    // عرض نص صفحة PDF
    SelectionContainer {
        Text(
            text = pageContent.text,
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
                    top = 16.dp,
                    bottom = 16.dp
                )
                .verticalScroll(rememberScrollState())
        )
    }
}

/**
 * عرض صفحة نصية (EPUB أو TXT)
 * يدعم التمرير والتنقل بين الصفحات بالإيماءات
 */
@Composable
private fun TextPageView(
    pageContent: PageContent,
    fontSize: Int,
    themeColors: ReaderViewThemeColors,
    onTextSelected: (String) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    // كشف إيماءات السحب للتنقل بين الصفحات
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    // السحب لليسار = الصفحة التالية (للنص العربي RTL)
                    // السحب لليمين = الصفحة السابقة
                    if (dragAmount < -50) {
                        onNextPage()
                    } else if (dragAmount > 50) {
                        onPreviousPage()
                    }
                }
            }
    ) {
        // محتوى الصفحة
        SelectionContainer {
            Text(
                text = pageContent.text,
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
                        top = 16.dp,
                        bottom = 60.dp
                    )
                    .verticalScroll(rememberScrollState())
            )
        }

        // مؤشر الصفحة
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(
                    themeColors.text.copy(alpha = 0.1f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentPage + 1} / $totalPages",
                style = MaterialTheme.typography.labelMedium,
                color = themeColors.text
            )
        }
    }
}

/**
 * عرض التحميل
 */
@Composable
private fun ReaderLoadingView(themeColors: ReaderViewThemeColors) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = themeColors.text
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "جارٍ تحميل الكتاب...",
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.text
            )
        }
    }
}

/**
 * عرض الخطأ
 */
@Composable
private fun ReaderErrorView(
    error: String,
    themeColors: ReaderViewThemeColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️",
                style = TextStyle(fontSize = 48.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "حدث خطأ",
                style = MaterialTheme.typography.titleMedium,
                color = themeColors.text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = themeColors.text.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
