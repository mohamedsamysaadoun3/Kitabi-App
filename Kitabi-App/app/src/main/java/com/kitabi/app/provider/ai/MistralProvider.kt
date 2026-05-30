package com.kitabi.app.provider.ai

import android.util.Log
import com.kitabi.app.domain.model.AiFeature
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود Mistral للذكاء الاصطناعي
 * يستخدم Mistral Large المجاني عبر واجهة برمجة التطبيقات
 * يدعم المحادثة، التلخيص، الشرح، وأكثر باللغة العربية
 */
@Singleton
class MistralProvider @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val remoteConfig: FirebaseRemoteConfig,
    private val cache: AiResponseCache
) : AiProvider {

    companion object {
        private const val TAG = "MistralProvider"
        private const val BASE_URL = "https://api.mistral.ai/v1/chat/completions"
        private const val MODEL = "mistral-large-latest"
        private const val REMOTE_CONFIG_KEY = "mistral_api_key"
    }

    override val name: String = "Mistral"

    override val supportedFeatures: Set<AiFeature> = setOf(
        AiFeature.SUMMARIZE,
        AiFeature.EXPLAIN,
        AiFeature.TRANSLATE,
        AiFeature.Q_AND_A,
        AiFeature.SIMILAR_BOOKS,
        AiFeature.GENERATE_NOTES,
        AiFeature.CHARACTER_ANALYSIS
    )

    override val supportsArabic: Boolean = true
    override val isFree: Boolean = true

    /** مفتاح API من Remote Config */
    private val apiKey: String
        get() = remoteConfig.getString(REMOTE_CONFIG_KEY)

    /**
     * محادثة مع Mistral
     */
    override suspend fun chat(
        messages: List<AiChatMessage>,
        systemPrompt: String?,
        maxTokens: Int
    ): AiResponse = withContext(Dispatchers.IO) {
        val cacheKey = cache.createKey(name, "chat", messages.toString() + (systemPrompt ?: ""))
        cache.get(cacheKey)?.let {
            return@withContext AiResponse(text = it, providerName = name, fromCache = true)
        }

        try {
            val jsonBody = buildChatRequest(messages, systemPrompt, maxTokens)
            val response = executeRequest(jsonBody)
            val responseText = parseChatResponse(response)

            cache.put(cacheKey, responseText)
            AiResponse(text = responseText, providerName = name)
        } catch (e: Exception) {
            Log.e(TAG, "فشل المحادثة: ${e.message}")
            AiResponse(
                text = "",
                providerName = name,
                isSuccess = false,
                errorMessage = e.message ?: "خطأ غير معروف"
            )
        }
    }

    /**
     * تلخيص النص باستخدام Mistral
     */
    override suspend fun summarize(text: String, language: String): SummaryResult {
        val prompt = if (language == "ar") {
            """لخص النص التالي باللغة العربية بشكل مختصر وواضح، مع ذكر النقاط الرئيسية:
            
            النص:
            $text"""
        } else {
            "Summarize the following text concisely:\n\n$text"
        }

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت مساعد ذكي متخصص في تلخيص النصوص العربية. قدم ملخصات دقيقة ومفيدة."
        )

        return SummaryResult(
            summary = response.text,
            compressionRatio = if (text.isNotEmpty()) response.text.length.toFloat() / text.length else 0f,
            providerName = name
        )
    }

    /**
     * شرح النص باستخدام Mistral
     */
    override suspend fun explain(text: String, question: String): ExplanationResult {
        val prompt = if (question.isNotEmpty()) {
            """اشرح النص التالي وأجب على السؤال: $question

            النص:
            $text"""
        } else {
            """اشرح النص التالي بأسلوب بسيط وواضح:

            النص:
            $text"""
        }

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت مساعد ذكي متخصص في شرح النصوص العربية. اشرح بأسلوب سهل ومفهوم."
        )

        return ExplanationResult(
            explanation = response.text,
            providerName = name
        )
    }

    /**
     * ترجمة النص باستخدام Mistral
     */
    override suspend fun translate(text: String, from: String, to: String): TranslationResult {
        val fromLang = if (from == "ar") "العربية" else "الإنجليزية"
        val toLang = if (to == "ar") "العربية" else "الإنجليزية"

        val prompt = """ترجم النص التالي من $fromLang إلى $toLang مع الحفاظ على المعنى والسياق:

        النص:
        $text"""

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت مترجم محترف. ترجم بدقة مع الحفاظ على الأسلوب والمعنى."
        )

        return TranslationResult(
            translatedText = response.text,
            detectedSourceLanguage = from,
            providerName = name
        )
    }

    /**
     * توليد أسئلة من النص
     */
    override suspend fun generateQuestions(text: String): List<Question> {
        val prompt = """أنشئ 5 أسئلة متنوعة من النص التالي، مع تحديد نوع كل سؤال:

        النص:
        $text

        أجب بصيغة JSON كالتالي:
        [{"text": "السؤال", "type": "OPEN_ENDED", "suggestedAnswer": "الإجابة المقترحة"}]

        أنواع الأسئلة: OPEN_ENDED, MULTIPLE_CHOICE, TRUE_FALSE, REFLECTIVE"""

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت معلم ذكي متخصص في إنشاء أسئلة تعليمية من النصوص العربية."
        )

        return try {
            val jsonText = extractJsonArray(response.text)
            val jsonArray = JSONArray(jsonText)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Question(
                    text = obj.optString("text", ""),
                    type = try {
                        QuestionType.valueOf(obj.optString("type", "OPEN_ENDED"))
                    } catch (_: Exception) {
                        QuestionType.OPEN_ENDED
                    },
                    suggestedAnswer = obj.optString("suggestedAnswer", "")
                )
            }.filter { it.text.isNotEmpty() }
        } catch (_: Exception) {
            // في حال فشل تحليل JSON، نعيد أسئلة من النص الخام
            response.text.split("\n")
                .filter { it.trim().startsWith("-") || it.trim().matches(Regex("^\\d+\\..*")) }
                .take(5)
                .map { Question(text = it.trim().removePrefix("-").removePrefix("\\d+\\.".toRegex())) }
        }
    }

    /**
     * استخراج النقاط الرئيسية
     */
    override suspend fun extractKeyPoints(text: String): List<String> {
        val prompt = """استخرج النقاط الرئيسية من النص التالي:

        النص:
        $text

        أجب بقائمة نقطية، كل نقطة في سطر منفصل تبدأ بـ -"""

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت محلل نصوص ذكي. استخرج النقاط الرئيسية بدقة ووضوح."
        )

        return response.text.split("\n")
            .map { it.trim() }
            .filter { it.startsWith("-") || it.startsWith("•") || it.matches(Regex("^\\d+[.،].*")) }
            .map { it.removePrefix("-").removePrefix("•").trim().removePrefix("\\d+[.،]".toRegex()).trim() }
            .filter { it.isNotEmpty() }
    }

    /**
     * تحليل مستوى الصعوبة
     */
    override suspend fun analyzeDifficulty(text: String): DifficultyAnalysis {
        val prompt = """حلل مستوى صعوبة النص التالي:

        النص:
        $text

        أجب بصيغة JSON كالتالي:
        {"level": "EASY أو MEDIUM أو HARD أو ADVANCED", "score": 50, "explanation": "شرح مستوى الصعوبة"}"""

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت محلل نصوص متخصص في تحديد مستوى صعوبة النصوص العربية."
        )

        return try {
            val jsonText = extractJsonObject(response.text)
            val json = JSONObject(jsonText)
            DifficultyAnalysis(
                level = try {
                    DifficultyLevel.valueOf(json.optString("level", "MEDIUM"))
                } catch (_: Exception) {
                    DifficultyLevel.MEDIUM
                },
                score = json.optInt("score", 50),
                explanation = json.optString("explanation", ""),
                providerName = name
            )
        } catch (_: Exception) {
            DifficultyAnalysis(providerName = name)
        }
    }

    /**
     * توصية بكتب
     */
    override suspend fun recommend(context: String): List<BookRecommendation> {
        val prompt = """بناءً على السياق التالي، اقترح 5 كتب عربية مناسبة:

        السياق: $context

        أجب بصيغة JSON كالتالي:
        [{"title": "عنوان الكتاب", "author": "المؤلف", "reason": "سبب التوصية", "category": "التصنيف"}]"""

        val response = chat(
            messages = listOf(AiChatMessage(content = prompt)),
            systemPrompt = "أنت مكتشف كتب ذكي. اقترح كتباً عربية مميزة ومناسبة."
        )

        return try {
            val jsonText = extractJsonArray(response.text)
            val jsonArray = JSONArray(jsonText)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                BookRecommendation(
                    title = obj.optString("title", ""),
                    author = obj.optString("author", ""),
                    reason = obj.optString("reason", ""),
                    category = obj.optString("category", "")
                )
            }.filter { it.title.isNotEmpty() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * بناء طلب المحادثة بصيغة JSON
     */
    private fun buildChatRequest(
        messages: List<AiChatMessage>,
        systemPrompt: String?,
        maxTokens: Int
    ): String {
        val jsonMessages = JSONArray()

        // إضافة تعليمات النظام
        if (systemPrompt != null) {
            val systemMsg = JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            }
            jsonMessages.put(systemMsg)
        }

        // إضافة تعليمات النظام الافتراضية للعربية
        val defaultSystemMsg = JSONObject().apply {
            put("role", "system")
            put("content", "أنت مساعد ذكي لتطبيق كتابي للقراءة العربية. أجب دائماً باللغة العربية بأسلوب واضح ومفيد. كن مختصراً ودقيقاً.")
        }
        if (systemPrompt == null) {
            jsonMessages.put(defaultSystemMsg)
        }

        // إضافة رسائل المستخدم
        messages.forEach { msg ->
            val jsonMsg = JSONObject().apply {
                put("role", when (msg.role) {
                    AiRole.USER -> "user"
                    AiRole.ASSISTANT -> "assistant"
                    AiRole.SYSTEM -> "system"
                })
                put("content", msg.content)
            }
            jsonMessages.put(jsonMsg)
        }

        return JSONObject().apply {
            put("model", MODEL)
            put("messages", jsonMessages)
            put("max_tokens", maxTokens)
            put("temperature", 0.7)
        }.toString()
    }

    /**
     * تنفيذ طلب HTTP إلى Mistral API
     */
    private fun executeRequest(jsonBody: String): String {
        val key = apiKey
        if (key.isBlank()) {
            throw IllegalStateException("مفتاح Mistral API غير متوفر من Remote Config")
        }

        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $key")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "خطأ غير معروف"
            throw Exception("خطأ API (${response.code}): $errorBody")
        }

        return response.body?.string() ?: throw Exception("استجابة فارغة")
    }

    /**
     * تحليل استجابة المحادثة من JSON
     */
    private fun parseChatResponse(responseBody: String): String {
        val json = JSONObject(responseBody)
        val choices = json.getJSONArray("choices")
        if (choices.length() == 0) {
            throw Exception("لا توجد استجابة من Mistral")
        }
        val firstChoice = choices.getJSONObject(0)
        val message = firstChoice.getJSONObject("message")
        return message.optString("content", "").trim()
    }

    /**
     * استخراج مصفوفة JSON من النص
     */
    private fun extractJsonArray(text: String): String {
        val startIndex = text.indexOf('[')
        val endIndex = text.lastIndexOf(']')
        if (startIndex >= 0 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1)
        }
        throw Exception("لم يتم العثور على مصفوفة JSON")
    }

    /**
     * استخراج كائن JSON من النص
     */
    private fun extractJsonObject(text: String): String {
        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')
        if (startIndex >= 0 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1)
        }
        throw Exception("لم يتم العثور على كائن JSON")
    }
}
