package com.kitabi.app.provider.ai

/**
 * أسماء نماذج HuggingFace المستخدمة
 * كل نموذج مخصص لمهمة معينة
 */
object HuggingFaceModels {

    /** ترجمة من العربية إلى الإنجليزية */
    const val TRANSLATION_AR_EN = "Helsinki-NLP/opus-mt-ar-en"

    /** ترجمة من الإنجليزية إلى العربية */
    const val TRANSLATION_EN_AR = "Helsinki-NLP/opus-mt-en-ar"

    /** تحليل المشاعر متعدد اللغات */
    const val SENTIMENT = "cardiffnlp/twitter-xlm-roberta-base-sentiment-multilingual"

    /** تحويل النص إلى كلام عربي */
    const val TTS_ARABIC = "facebook/mms-tts-ara"

    /** تلخيص النصوص */
    const val SUMMARIZATION = "facebook/bart-large-cnn"

    /** الرابط الأساسي لـ HuggingFace Inference API */
    const val BASE_URL = "https://api-inference.huggingface.co/models/"
}
