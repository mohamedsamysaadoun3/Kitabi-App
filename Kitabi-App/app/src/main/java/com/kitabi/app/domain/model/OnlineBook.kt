package com.kitabi.app.domain.model

/**
 * نموذج الكتاب الإلكتروني من المتجر
 * يمثل كتاباً متاحاً للتحميل من مصادر إلكترونية مختلفة
 */
data class OnlineBook(
    /** المعرف الفريد */
    val id: String,

    /** عنوان الكتاب */
    val title: String,

    /** اسم المؤلف */
    val author: String,

    /** رابط صورة الغلاف */
    val coverUrl: String = "",

    /** وصف الكتاب */
    val description: String = "",

    /** تقييم الكتاب (0-5) */
    val rating: Float = 0f,

    /** عدد التقييمات */
    val ratingsCount: Int = 0,

    /** عدد الصفحات */
    val pageCount: Int = 0,

    /** التصنيف */
    val category: String = "GENERAL",

    /** اللغة */
    val language: String = "ar",

    /** هل الكتاب مترجم */
    val isTranslated: Boolean = false,

    /** هل الكتاب ملكية عامة */
    val isPublicDomain: Boolean = false,

    /** دار النشر */
    val publisher: String = "",

    /** سنة النشر */
    val publishYear: Int = 0,

    /** الرقم الدولي ISBN */
    val isbn: String = "",

    /** مصدر الكتاب (openlibrary, google, gutenberg) */
    val source: String = "",

    /** المعرف في المصدر */
    val sourceId: String = "",

    /** رابط المعاينة */
    val previewUrl: String = "",

    /** رابط المعلومات */
    val infoUrl: String = "",

    /** رابط التحميل */
    val downloadUrl: String = "",

    /** روابط التحميل بصيغ مختلفة */
    val downloadFormats: Map<String, String> = emptyMap(),

    /** التصنيفات */
    val subjects: List<String> = emptyList(),

    /** عدد التحميلات */
    val downloadCount: Int = 0,

    /** حقوق النشر */
    val copyright: String = ""
) {
    /** هل يوجد غلاف */
    val hasCover: Boolean get() = coverUrl.isNotBlank()

    /** هل يمكن تحميله */
    val isDownloadable: Boolean get() = downloadUrl.isNotBlank() || downloadFormats.isNotEmpty()

    /** هل يوجد وصف */
    val hasDescription: Boolean get() = description.isNotBlank()
}
