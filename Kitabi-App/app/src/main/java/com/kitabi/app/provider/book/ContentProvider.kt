package com.kitabi.app.provider.book

import android.net.Uri
import com.kitabi.app.domain.model.BookFormat

/**
 * واجهة مزود محتوى الكتاب
 * تعرف العمليات الأساسية للتعامل مع صيغ الكتب المختلفة
 * كل صيغة (PDF، EPUB، TXT) تنفذ هذه الواجهة
 */
interface BookContentProvider {

    /** الصيغ المدعومة من هذا المزود */
    val supportedFormats: Set<BookFormat>

    /**
     * فتح كتاب من مسار محدد
     * @param uri مسار ملف الكتاب
     * @return محتوى الكتاب مع معلومات الصفحات والفهرس
     * @throws BookProviderException في حالة فشل فتح الملف
     */
    suspend fun openBook(uri: Uri): BookContent

    /**
     * إغلاق الكتاب وتحرير الموارد
     * يجب استدعاؤها بعد الانتهاء من القراءة
     */
    suspend fun closeBook()

    /**
     * الحصول على عدد صفحات الكتاب
     * @return عدد الصفحات
     */
    suspend fun getPageCount(): Int

    /**
     * الحصول على محتوى صفحة محددة
     * @param index فهرس الصفحة (يبدأ من 0)
     * @return محتوى الصفحة
     * @throws BookProviderException في حالة تجاوز الفهرس
     */
    suspend fun getPage(index: Int): PageContent

    /**
     * الحصول على جدول المحتويات
     * @return قائمة عناصر الفهرس
     */
    suspend fun getTableOfContents(): List<TocItem>

    /**
     * البحث في نص الكتاب
     * @param query نص البحث
     * @return قائمة نتائج البحث
     */
    suspend fun searchText(query: String): List<SearchResult>

    /**
     * استخراج البيانات الوصفية للكتاب
     * @param uri مسار ملف الكتاب
     * @return البيانات الوصفية (العنوان، المؤلف، إلخ)
     */
    suspend fun getMetadata(uri: Uri): BookMetadata
}

/**
 * محتوى الكتاب المفتوح
 * يحتوي على معلومات الصفحات والفهرس والبيانات الوصفية
 */
data class BookContent(
    /** عدد الصفحات */
    val pageCount: Int,
    /** جدول المحتويات */
    val toc: List<TocItem>,
    /** البيانات الوصفية */
    val metadata: BookMetadata
)

/**
 * محتوى صفحة واحدة
 */
data class PageContent(
    /** فهرس الصفحة (يبدأ من 0) */
    val index: Int,
    /** النص الموجود في الصفحة */
    val text: String,
    /** نوع الصفحة */
    val type: PageType
)

/**
 * عنصر في جدول المحتويات
 */
data class TocItem(
    /** عنوان الفصل أو القسم */
    val title: String,
    /** فهرس الصفحة */
    val pageIndex: Int,
    /** مستوى التداخل (0 = فصل، 1 = قسم، 2 = قسم فرعي) */
    val level: Int,
    /** العناصر الفرعية */
    val children: List<TocItem> = emptyList()
)

/**
 * البيانات الوصفية للكتاب
 */
data class BookMetadata(
    /** عنوان الكتاب */
    val title: String? = null,
    /** اسم المؤلف */
    val author: String? = null,
    /** دار النشر */
    val publisher: String? = null,
    /** سنة النشر */
    val year: String? = null,
    /** الرقم الدولي المعياري */
    val isbn: String? = null,
    /** وصف الكتاب */
    val description: String? = null,
    /** لغة الكتاب */
    val language: String? = null,
    /** مسار صورة الغلاف */
    val coverPath: String? = null
)

/**
 * نتيجة البحث في النص
 */
data class SearchResult(
    /** النص المطابق */
    val text: String,
    /** فهرس الصفحة */
    val pageIndex: Int,
    /** الموضع في الصفحة */
    val position: Int,
    /** النص المحيط بالنتيجة */
    val context: String
)

/**
 * نوع محتوى الصفحة
 */
enum class PageType {
    /** صفحة نصية */
    TEXT,
    /** صفحة صورة */
    IMAGE,
    /** صفحة مختلطة (نص وصور) */
    MIXED
}

/**
 * استثناء مزود المحتوى
 * يُستخدم للإشارة إلى أخطاء في قراءة ملفات الكتب
 */
class BookProviderException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
