package com.kitabi.app.provider.book

import android.content.Context
import android.net.Uri
import com.kitabi.app.domain.model.BookFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود محتوى الملفات النصية (TXT)
 * أبسط مزود - يقرأ الملف النصي مباشرة
 * يقسم النص إلى صفحات بحجم ثابت (3000 حرف لكل صفحة)
 * يدعم ترميز UTF-8 والعربية
 * يستخرج جدول المحتويات من علامات الفصول (#)
 */
@Singleton
class TxtContentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BookContentProvider {

    /** الصيغ المدعومة: TXT */
    override val supportedFormats: Set<BookFormat> = setOf(BookFormat.TXT)

    /** عدد الأحرف في كل صفحة */
    private val charsPerPage = 3000

    /** النص الكامل للملف */
    private var fullText: String = ""

    /** الصفحات المقسمة */
    private var pages: List<String> = emptyList()

    /** هل الكتاب مفتوح */
    private var isOpen = false

    /** جدول المحتويات */
    private var tocItems: List<TocItem> = emptyList()

    /** البيانات الوصفية */
    private var metadata: BookMetadata = BookMetadata()

    /**
     * فتح كتاب نصي
     * @param uri مسار الملف النصي
     * @return محتوى الكتاب
     */
    override suspend fun openBook(uri: Uri): BookContent {
        try {
            // إغلاق أي كتاب مفتوح سابقاً
            closeBook()

            // قراءة محتوى الملف
            fullText = readTextFile(uri)

            // تقسيم النص إلى صفحات
            pages = splitIntoPages(fullText)

            // استخراج جدول المحتويات
            tocItems = extractTableOfContents(fullText)

            // استخراج البيانات الوصفية
            metadata = extractMetadata(uri, fullText)

            isOpen = true

            return BookContent(
                pageCount = pages.size,
                toc = tocItems,
                metadata = metadata
            )
        } catch (e: BookProviderException) {
            throw e
        } catch (e: Exception) {
            throw BookProviderException("فشل فتح الملف النصي: ${e.message}", e)
        }
    }

    /**
     * إغلاق الكتاب وتحرير الموارد
     */
    override suspend fun closeBook() {
        fullText = ""
        pages = emptyList()
        isOpen = false
        tocItems = emptyList()
        metadata = BookMetadata()
    }

    /**
     * الحصول على عدد الصفحات
     * @return عدد الصفحات
     */
    override suspend fun getPageCount(): Int {
        return pages.size
    }

    /**
     * الحصول على محتوى صفحة محددة
     * @param index فهرس الصفحة (يبدأ من 0)
     * @return محتوى الصفحة
     */
    override suspend fun getPage(index: Int): PageContent {
        if (!isOpen || pages.isEmpty()) {
            throw BookProviderException("الكتاب غير مفتوح")
        }
        if (index < 0 || index >= pages.size) {
            throw BookProviderException("فهرس الصفحة خارج النطاق: $index")
        }

        return PageContent(
            index = index,
            text = pages[index],
            type = PageType.TEXT
        )
    }

    /**
     * الحصول على جدول المحتويات
     * @return قائمة عناصر الفهرس
     */
    override suspend fun getTableOfContents(): List<TocItem> {
        return tocItems
    }

    /**
     * البحث في نص الكتاب
     * @param query نص البحث
     * @return قائمة نتائج البحث
     */
    override suspend fun searchText(query: String): List<SearchResult> {
        if (query.isBlank() || !isOpen) return emptyList()

        val results = mutableListOf<SearchResult>()
        var searchPos = 0

        while (fullText.indexOf(query, searchPos, ignoreCase = true).also { searchPos = it } != -1) {
            // حساب فهرس الصفحة
            val pageIndex = searchPos / charsPerPage

            // استخراج السياق المحيط
            val contextStart = maxOf(0, searchPos - 50)
            val contextEnd = minOf(fullText.length, searchPos + query.length + 50)
            val contextText = fullText.substring(contextStart, contextEnd)

            results.add(
                SearchResult(
                    text = fullText.substring(searchPos, searchPos + query.length),
                    pageIndex = pageIndex.coerceAtMost(pages.size - 1),
                    position = searchPos % charsPerPage,
                    context = contextText
                )
            )
            searchPos += query.length
        }

        return results
    }

    /**
     * استخراج البيانات الوصفية من الملف النصي
     * @param uri مسار الملف
     * @return البيانات الوصفية
     */
    override suspend fun getMetadata(uri: Uri): BookMetadata {
        return try {
            val wasOpen = isOpen
            if (!wasOpen) {
                openBook(uri)
            }
            val result = metadata
            if (!wasOpen) {
                closeBook()
            }
            result
        } catch (e: Exception) {
            BookMetadata(
                title = uri.lastPathSegment?.substringBeforeLast(".")?.replace("_", " ") ?: "ملف نصي"
            )
        }
    }

    // ============ طرق مساعدة داخلية ============

    /**
     * قراءة محتوى الملف النصي مع دعم الترميزات المختلفة
     * @param uri مسار الملف
     * @return محتوى الملف
     */
    private fun readTextFile(uri: Uri): String {
        return try {
            // محاولة القراءة بترميز UTF-8 أولاً
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()

                // محاولة الكشف عن الترميز
                val charset = detectCharset(bytes)

                String(bytes, charset)
            } ?: throw BookProviderException("فشل قراءة الملف النصي")
        } catch (e: BookProviderException) {
            throw e
        } catch (e: Exception) {
            throw BookProviderException("فشل قراءة الملف النصي: ${e.message}", e)
        }
    }

    /**
     * كشف ترميز النص
     * يدعم UTF-8 و UTF-16 و windows-1256 (العربية)
     * @param bytes بايتات الملف
     * @return الترميز المناسب
     */
    private fun detectCharset(bytes: ByteArray): Charset {
        // التحقق من BOM (Byte Order Mark)
        if (bytes.size >= 3) {
            // UTF-8 BOM
            if (bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
                return Charsets.UTF_8
            }
        }
        if (bytes.size >= 2) {
            // UTF-16 BE BOM
            if (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
                return Charsets.UTF_16BE
            }
            // UTF-16 LE BOM
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
                return Charsets.UTF_16LE
            }
        }

        // محاولة UTF-8 أولاً (الأكثر شيوعاً)
        try {
            val text = String(bytes, Charsets.UTF_8)
            // التحقق من عدم وجود أحرف غير صالحة
            if (!text.contains("\uFFFD")) {
                return Charsets.UTF_8
            }
        } catch (_: Exception) {
            // تجاهل والمحاولة بترميز آخر
        }

        // محاولة windows-1256 للنصوص العربية
        try {
            return Charset.forName("windows-1256")
        } catch (_: Exception) {
            // تجاهل
        }

        // الافتراضي: UTF-8
        return Charsets.UTF_8
    }

    /**
     * تقسيم النص إلى صفحات
     * @param text النص الكامل
     * @return قائمة الصفحات
     */
    private fun splitIntoPages(text: String): List<String> {
        if (text.isEmpty()) return listOf("")

        val pagesList = mutableListOf<String>()
        var remaining = text

        while (remaining.isNotEmpty()) {
            if (remaining.length <= charsPerPage) {
                pagesList.add(remaining.trim())
                break
            }

            // البحث عن أفضل نقطة قطع (نهاية فقرة أو سطر)
            var cutPoint = remaining.lastIndexOf('\n', charsPerPage)
            if (cutPoint <= charsPerPage / 2) {
                // إذا لم يتم العثور على سطر جديد قريب، البحث عن مسافة
                cutPoint = remaining.lastIndexOf(' ', charsPerPage)
            }
            if (cutPoint <= charsPerPage / 2) {
                // قطع عند الحد الأقصى إذا لم يتم العثور على فاصل مناسب
                cutPoint = charsPerPage
            }

            pagesList.add(remaining.substring(0, cutPoint).trim())
            remaining = remaining.substring(cutPoint).trimStart()
        }

        return pagesList.ifEmpty { listOf("") }
    }

    /**
     * استخراج جدول المحتويات من علامات الفصول
     * يبحث عن أسطر تبدأ بعلامات # (مثل Markdown)
     * أو أسطر تحتوي على كلمات مثل "فصل" أو "باب"
     * @param text النص الكامل
     * @return قائمة عناصر الفهرس
     */
    private fun extractTableOfContents(text: String): List<TocItem> {
        val tocItems = mutableListOf<TocItem>()
        val lines = text.lines()
        var charOffset = 0

        for (line in lines) {
            val trimmedLine = line.trim()

            // البحث عن علامات الفصول بأسلوب Markdown
            if (trimmedLine.startsWith("#")) {
                val level = trimmedLine.takeWhile { it == '#' }.length.coerceIn(0, 2)
                val title = trimmedLine.removePrefix("#".repeat(level + 1)).trim()
                val pageIndex = charOffset / charsPerPage

                if (title.isNotEmpty()) {
                    tocItems.add(
                        TocItem(
                            title = title,
                            pageIndex = pageIndex,
                            level = level
                        )
                    )
                }
            }
            // البحث عن علامات الفصول بالعربية
            else if (trimmedLine.matches(Regex("""^(فصل|باب|قسم|جزء|مقدمة|خاتمة)\s+\d*.*$""", RegexOption.IGNORE_CASE))) {
                val pageIndex = charOffset / charsPerPage
                tocItems.add(
                    TocItem(
                        title = trimmedLine,
                        pageIndex = pageIndex,
                        level = 0
                    )
                )
            }
            // البحث عن أرقام الفصول (مثل: الفصل الأول)
            else if (trimmedLine.matches(Regex("""^(الفصل|الباب|القسم|الجزء)\s+(الأول|الثاني|الثالث|الرابع|الخامس|السادس|السابع|الثامن|التاسع|العاشر|\d+).*"""))) {
                val pageIndex = charOffset / charsPerPage
                tocItems.add(
                    TocItem(
                        title = trimmedLine,
                        pageIndex = pageIndex,
                        level = 0
                    )
                )
            }

            charOffset += line.length + 1 // +1 للسطر الجديد
        }

        return tocItems
    }

    /**
     * استخراج البيانات الوصفية من الملف النصي
     * يحاول استخراج العنوان والمؤلف من الأسطر الأولى
     * @param uri مسار الملف
     * @param text محتوى الملف
     * @return البيانات الوصفية
     */
    private fun extractMetadata(uri: Uri, text: String): BookMetadata {
        val fileName = uri.lastPathSegment?.substringBeforeLast(".")?.replace("_", " ") ?: "ملف نصي"
        val lines = text.lines().take(20)

        var title: String? = null
        var author: String? = null

        // البحث عن العنوان في الأسطر الأولى
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // البحث عن نمط "العنوان: ..." أو "عنوان: ..."
            val titleMatch = Regex("""^(العنوان|عنوان|Title)\s*[:：]\s*(.+)""", RegexOption.IGNORE_CASE)
                .find(trimmed)
            if (titleMatch != null) {
                title = titleMatch.groupValues[2].trim()
                continue
            }

            // البحث عن نمط "المؤلف: ..." أو "الكاتب: ..."
            val authorMatch = Regex("""^(المؤلف|الكاتب|Author|By)\s*[:：]\s*(.+)""", RegexOption.IGNORE_CASE)
                .find(trimmed)
            if (authorMatch != null) {
                author = authorMatch.groupValues[2].trim()
                continue
            }
        }

        return BookMetadata(
            title = title ?: fileName,
            author = author ?: "غير معروف",
            language = detectLanguage(text)
        )
    }

    /**
     * كشف لغة النص
     * يفحص الأحرف العربية في بداية النص
     * @param text النص
     * @return رمز اللغة
     */
    private fun detectLanguage(text: String): String {
        // فحص أول 500 حرف
        val sample = text.take(500)
        val arabicCharCount = sample.count { char ->
            char.code in 0x0600..0x06FF || // العربية
            char.code in 0x0750..0x077F || // العربية الممتدة
            char.code in 0xFB50..0xFDFF || // العربية التقديمية أ
            char.code in 0xFE70..0xFEFF    // العربية التقديمية ب
        }

        val totalLetters = sample.count { it.isLetter() }
        return if (totalLetters > 0 && arabicCharCount.toFloat() / totalLetters > 0.3f) {
            "ar"
        } else {
            "en"
        }
    }
}
