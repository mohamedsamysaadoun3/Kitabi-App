package com.kitabi.app.provider.book

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.kitabi.app.domain.model.BookFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود محتوى PDF
 * يستخدم PdfRenderer المدمج في أندرويد لقراءة ملفات PDF
 * لا يحتاج إلى مكتبات خارجية
 * يدعم API 21 وما فوق
 */
@Singleton
class PdfContentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BookContentProvider {

    /** الصيغ المدعومة: PDF فقط */
    override val supportedFormats: Set<BookFormat> = setOf(BookFormat.PDF)

    /** محلل PDF */
    private var pdfRenderer: PdfRenderer? = null

    /** واصف الملف */
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    /** هل الكتاب مفتوح حالياً */
    private var isOpen = false

    /** نصوص الصفحات المستخرجة للتخزين المؤقت */
    private val pageTextCache = mutableMapOf<Int, String>()

    /**
     * فتح كتاب PDF
     * @param uri مسار ملف PDF
     * @return محتوى الكتاب
     */
    override suspend fun openBook(uri: Uri): BookContent {
        try {
            // إغلاق أي كتاب مفتوح سابقاً
            closeBook()

            // فتح ملف PDF
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: throw BookProviderException("فشل فتح ملف PDF: لا يمكن الوصول للملف")

            pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
            isOpen = true

            // مسح التخزين المؤقت
            pageTextCache.clear()

            val pageCount = pdfRenderer!!.pageCount
            val metadata = getMetadata(uri)
            val toc = getTableOfContents()

            return BookContent(
                pageCount = pageCount,
                toc = toc,
                metadata = metadata
            )
        } catch (e: BookProviderException) {
            throw e
        } catch (e: Exception) {
            throw BookProviderException("فشل فتح ملف PDF: ${e.message}", e)
        }
    }

    /**
     * إغلاق الكتاب وتحرير الموارد
     */
    override suspend fun closeBook() {
        try {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        } catch (_: Exception) {
            // تجاهل أخطاء الإغلاق
        } finally {
            pdfRenderer = null
            parcelFileDescriptor = null
            isOpen = false
            pageTextCache.clear()
        }
    }

    /**
     * الحصول على عدد صفحات PDF
     * @return عدد الصفحات
     */
    override suspend fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }

    /**
     * الحصول على محتوى صفحة PDF
     * ملاحظة: PdfRenderer لا يدعم استخراج النص مباشرة
     * يتم عرض معلومات الصفحة بدلاً من ذلك
     * @param index فهرس الصفحة
     * @return محتوى الصفحة
     */
    override suspend fun getPage(index: Int): PageContent {
        val renderer = pdfRenderer ?: throw BookProviderException("الكتاب غير مفتوح")
        if (index < 0 || index >= renderer.pageCount) {
            throw BookProviderException("فهرس الصفحة خارج النطاق: $index")
        }

        // التحقق من التخزين المؤقت
        pageTextCache[index]?.let { cachedText ->
            return PageContent(
                index = index,
                text = cachedText,
                type = PageType.MIXED
            )
        }

        // فتح الصفحة واستخراج المعلومات
        val page = renderer.openPage(index)
        try {
            // PdfRenderer لا يوفر استخراج نص مباشر
            // نُنشئ نصاً وصفياً للصفحة
            val pageInfo = buildString {
                append("صفحة ${index + 1}")
                append("\n")
                append("الأبعاد: ${page.width}×${page.height}")
                append("\n\n")
                append("محتوى صفحة PDF رقم ${index + 1}")
                append("\n")
                append("للحصول على أفضل تجربة قراءة، يُفضل استخدام عارض PDF متخصص")
            }

            // حفظ في التخزين المؤقت
            pageTextCache[index] = pageInfo

            return PageContent(
                index = index,
                text = pageInfo,
                type = PageType.MIXED // PDF يحتوي على نص وصور
            )
        } finally {
            page.close()
        }
    }

    /**
     * الحصول على جدول المحتويات
     * PdfRenderer لا يدعم استخراج الفهرس تلقائياً
     * يتم إنشاء فهرس بسيط بناءً على عدد الصفحات
     * @return قائمة عناصر الفهرس
     */
    override suspend fun getTableOfContents(): List<TocItem> {
        val pageCount = getPageCount()
        if (pageCount == 0) return emptyList()

        // إنشاء فهرس بسيط كل 10 صفحات
        val tocItems = mutableListOf<TocItem>()
        val chapterSize = if (pageCount <= 30) 10 else if (pageCount <= 100) 20 else 50

        for (i in 0 until pageCount step chapterSize) {
            val endIndex = minOf(i + chapterSize, pageCount)
            tocItems.add(
                TocItem(
                    title = "الصفحات ${i + 1} - $endIndex",
                    pageIndex = i,
                    level = 0
                )
            )
        }

        return tocItems
    }

    /**
     * البحث في نصوص PDF
     * نظراً لعدم دعم استخراج النص، يتم البحث في النصوص المخزنة مؤقتاً
     * @param query نص البحث
     * @return قائمة نتائج البحث
     */
    override suspend fun searchText(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()
        val renderer = pdfRenderer ?: return emptyList()

        // البحث في النصوص المخزنة مؤقتاً
        for ((pageIndex, text) in pageTextCache) {
            var searchPos = 0
            while (text.indexOf(query, searchPos, ignoreCase = true).also { searchPos = it } != -1) {
                // استخراج السياق المحيط بالنص
                val contextStart = maxOf(0, searchPos - 50)
                val contextEnd = minOf(text.length, searchPos + query.length + 50)
                val contextText = text.substring(contextStart, contextEnd)

                results.add(
                    SearchResult(
                        text = text.substring(searchPos, searchPos + query.length),
                        pageIndex = pageIndex,
                        position = searchPos,
                        context = contextText
                    )
                )
                searchPos += query.length
            }
        }

        return results
    }

    /**
     * استخراج البيانات الوصفية من ملف PDF
     * PdfRenderer لا يدعم استخراج البيانات الوصفية مباشرة
     * يتم استخراج المعلومات الأساسية من اسم الملف
     * @param uri مسار ملف PDF
     * @return البيانات الوصفية
     */
    override suspend fun getMetadata(uri: Uri): BookMetadata {
        val renderer = pdfRenderer
        val fileName = uri.lastPathSegment ?: "كتاب PDF"

        return BookMetadata(
            title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " "),
            language = "ar"
        )
    }
}
