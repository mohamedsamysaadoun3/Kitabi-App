package com.kitabi.app.domain.model

/**
 * نموذج تقييم الكتاب
 * ملخص التقييمات لكتاب معين
 */
data class BookRating(
    /** معرف الكتاب */
    val bookId: String,

    /** متوسط التقييم */
    val averageRating: Float = 0f,

    /** عدد التقييمات */
    val ratingsCount: Int = 0,

    /** توزيع التقييمات (من 1 إلى 5) */
    val distribution: Map<Int, Int> = emptyMap()
) {
    /** تقييم مقرب لعمرض */
    val displayRating: Float get() = (Math.round(averageRating * 10.0) / 10.0).toFloat()

    /** عدد التقييمات بصيغة مقروءة */
    val displayCount: String get() = when {
        ratingsCount >= 1000 -> String.format("%.1fK", ratingsCount / 1000.0)
        else -> ratingsCount.toString()
    }
}
