package com.kitabi.app.domain.model

/**
 * التعدادات المستخدمة في تطبيق كتابي
 * تحدد القيم الممكنة لكل خاصية
 */

// ============ تعدادات الكتاب ============

/**
 * مصدر الكتاب
 */
enum class BookSource {
    /** كتاب محلي على الجهاز */
    LOCAL,

    /** كتاب إلكتروني من المتجر */
    ONLINE
}

/**
 * صيغة ملف الكتاب
 */
enum class BookFormat {
    /** صيغة PDF */
    PDF,

    /** صيغة EPUB */
    EPUB,

    /** صيغة نص عادي */
    TXT
}

/**
 * حالة التحميل
 */
enum class DownloadState {
    /** لم يتم التحميل */
    NOT_DOWNLOADED,

    /** قيد التحميل */
    DOWNLOADING,

    /** تم إيقاف التحميل */
    PAUSED,

    /** تم التحميل بنجاح */
    DOWNLOADED,

    /** فشل التحميل */
    FAILED
}

/**
 * مستوى الصعوبة - يُستخدم من provider.ai.DifficultyLevel
 * هذا مجرد نوع بديل للتوافق
 */
typealias DifficultyLevel = com.kitabi.app.provider.ai.DifficultyLevel

/**
 * تصنيف الكتاب
 */
enum class Category(val arabicName: String) {
    /** عام */
    GENERAL("عام"),

    /** أدب وروايات */
    LITERATURE("أدب وروايات"),

    /** شعر */
    POETRY("شعر"),

    /** تاريخ */
    HISTORY("تاريخ"),

    /** فلسفة */
    PHILOSOPHY("فلسفة"),

    /** دين وعلوم إسلامية */
    RELIGION("دين وعلوم إسلامية"),

    /** علم نفس */
    PSYCHOLOGY("علم نفس"),

    /** علوم */
    SCIENCE("علوم"),

    /** تقنية */
    TECHNOLOGY("تقنية"),

    /** أعمال وإدارة */
    BUSINESS("أعمال وإدارة"),

    /** تعليم */
    EDUCATION("تعليم"),

    /** فنون */
    ARTS("فنون"),

    /** سير ذاتية */
    BIOGRAPHY("سير ذاتية"),

    /** أطفال */
    CHILDREN("أطفال"),

    /** صحة وعافية */
    HEALTH("صحة وعافية"),

    /** طبخ */
    COOKING("طبخ"),

    /** سفر */
    TRAVEL("سفر"),

    /** سياسة */
    POLITICS("سياسة"),

    /** قانون */
    LAW("قانون"),

    /** لغات */
    LANGUAGES("لغات")
}

/**
 * لغة الكتاب
 */
enum class Language(val arabicName: String, val code: String) {
    /** العربية */
    ARABIC("العربية", "ar"),

    /** الإنجليزية */
    ENGLISH("الإنجليزية", "en"),

    /** الفرنسية */
    FRENCH("الفرنسية", "fr"),

    /** الألمانية */
    GERMAN("الألمانية", "de"),

    /** التركية */
    TURKISH("التركية", "tr"),

    /** الفارسية */
    PERSIAN("الفارسية", "fa"),

    /** الأردية */
    URDU("الأردية", "ur")
}

// ============ تعدادات المحادثة ============

/**
 * نوع الرسالة في المحادثة
 */
enum class MessageType {
    /** رسالة نصية */
    TEXT,

    /** رسالة تحتوي على صورة */
    IMAGE,

    /** اقتباس من كتاب */
    BOOK_QUOTE,

    /** اقتراح كتاب */
    BOOK_SUGGESTION,

    /** رسالة نظام */
    SYSTEM
}

// ============ تعدادات الذكاء الاصطناعي ============

/**
 * ميزات الذكاء الاصطناعي
 */
enum class AiFeature(val arabicName: String) {
    /** تلخيص النص */
    SUMMARIZE("تلخيص النص"),

    /** شرح المفاهيم */
    EXPLAIN("شرح المفاهيم"),

    /** ترجمة النص */
    TRANSLATE("ترجمة النص"),

    /** تحليل الشخصيات */
    CHARACTER_ANALYSIS("تحليل الشخصيات"),

    /** أسئلة وأجوبة */
    Q_AND_A("أسئلة وأجوبة"),

    /** اقتراح كتب مشابهة */
    SIMILAR_BOOKS("كتب مشابهة"),

    /** تحليل المشاعر */
    SENTIMENT_ANALYSIS("تحليل المشاعر"),

    /** إنشاء ملاحظات */
    GENERATE_NOTES("إنشاء ملاحظات")
}

/**
 * دور الذكاء الاصطناعي - يُستخدم من provider.ai.AiRole
 * هذا مجرد نوع بديل للتوافق
 */
typealias AiRole = com.kitabi.app.provider.ai.AiRole
