package com.kitabi.app.feature.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.data.local.dao.BookmarkDao
import com.kitabi.app.data.local.entity.BookmarkEntity
import com.kitabi.app.domain.model.AiMessage
import com.kitabi.app.domain.model.AiFeature
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.domain.repository.UserPreferencesRepository
import com.kitabi.app.provider.ai.AiRouter
import com.kitabi.app.provider.book.ContentProviderFactory
import com.kitabi.app.provider.book.BookContentProvider
import android.net.Uri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * نموذج عرض شاشة القراءة
 * يدير حالة القراءة والتفاعل مع مساعد الذكاء الاصطناعي
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookmarkDao: BookmarkDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val aiRouter: AiRouter,
    private val contentProviderFactory: ContentProviderFactory
) : ViewModel() {

    /** حالة واجهة المستخدم */
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        // تحميل إعدادات القراءة
        viewModelScope.launch {
            val fontSize = userPreferencesRepository.readerFontSize.first()
            val theme = userPreferencesRepository.readerTheme.first()
            _uiState.update { it.copy(fontSize = fontSize, readerTheme = theme) }
        }
    }

    /**
     * تحميل بيانات الكتاب
     */
    fun loadBook(bookId: String) {
        viewModelScope.launch {
            try {
                val book = bookRepository.getBookById(bookId)
                book?.let {
                    _uiState.update { state ->
                        state.copy(
                            bookTitle = it.title,
                            currentPage = it.currentPage,
                            totalPages = it.pageCount,
                            isLoading = false
                        )
                    }
                    loadBookContent(it.filePath)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * تحميل محتوى الكتاب من الملف باستخدام مزود المحتوى
     */
    private fun loadBookContent(filePath: String) {
        viewModelScope.launch {
            try {
                if (filePath.isBlank()) {
                    // لا يوجد ملف - عرض رسالة تحميل الكتاب
                    _uiState.update { state ->
                        state.copy(
                            bookContent = "لم يتم تحميل هذا الكتاب بعد.\nقم بتحميل الكتاب من المتجر أو استورد ملفاً من جهازك.",
                            currentChapter = "",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                val uri = Uri.parse(filePath)
                val provider = contentProviderFactory.getProvider(uri)

                // فتح الكتاب
                val bookContent = provider.openBook(uri)

                // تحميل الصفحة الأولى
                val firstPage = provider.getPage(0)

                // بناء محتوى النص من الصفحات المتاحة
                val contentBuilder = StringBuilder()
                val totalPages = bookContent.pageCount
                val pagesToLoad = totalPages.coerceAtMost(50) // تحميل أول 50 صفحة

                for (i in 0 until pagesToLoad) {
                    try {
                        val page = provider.getPage(i)
                        contentBuilder.append(page.text)
                        if (i < pagesToLoad - 1) {
                            contentBuilder.append("\n\n")
                        }
                    } catch (_: Exception) {
                        // تجاهل صفحات الخطأ
                    }
                }

                // اسم الفصل الأول
                val firstChapter = bookContent.toc.firstOrNull()?.title ?: ""

                _uiState.update { state ->
                    state.copy(
                        bookContent = contentBuilder.toString(),
                        totalPages = totalPages,
                        currentPage = 1,
                        currentChapter = firstChapter,
                        isLoading = false
                    )
                }

                provider.closeBook()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "فشل تحميل الكتاب: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    /**
     * الانتقال لصفحة محددة
     */
    fun goToPage(page: Int) {
        _uiState.update { it.copy(currentPage = page.coerceIn(1, it.totalPages)) }
    }

    /**
     * تبديل حالة الإشارة المرجعية
     */
    fun toggleBookmark() {
        val state = _uiState.value
        _uiState.update { it.copy(isBookmarked = !it.isBookmarked) }

        viewModelScope.launch {
            try {
                if (!state.isBookmarked) {
                    bookmarkDao.insertBookmark(
                        BookmarkEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            bookId = state.bookTitle,
                            page = state.currentPage,
                            note = ""
                        )
                    )
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * تحديث إعدادات القراءة
     */
    fun updateSettings(fontSize: Int, theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.setReaderFontSize(fontSize)
            userPreferencesRepository.setReaderTheme(theme)
        }
        _uiState.update { it.copy(fontSize = fontSize, readerTheme = theme) }
    }

    /**
     * إرسال إجراء ذكاء اصطناعي
     */
    fun aiAction(action: String, text: String) {
        val prompt = when (action) {
            "تلخيص" -> "لخص النص التالي: $text"
            "شرح" -> "اشرح النص التالي: $text"
            "ترجمة" -> "ترجم النص التالي إلى الإنجليزية: $text"
            "سؤال" -> "أجب عن سؤال حول النص التالي: $text"
            else -> text
        }

        // إضافة رسالة المستخدم
        _uiState.update { state ->
            state.copy(
                aiMessages = state.aiMessages + AiMessage(
                    content = prompt,
                    isFromUser = true,
                    bookId = _uiState.value.bookTitle,
                    conversationId = "reader_session"
                ),
                isAiLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val response = when (action) {
                    "تلخيص" -> aiRouter.summarize(text).summary
                    "شرح" -> aiRouter.explain(text).explanation
                    "ترجمة" -> aiRouter.translate(text).translatedText
                    else -> {
                        val chatResponse = aiRouter.chat(
                            messages = listOf(
                                com.kitabi.app.provider.ai.AiChatMessage(content = prompt)
                            )
                        )
                        chatResponse.text
                    }
                }

                _uiState.update { state ->
                    state.copy(
                        aiMessages = state.aiMessages + AiMessage(
                            content = response.ifEmpty { "عذراً، لم أتمكن من معالجة طلبك." },
                            isFromUser = false,
                            feature = when (action) {
                                "تلخيص" -> AiFeature.SUMMARIZE
                                "شرح" -> AiFeature.EXPLAIN
                                "ترجمة" -> AiFeature.TRANSLATE
                                else -> AiFeature.Q_AND_A
                            },
                            bookId = state.bookTitle,
                            conversationId = "reader_session"
                        ),
                        isAiLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        aiMessages = state.aiMessages + AiMessage(
                            content = "عذراً، حدث خطأ: ${e.message}",
                            isFromUser = false,
                            bookId = state.bookTitle,
                            conversationId = "reader_session"
                        ),
                        isAiLoading = false
                    )
                }
            }
        }
    }

    /**
     * إرسال رسالة لمساعد الذكاء الاصطناعي
     */
    fun sendAiMessage(message: String) {
        _uiState.update { state ->
            state.copy(
                aiMessages = state.aiMessages + AiMessage(
                    content = message,
                    isFromUser = true,
                    bookId = state.bookTitle,
                    conversationId = "reader_session"
                ),
                isAiLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val response = aiRouter.chat(
                    messages = _uiState.value.aiMessages.map {
                        com.kitabi.app.provider.ai.AiChatMessage(
                            content = it.content,
                            role = if (it.isFromUser) com.kitabi.app.provider.ai.AiRole.USER else com.kitabi.app.provider.ai.AiRole.ASSISTANT
                        )
                    }
                )

                _uiState.update { state ->
                    state.copy(
                        aiMessages = state.aiMessages + AiMessage(
                            content = response.text.ifEmpty { "عذراً، لم أتمكن من الرد." },
                            isFromUser = false,
                            bookId = state.bookTitle,
                            conversationId = "reader_session"
                        ),
                        isAiLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        aiMessages = state.aiMessages + AiMessage(
                            content = "عذراً، حدث خطأ: ${e.message}",
                            isFromUser = false,
                            bookId = state.bookTitle,
                            conversationId = "reader_session"
                        ),
                        isAiLoading = false
                    )
                }
            }
        }
    }

    /**
     * بدء النطق
     */
    fun startTts() {
        _uiState.update { it.copy(isTtsPlaying = true) }
        aiRouter.speakText(_uiState.value.bookContent)
    }

    /**
     * إيقاف مؤقت للنطق
     */
    fun pauseTts() {
        _uiState.update { it.copy(isTtsPlaying = false) }
        aiRouter.stopSpeaking()
    }

    /**
     * إيقاف النطق
     */
    fun stopTts() {
        _uiState.update { it.copy(isTtsPlaying = false) }
        aiRouter.stopSpeaking()
    }

    /**
     * تحديث سرعة النطق
     */
    fun updateTtsSpeed(speed: Float) {
        _uiState.update { it.copy(ttsSpeed = speed) }
        aiRouter.setTtsSpeed(speed)
    }
}

/**
 * حالة واجهة مستخدم شاشة القراءة
 */
data class ReaderUiState(
    /** عنوان الكتاب */
    val bookTitle: String = "",
    /** محتوى الكتاب */
    val bookContent: String = "",
    /** الصفحة الحالية */
    val currentPage: Int = 1,
    /** إجمالي الصفحات */
    val totalPages: Int = 1,
    /** اسم الفصل الحالي */
    val currentChapter: String = "",
    /** هل الكتاب في الإشارات المرجعية */
    val isBookmarked: Boolean = false,
    /** حجم الخط */
    val fontSize: Int = 18,
    /** السطوع */
    val brightness: Float = 0.5f,
    /** سمة القارئ */
    val readerTheme: String = "light",
    /** هل يتم التحميل */
    val isLoading: Boolean = true,
    /** رسالة الخطأ */
    val error: String? = null,
    /** رسائل مساعد الذكاء الاصطناعي */
    val aiMessages: List<AiMessage> = emptyList(),
    /** هل مساعد الذكاء الاصطناعي يُحمّل */
    val isAiLoading: Boolean = false,
    /** هل النطق يعمل */
    val isTtsPlaying: Boolean = false,
    /** سرعة النطق */
    val ttsSpeed: Float = 1.0f
)
