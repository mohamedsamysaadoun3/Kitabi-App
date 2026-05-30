package com.kitabi.app.provider.ai

import com.kitabi.app.domain.model.AiFeature

/**
 * واجهة مزود الذكاء الاصطناعي
 * تعرف العمليات التي يجب أن ينفذها أي مزود ذكاء اصطناعي
 * يدعم المحادثة، التلخيص، الشرح، الترجمة، وأكثر
 */
interface AiProvider {

    /** اسم المزود */
    val name: String

    /** الميزات المدعومة */
    val supportedFeatures: Set<AiFeature>

    /** هل يدعم العربية */
    val supportsArabic: Boolean

    /** هل المزود مجاني */
    val isFree: Boolean

    /**
     * محادثة مع الذكاء الاصطناعي
     * @param messages قائمة الرسائل السابقة
     * @param systemPrompt تعليمات النظام (اختياري)
     * @param maxTokens الحد الأقصى للرموز
     * @return استجابة الذكاء الاصطناعي
     */
    suspend fun chat(
        messages: List<AiChatMessage>,
        systemPrompt: String? = null,
        maxTokens: Int = 2048
    ): AiResponse

    /**
     * تلخيص النص
     * @param text النص المطلوب تلخيصه
     * @param language لغة النص
     * @return نتيجة التلخيص
     */
    suspend fun summarize(text: String, language: String = "ar"): SummaryResult

    /**
     * شرح نص معين
     * @param text النص المطلوب شرحه
     * @param question سؤال محدد (اختياري)
     * @return نتيجة الشرح
     */
    suspend fun explain(text: String, question: String = ""): ExplanationResult

    /**
     * ترجمة النص
     * @param text النص المطلوب ترجمته
     * @param from لغة المصدر
     * @param to لغة الهدف
     * @return نتيجة الترجمة
     */
    suspend fun translate(text: String, from: String = "ar", to: String = "en"): TranslationResult

    /**
     * توليد أسئلة من النص
     * @param text النص المصدر
     * @return قائمة الأسئلة
     */
    suspend fun generateQuestions(text: String): List<Question>

    /**
     * استخراج النقاط الرئيسية
     * @param text النص المصدر
     * @return قائمة النقاط الرئيسية
     */
    suspend fun extractKeyPoints(text: String): List<String>

    /**
     * تحليل مستوى الصعوبة
     * @param text النص المطلوب تحليله
     * @return تحليل الصعوبة
     */
    suspend fun analyzeDifficulty(text: String): DifficultyAnalysis

    /**
     * توصية بكتب
     * @param context سياق التوصية (مثل: اهتمامات القارئ)
     * @return قائمة التوصيات
     */
    suspend fun recommend(context: String): List<BookRecommendation>
}

/**
 * رسالة محادثة الذكاء الاصطناعي
 */
data class AiChatMessage(
    /** محتوى الرسالة */
    val content: String,
    /** دور المرسل */
    val role: AiRole = AiRole.USER
)

/**
 * دور المرسل في المحادثة
 */
enum class AiRole {
    /** مستخدم */
    USER,
    /** مساعد ذكي */
    ASSISTANT,
    /** نظام */
    SYSTEM
}

/**
 * استجابة الذكاء الاصطناعي
 */
data class AiResponse(
    /** نص الاستجابة */
    val text: String,
    /** اسم المزود الذي أنتج الاستجابة */
    val providerName: String,
    /** عدد الرموز المستخدمة */
    val tokensUsed: Int = 0,
    /** هل كانت من ذاكرة التخزين المؤقت */
    val fromCache: Boolean = false,
    /** هل نجح الطلب */
    val isSuccess: Boolean = true,
    /** رسالة الخطأ في حال الفشل */
    val errorMessage: String = ""
)

/**
 * نتيجة التلخيص
 */
data class SummaryResult(
    /** الملخص */
    val summary: String,
    /** نسبة الضغط (النص الأصلي / الملخص) */
    val compressionRatio: Float = 0f,
    /** اسم المزود */
    val providerName: String
)

/**
 * نتيجة الشرح
 */
data class ExplanationResult(
    /** الشرح */
    val explanation: String,
    /** المفاهيم المفتاحية */
    val keyConcepts: List<String> = emptyList(),
    /** اسم المزود */
    val providerName: String
)

/**
 * نتيجة الترجمة
 */
data class TranslationResult(
    /** النص المترجم */
    val translatedText: String,
    /** لغة المصدر المكتشفة */
    val detectedSourceLanguage: String = "",
    /** مستوى الثقة */
    val confidence: Float = 1f,
    /** اسم المزود */
    val providerName: String
)

/**
 * سؤال
 */
data class Question(
    /** نص السؤال */
    val text: String,
    /** نوع السؤال */
    val type: QuestionType = QuestionType.OPEN_ENDED,
    /** الإجابة المقترحة (اختياري) */
    val suggestedAnswer: String = ""
)

/**
 * نوع السؤال
 */
enum class QuestionType(val arabicName: String) {
    /** سؤال مفتوح */
    OPEN_ENDED("سؤال مفتوح"),
    /** سؤال اختيار من متعدد */
    MULTIPLE_CHOICE("اختيار من متعدد"),
    /** صح أو خطأ */
    TRUE_FALSE("صح أو خطأ"),
    /** سؤال تأملي */
    REFLECTIVE("سؤال تأملي")
}

/**
 * تحليل الصعوبة
 */
data class DifficultyAnalysis(
    /** مستوى الصعوبة */
    val level: DifficultyLevel = DifficultyLevel.MEDIUM,
    /** درجة الصعوبة (0-100) */
    val score: Int = 50,
    /** شرح مستوى الصعوبة */
    val explanation: String = "",
    /** اسم المزود */
    val providerName: String
)

/**
 * مستوى الصعوبة
 */
enum class DifficultyLevel(val arabicName: String) {
    /** سهل */
    EASY("سهل"),
    /** متوسط */
    MEDIUM("متوسط"),
    /** صعب */
    HARD("صعب"),
    /** متقدم */
    ADVANCED("متقدم")
}

/**
 * توصية بكتاب
 */
data class BookRecommendation(
    /** عنوان الكتاب */
    val title: String,
    /** المؤلف */
    val author: String,
    /** سبب التوصية */
    val reason: String = "",
    /** التصنيف */
    val category: String = ""
)
