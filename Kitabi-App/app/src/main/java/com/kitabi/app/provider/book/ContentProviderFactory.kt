package com.kitabi.app.provider.book

import android.net.Uri
import com.kitabi.app.domain.model.BookFormat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مصنع مزودي المحتوى
 * يوفر المزود المناسب حسب صيغة الكتاب
 * يحدد الصيغة تلقائياً من امتداد الملف
 */
@Singleton
class ContentProviderFactory @Inject constructor(
    /** مزود PDF */
    private val pdfProvider: PdfContentProvider,
    /** مزود EPUB */
    private val epubProvider: EpubContentProvider,
    /** مزود TXT */
    private val txtProvider: TxtContentProvider
) {

    /**
     * الحصول على المزود المناسب حسب صيغة الكتاب
     * @param format صيغة الكتاب
     * @return مزود المحتوى المناسب
     */
    fun getProvider(format: BookFormat): BookContentProvider {
        return when (format) {
            BookFormat.PDF -> pdfProvider
            BookFormat.EPUB -> epubProvider
            BookFormat.TXT -> txtProvider
        }
    }

    /**
     * الحصول على المزود المناسب حسب مسار الملف
     * يحدد الصيغة تلقائياً من امتداد الملف
     * @param uri مسار ملف الكتاب
     * @return مزود المحتوى المناسب
     */
    fun getProvider(uri: Uri): BookContentProvider {
        val format = detectFormat(uri)
        return getProvider(format)
    }

    /**
     * تحديد صيغة الكتاب من امتداد الملف
     * @param uri مسار الملف
     * @return صيغة الكتاب
     */
    fun detectFormat(uri: Uri): BookFormat {
        val fileName = uri.lastPathSegment ?: ""
        val extension = fileName.substringAfterLast(".", "txt").lowercase()
        return when (extension) {
            "pdf" -> BookFormat.PDF
            "epub" -> BookFormat.EPUB
            "txt", "text" -> BookFormat.TXT
            else -> BookFormat.TXT // الافتراضي: نص عادي
        }
    }

    /**
     * التحقق من دعم صيغة معينة
     * @param format الصيغة المراد التحقق منها
     * @return هل الصيغة مدعومة
     */
    fun isFormatSupported(format: BookFormat): Boolean {
        return when (format) {
            BookFormat.PDF -> true
            BookFormat.EPUB -> true
            BookFormat.TXT -> true
        }
    }

    /**
     * الحصول على جميع الصيغ المدعومة
     * @return مجموعة الصيغ المدعومة
     */
    fun getSupportedFormats(): Set<BookFormat> {
        return setOf(BookFormat.PDF, BookFormat.EPUB, BookFormat.TXT)
    }
}
