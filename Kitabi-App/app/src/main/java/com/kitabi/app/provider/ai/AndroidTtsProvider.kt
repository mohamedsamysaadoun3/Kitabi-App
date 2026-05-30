package com.kitabi.app.provider.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * مزود تحويل النص إلى كلام باللغة العربية
 * يستخدم محرك TTS المدمج في أندرويد
 * يعمل بدون اتصال بالإنترنت
 */
@Singleton
class AndroidTtsProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "AndroidTtsProvider"
    }

    /** محرك تحويل النص إلى كلام */
    private var tts: TextToSpeech? = null

    /** هل تم التهيئة بنجاح */
    private var isInitialized = false

    /** حالة التحدث */
    private var isSpeaking = false

    /** سرعة الكلام (1.0 = عادي) */
    var speechRate = 1.0f

    /** درجة الصوت (1.0 = عادي) */
    var pitch = 1.0f

    /** مستمع الأحداث */
    var listener: TtsListener? = null

    init {
        tts = TextToSpeech(context, this)
    }

    /**
     * تهيئة محرك TTS
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "اللغة العربية غير مدعومة في محرك TTS")
                // محاولة استخدام اللغة الافتراضية
                tts?.language = Locale.getDefault()
            } else {
                isInitialized = true
                Log.d(TAG, "تم تهيئة محرك TTS بنجاح للعربية")
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeaking = true
                    listener?.onSpeakStart()
                }

                override fun onDone(utteranceId: String?) {
                    isSpeaking = false
                    listener?.onSpeakDone()
                }

                override fun onError(utteranceId: String?) {
                    isSpeaking = false
                    listener?.onSpeakError("خطأ في النطق")
                }
            })
        } else {
            Log.e(TAG, "فشل تهيئة محرك TTS")
        }
    }

    /**
     * نطق النص باللغة العربية
     * @param text النص المطلوب نطقه
     * @param utteranceId معرف فريد للنطق
     */
    fun speak(text: String, utteranceId: String = "kitabi_tts_${System.currentTimeMillis()}") {
        if (!isInitialized) {
            Log.w(TAG, "محرك TTS غير مهيأ بعد")
            return
        }

        tts?.apply {
            setSpeechRate(speechRate)
            setPitch(pitch)
            speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * إيقاف النطق
     */
    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    /**
     * إيقاف مؤقت (غير مدعوم في جميع الأجهزة)
     */
    fun pause() {
        // بعض الأجهزة لا تدعم الإيقاف المؤقت
        tts?.stop()
    }

    /**
     * هل المحرك يتحدث حالياً
     */
    fun isCurrentlySpeaking(): Boolean = isSpeaking || (tts?.isSpeaking == true)

    /**
     * تحرير الموارد
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    /**
     * واجهة مستمع أحداث TTS
     */
    interface TtsListener {
        /** بدء النطق */
        fun onSpeakStart() {}
        /** انتهاء النطق */
        fun onSpeakDone() {}
        /** خطأ في النطق */
        fun onSpeakError(error: String) {}
    }
}
