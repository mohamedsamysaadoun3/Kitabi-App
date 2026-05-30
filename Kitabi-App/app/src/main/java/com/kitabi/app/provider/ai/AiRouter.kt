package com.kitabi.app.provider.ai

import android.util.Log
import com.kitabi.app.domain.model.AiFeature
import javax.inject.Inject
import javax.inject.Singleton

/**
 * موجه طلبات الذكاء الاصطناعي الذكي
 * يوزع الطلبات بين المزودين حسب نوع المهمة
 * يدعم الانتقال التلقائي للمزود البديل عند فشل المزود الأساسي
 *
 * قواعد التوجيه:
 * - المحادثة/الشرح/الأسئلة/التلخيص(عربي)/التوصيات → Mistral
 * - الترجمة(عربي↔إنجليزي)/تحليل المشاعر → HuggingFace
 * - تحويل النص لكلام → Android TTS (محلي)
 * - في حال فشل المزود الأساسي → محاولة المزود البديل
 */
@Singleton
class AiRouter @Inject constructor(
    private val mistralProvider: MistralProvider,
    private val huggingFaceProvider: HuggingFaceProvider,
    private val ttsProvider: AndroidTtsProvider,
    private val cache: AiResponseCache
) {

    companion object {
        private const val TAG = "AiRouter"
    }

    /** المزود المفضل للمستخدم */
    var preferredProvider: String = "mistral"

    /**
     * محادثة مع الذكاء الاصطناعي
     * المزود الأساسي: Mistral
     */
    suspend fun chat(
        messages: List<AiChatMessage>,
        systemPrompt: String? = null,
        maxTokens: Int = 2048
    ): AiResponse {
        return tryWithFallback(
            primary = { mistralProvider.chat(messages, systemPrompt, maxTokens) },
            fallback = { huggingFaceProvider.chat(messages, systemPrompt, maxTokens) },
            feature = "chat"
        )
    }

    /**
     * تلخيص النص
     * المزود الأساسي: Mistral (للعربية)، HuggingFace (للإنجليزية)
     */
    suspend fun summarize(text: String, language: String = "ar"): SummaryResult {
        return if (language == "ar") {
            try {
                mistralProvider.summarize(text, language)
            } catch (e: Exception) {
                Log.w(TAG, "فشل التلخيص من Mistral، محاولة HuggingFace: ${e.message}")
                huggingFaceProvider.summarize(text, language)
            }
        } else {
            try {
                huggingFaceProvider.summarize(text, language)
            } catch (e: Exception) {
                Log.w(TAG, "فشل التلخيص من HuggingFace، محاولة Mistral: ${e.message}")
                mistralProvider.summarize(text, language)
            }
        }
    }

    /**
     * شرح النص
     * المزود الأساسي: Mistral
     */
    suspend fun explain(text: String, question: String = ""): ExplanationResult {
        return try {
            mistralProvider.explain(text, question)
        } catch (e: Exception) {
            Log.e(TAG, "فشل الشرح: ${e.message}")
            ExplanationResult(
                explanation = "عذراً، لم أتمكن من شرح النص. حاول مرة أخرى.",
                providerName = "none"
            )
        }
    }

    /**
     * ترجمة النص
     * المزود الأساسي: HuggingFace (للترجمة المتخصصة)
     * المزود البديل: Mistral
     */
    suspend fun translate(text: String, from: String = "ar", to: String = "en"): TranslationResult {
        return try {
            // استخدام HuggingFace للترجمة المتخصصة
            huggingFaceProvider.translate(text, from, to)
        } catch (e: Exception) {
            Log.w(TAG, "فشلت الترجمة من HuggingFace، محاولة Mistral: ${e.message}")
            try {
                mistralProvider.translate(text, from, to)
            } catch (e2: Exception) {
                Log.e(TAG, "فشلت الترجمة من جميع المزودين: ${e2.message}")
                TranslationResult(
                    translatedText = "",
                    detectedSourceLanguage = from,
                    providerName = "none"
                )
            }
        }
    }

    /**
     * توليد أسئلة
     * المزود الأساسي: Mistral
     */
    suspend fun generateQuestions(text: String): List<Question> {
        return try {
            mistralProvider.generateQuestions(text)
        } catch (e: Exception) {
            Log.e(TAG, "فشل توليد الأسئلة: ${e.message}")
            emptyList()
        }
    }

    /**
     * استخراج النقاط الرئيسية
     * المزود الأساسي: Mistral
     */
    suspend fun extractKeyPoints(text: String): List<String> {
        return try {
            mistralProvider.extractKeyPoints(text)
        } catch (e: Exception) {
            Log.e(TAG, "فشل استخراج النقاط: ${e.message}")
            emptyList()
        }
    }

    /**
     * تحليل مستوى الصعوبة
     * المزود الأساسي: Mistral
     */
    suspend fun analyzeDifficulty(text: String): DifficultyAnalysis {
        return try {
            mistralProvider.analyzeDifficulty(text)
        } catch (e: Exception) {
            Log.e(TAG, "فشل تحليل الصعوبة: ${e.message}")
            DifficultyAnalysis(providerName = "none")
        }
    }

    /**
     * توصية بكتب
     * المزود الأساسي: Mistral
     */
    suspend fun recommend(context: String): List<BookRecommendation> {
        return try {
            mistralProvider.recommend(context)
        } catch (e: Exception) {
            Log.e(TAG, "فشل التوصية: ${e.message}")
            emptyList()
        }
    }

    /**
     * تحليل المشاعر
     * المزود: HuggingFace فقط
     */
    suspend fun analyzeSentiment(text: String): List<SentimentResult> {
        return try {
            huggingFaceProvider.analyzeSentiment(text)
        } catch (e: Exception) {
            Log.e(TAG, "فشل تحليل المشاعر: ${e.message}")
            emptyList()
        }
    }

    /**
     * نطق النص بالعربية
     * المزود: Android TTS (محلي بدون إنترنت)
     */
    fun speakText(text: String) {
        ttsProvider.speak(text)
    }

    /**
     * إيقاف النطق
     */
    fun stopSpeaking() {
        ttsProvider.stop()
    }

    /**
     * تحديث سرعة النطق
     * @param speed سرعة الكلام (1.0 = عادي)
     */
    fun setTtsSpeed(speed: Float) {
        ttsProvider.speechRate = speed
    }

    /**
     * هل المحرك يتحدث حالياً
     */
    fun isSpeaking(): Boolean = ttsProvider.isCurrentlySpeaking()

    /**
     * الحصول على مزود الذكاء الاصطناعي للميزة المحددة
     */
    fun getProviderForFeature(feature: AiFeature): AiProvider {
        return when (feature) {
            AiFeature.TRANSLATE,
            AiFeature.SENTIMENT_ANALYSIS -> huggingFaceProvider
            else -> mistralProvider
        }
    }

    /**
     * محاولة مع مزود بديل
     */
    private suspend fun tryWithFallback(
        primary: suspend () -> AiResponse,
        fallback: suspend () -> AiResponse,
        feature: String
    ): AiResponse {
        return try {
            val result = primary()
            if (result.isSuccess) {
                result
            } else {
                Log.w(TAG, "فشل المزود الأساسي لـ $feature، محاولة البديل")
                fallback()
            }
        } catch (e: Exception) {
            Log.w(TAG, "استثناء من المزود الأساسي لـ $feature: ${e.message}")
            try {
                fallback()
            } catch (e2: Exception) {
                AiResponse(
                    text = "عذراً، حدث خطأ في معالجة طلبك. حاول مرة أخرى لاحقاً.",
                    providerName = "none",
                    isSuccess = false,
                    errorMessage = "${e.message} | ${e2.message}"
                )
            }
        }
    }
}
