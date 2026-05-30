package com.kitabi.app.provider.ai

import android.util.Log
import com.kitabi.app.domain.model.AiFeature
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود HuggingFace للذكاء الاصطناعي
 * يستخدم HuggingFace Inference API للترجمة وتحليل المشاعر
 * يدعم نماذج متعددة متخصصة لكل مهمة
 */
@Singleton
class HuggingFaceProvider @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val remoteConfig: FirebaseRemoteConfig,
    private val cache: AiResponseCache
) : AiProvider {

    companion object {
        private const val TAG = "HuggingFaceProvider"
        private const val REMOTE_CONFIG_KEY = "huggingface_api_key"
    }

    override val name: String = "HuggingFace"

    override val supportedFeatures: Set<AiFeature> = setOf(
        AiFeature.TRANSLATE,
        AiFeature.SENTIMENT_ANALYSIS
    )

    override val supportsArabic: Boolean = true
    override val isFree: Boolean = true

    /** مفتاح API من Remote Config */
    private val apiKey: String
        get() = remoteConfig.getString(REMOTE_CONFIG_KEY)

    /**
     * محادثة - غير مدعومة مباشرة من HuggingFace
     * يتم توجيهها إلى نموذج التلخيص كحل بديل
     */
    override suspend fun chat(
        messages: List<AiChatMessage>,
        systemPrompt: String?,
        maxTokens: Int
    ): AiResponse {
        // HuggingFace لا يدعم المحادثة المباشرة
        // يتم توجيه الطلب إلى Mistral عبر AiRouter
        return AiResponse(
            text = "",
            providerName = name,
            isSuccess = false,
            errorMessage = "المحادثة غير مدعومة من HuggingFace"
        )
    }

    /**
     * تلخيص النص باستخدام BART
     */
    override suspend fun summarize(text: String, language: String): SummaryResult =
        withContext(Dispatchers.IO) {
            val cacheKey = cache.createKey(name, "summarize", text.take(500))
            cache.get(cacheKey)?.let {
                return@withContext SummaryResult(summary = it, providerName = name)
            }

            try {
                val result = queryModel(HuggingFaceModels.SUMMARIZATION, text)
                val summary = parseTextGenerationResult(result)
                cache.put(cacheKey, summary)
                SummaryResult(
                    summary = summary,
                    compressionRatio = if (text.isNotEmpty()) summary.length.toFloat() / text.length else 0f,
                    providerName = name
                )
            } catch (e: Exception) {
                Log.e(TAG, "فشل التلخيص: ${e.message}")
                SummaryResult(summary = "", providerName = name)
            }
        }

    /**
     * شرح - غير مدعوم مباشرة
     */
    override suspend fun explain(text: String, question: String): ExplanationResult {
        return ExplanationResult(
            explanation = "",
            providerName = name
        )
    }

    /**
     * ترجمة النص باستخدام Helsinki-NLP
     * يدعم الترجمة بين العربية والإنجليزية
     */
    override suspend fun translate(text: String, from: String, to: String): TranslationResult =
        withContext(Dispatchers.IO) {
            val model = when {
                from == "ar" && to == "en" -> HuggingFaceModels.TRANSLATION_AR_EN
                from == "en" && to == "ar" -> HuggingFaceModels.TRANSLATION_EN_AR
                else -> HuggingFaceModels.TRANSLATION_AR_EN
            }

            val cacheKey = cache.createKey(name, "translate_${from}_${to}", text.take(500))
            cache.get(cacheKey)?.let {
                return@withContext TranslationResult(
                    translatedText = it,
                    detectedSourceLanguage = from,
                    providerName = name
                )
            }

            try {
                val result = queryModel(model, text)
                val translated = parseTranslationResult(result)
                cache.put(cacheKey, translated)
                TranslationResult(
                    translatedText = translated,
                    detectedSourceLanguage = from,
                    providerName = name
                )
            } catch (e: Exception) {
                Log.e(TAG, "فشل الترجمة: ${e.message}")
                TranslationResult(
                    translatedText = "",
                    detectedSourceLanguage = from,
                    providerName = name
                )
            }
        }

    /**
     * توليد أسئلة - غير مدعوم من HuggingFace
     */
    override suspend fun generateQuestions(text: String): List<Question> {
        return emptyList()
    }

    /**
     * استخراج نقاط رئيسية - غير مدعوم مباشرة
     */
    override suspend fun extractKeyPoints(text: String): List<String> {
        return emptyList()
    }

    /**
     * تحليل الصعوبة - غير مدعوم مباشرة
     */
    override suspend fun analyzeDifficulty(text: String): DifficultyAnalysis {
        return DifficultyAnalysis(providerName = name)
    }

    /**
     * توصية بكتب - غير مدعومة
     */
    override suspend fun recommend(context: String): List<BookRecommendation> {
        return emptyList()
    }

    /**
     * تحليل المشاعر من النص
     * @param text النص المطلوب تحليله
     * @return قائمة نتائج تحليل المشاعر
     */
    suspend fun analyzeSentiment(text: String): List<SentimentResult> = withContext(Dispatchers.IO) {
        try {
            val result = queryModel(HuggingFaceModels.SENTIMENT, text)
            parseSentimentResult(result)
        } catch (e: Exception) {
            Log.e(TAG, "فشل تحليل المشاعر: ${e.message}")
            emptyList()
        }
    }

    /**
     * إرسال طلب إلى نموذج HuggingFace
     * @param model اسم النموذج
     * @param input المدخلات
     * @return استجابة JSON كنص
     */
    private fun queryModel(model: String, input: String): String {
        val key = apiKey
        if (key.isBlank()) {
            throw IllegalStateException("مفتاح HuggingFace API غير متوفر من Remote Config")
        }

        val url = "${HuggingFaceModels.BASE_URL}$model"
        val jsonBody = JSONObject().apply {
            put("inputs", input)
        }.toString()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "خطأ غير معروف"
            throw Exception("خطأ HuggingFace API (${response.code}): $errorBody")
        }

        return response.body?.string() ?: throw Exception("استجابة فارغة")
    }

    /**
     * تحليل نتيجة الترجمة
     */
    private fun parseTranslationResult(responseBody: String): String {
        val jsonArray = JSONArray(responseBody)
        if (jsonArray.length() == 0) return ""
        val firstResult = jsonArray.getJSONObject(0)
        return firstResult.optString("translation_text", "")
    }

    /**
     * تحليل نتيجة توليد النص / التلخيص
     */
    private fun parseTextGenerationResult(responseBody: String): String {
        return try {
            val jsonArray = JSONArray(responseBody)
            if (jsonArray.length() == 0) return ""
            val firstResult = jsonArray.getJSONObject(0)
            firstResult.optString("summary_text", firstResult.optString("generated_text", ""))
        } catch (_: Exception) {
            try {
                val json = JSONObject(responseBody)
                json.optString("summary_text", json.optString("generated_text", ""))
            } catch (_: Exception) {
                ""
            }
        }
    }

    /**
     * تحليل نتيجة تحليل المشاعر
     */
    private fun parseSentimentResult(responseBody: String): List<SentimentResult> {
        val results = mutableListOf<SentimentResult>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                results.add(
                    SentimentResult(
                        label = obj.optString("label", ""),
                        score = obj.optDouble("score", 0.0).toFloat()
                    )
                )
            }
        } catch (_: Exception) { }
        return results.sortedByDescending { it.score }
    }
}

/**
 * نتيجة تحليل المشاعر
 */
data class SentimentResult(
    /** التسمية (إيجابي/سلبي/محايد) */
    val label: String,
    /** درجة الثقة */
    val score: Float
)
