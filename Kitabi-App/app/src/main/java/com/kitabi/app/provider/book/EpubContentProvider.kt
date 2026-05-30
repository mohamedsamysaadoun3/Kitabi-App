package com.kitabi.app.provider.book

import android.content.Context
import android.net.Uri
import com.kitabi.app.domain.model.BookFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * مزود محتوى EPUB
 * يحلل ملفات EPUB باستخدام ZipFile بدون مكتبات خارجية
 * بنية EPUB:
 * - mimetype (أول ملف، غير مضغوط)
 * - META-INF/container.xml (يشير إلى content.opf)
 * - OEBPS/content.opf (البيانات الوصفية + ترتيب الفصول)
 * - OEBPS/toc.ncx (جدول المحتويات)
 * - OEBPS/Text/chapter*.xhtml (المحتوى الفعلي)
 */
@Singleton
class EpubContentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : BookContentProvider {

    /** الصيغ المدعومة: EPUB فقط */
    override val supportedFormats: Set<BookFormat> = setOf(BookFormat.EPUB)

    /** ملف ZIP المفتوح */
    private var zipFile: ZipFile? = null

    /** قائمة الفصول المرتبة */
    private var chapters: List<EpubChapter> = emptyList()

    /** مسار ملف الكتاب */
    private var currentFilePath: String? = null

    /** هل الكتاب مفتوح */
    private var isOpen = false

    /** البيانات الوصفية */
    private var metadata: BookMetadata = BookMetadata()

    /** جدول المحتويات */
    private var tocItems: List<TocItem> = emptyList()

    /**
     * فتح كتاب EPUB
     * @param uri مسار ملف EPUB
     * @return محتوى الكتاب
     */
    override suspend fun openBook(uri: Uri): BookContent {
        try {
            // إغلاق أي كتاب مفتوح سابقاً
            closeBook()

            // الحصول على مسار الملف الفعلي
            val filePath = getRealFilePath(uri)
            currentFilePath = filePath

            // فتح ملف ZIP
            zipFile = ZipFile(filePath)

            // 1. تحليل container.xml للعثور على content.opf
            val opfPath = parseContainerXml()

            // 2. تحليل content.opf لاستخراج البيانات الوصفية وترتيب الفصول
            val opfData = parseContentOpf(opfPath)

            // 3. تحديث البيانات الوصفية
            metadata = opfData.metadata

            // 4. تحميل الفصول بترتيب Spine أولاً (حتاج الفصول لربط جدول المحتويات بالصفحات)
            chapters = loadChapters(opfData.spine, opfData.manifest)

            // 5. تحليل toc.ncx لجدول المحتويات (بعد تحميل الفصول لربط الفهرس بالصفحات)
            tocItems = parseTocNcx(opfData.tocPath, opfData.manifest)

            isOpen = true

            return BookContent(
                pageCount = chapters.size,
                toc = tocItems,
                metadata = metadata
            )
        } catch (e: BookProviderException) {
            throw e
        } catch (e: Exception) {
            throw BookProviderException("فشل فتح ملف EPUB: ${e.message}", e)
        }
    }

    /**
     * إغلاق الكتاب وتحرير الموارد
     */
    override suspend fun closeBook() {
        try {
            zipFile?.close()
        } catch (_: Exception) {
            // تجاهل أخطاء الإغلاق
        } finally {
            zipFile = null
            chapters = emptyList()
            currentFilePath = null
            isOpen = false
            metadata = BookMetadata()
            tocItems = emptyList()
        }
    }

    /**
     * الحصول على عدد الفصول/الصفحات
     * @return عدد الفصول
     */
    override suspend fun getPageCount(): Int {
        return chapters.size
    }

    /**
     * الحصول على محتوى فصل/صفحة
     * @param index فهرس الفصل (يبدأ من 0)
     * @return محتوى الصفحة
     */
    override suspend fun getPage(index: Int): PageContent {
        if (!isOpen || chapters.isEmpty()) {
            throw BookProviderException("الكتاب غير مفتوح")
        }
        if (index < 0 || index >= chapters.size) {
            throw BookProviderException("فهرس الصفحة خارج النطاق: $index")
        }

        val chapter = chapters[index]
        return PageContent(
            index = index,
            text = chapter.text,
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

        for ((pageIndex, chapter) in chapters.withIndex()) {
            val text = chapter.text
            var searchPos = 0

            while (text.indexOf(query, searchPos, ignoreCase = true).also { searchPos = it } != -1) {
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
     * استخراج البيانات الوصفية من ملف EPUB
     * @param uri مسار ملف EPUB
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
                title = uri.lastPathSegment?.substringBeforeLast(".")?.replace("_", " ") ?: "كتاب EPUB"
            )
        }
    }

    // ============ طرق التحليل الداخلية ============

    /**
     * الحصول على مسار الملف الفعلي من Uri
     */
    private fun getRealFilePath(uri: Uri): String {
        // محاولة الحصول على مسار الملف من URI
        val filePath = uri.path
        if (filePath != null && File(filePath).exists()) {
            return filePath
        }

        // نسخ الملف من ContentResolver إلى ملف مؤقت
        val tempFile = File(context.cacheDir, "temp_epub_${System.currentTimeMillis()}.epub")
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw BookProviderException("فشل قراءة الملف من المسار المحدد")

        return tempFile.absolutePath
    }

    /**
     * تحليل ملف container.xml للعثور على مسار content.opf
     * @return مسار ملف OPF بالنسبة لجذر ZIP
     */
    private fun parseContainerXml(): String {
        val zip = zipFile ?: throw BookProviderException("ملف ZIP غير مفتوح")

        val containerEntry = zip.getEntry("META-INF/container.xml")
            ?: throw BookProviderException("ملف container.xml غير موجود - ملف EPUB غير صالح")

        val content = zip.getInputStream(containerEntry).bufferedReader().use { it.readText() }

        // استخراج مسار OPF من container.xml
        // البحث عن السمة full-path في عنصر rootfile
        val pathRegex = Regex("""full-path\s*=\s*"([^"]+)"""")
        val match = pathRegex.find(content)
            ?: throw BookProviderException("لم يتم العثور على مسار content.opf")

        return match.groupValues[1]
    }

    /**
     * بيانات ملف OPF المحللة
     */
    private data class OpfData(
        val metadata: BookMetadata,
        val manifest: Map<String, String>,
        val spine: List<String>,
        val tocPath: String?
    )

    /**
     * تحليل ملف content.opf لاستخراج البيانات الوصفية وترتيب الفصول
     * @param opfPath مسار ملف OPF داخل ZIP
     * @return بيانات OPF المحللة
     */
    private fun parseContentOpf(opfPath: String): OpfData {
        val zip = zipFile ?: throw BookProviderException("ملف ZIP غير مفتوح")

        val opfEntry = zip.getEntry(opfPath)
            ?: throw BookProviderException("ملف content.opf غير موجود: $opfPath")

        val content = zip.getInputStream(opfEntry).bufferedReader().use { it.readText() }

        // المجلد الأساسي لملف OPF
        val opfDir = opfPath.substringBeforeLast("/", "")

        // استخراج البيانات الوصفية
        val title = extractXmlTag(content, "dc:title")?.trim()
        val author = extractXmlTag(content, "dc:creator")?.trim()
        val publisher = extractXmlTag(content, "dc:publisher")?.trim()
        val year = extractXmlTag(content, "dc:date")?.trim()?.take(4)
        val isbn = extractXmlAttribute(content, "opf:scheme", "ISBN")
        val description = extractXmlTag(content, "dc:description")?.trim()
        val language = extractXmlTag(content, "dc:language")?.trim()

        // استخراج مسار الغلاف
        val coverId = extractXmlAttribute(content, "name", "cover")
        val coverPath = coverId?.let { cid ->
            manifestEntryPath(content, cid, opfDir)
        } ?: run {
            // محاولة العثور على صورة الغلاف من manifest
            val coverMetaRegex = Regex("""<meta[^>]*name\s*=\s*"cover"[^>]*content\s*=\s*"([^"]+)"[^>]*>""")
            val coverMetaMatch = coverMetaRegex.find(content)
            coverMetaMatch?.groupValues?.get(1)?.let { cid2 ->
                manifestEntryPath(content, cid2, opfDir)
            }
        }

        // استخراج قائمة manifest (معرف -> مسار)
        val manifest = parseManifest(content, opfDir)

        // استخراج ترتيب spine
        val spine = parseSpine(content)

        // استخراج مسار جدول المحتويات
        val tocId = extractXmlAttribute(content, "properties", "nav")
            ?: extractXmlTagAttribute(content, "spine", "toc")
        val tocPath = tocId?.let { manifest[it] }

        return OpfData(
            metadata = BookMetadata(
                title = title,
                author = author,
                publisher = publisher,
                year = year,
                isbn = isbn,
                description = description,
                language = language,
                coverPath = coverPath
            ),
            manifest = manifest,
            spine = spine,
            tocPath = tocPath
        )
    }

    /**
     * تحليل قائمة manifest من OPF
     * @return خريطة من المعرف إلى المسار الكامل
     */
    private fun parseManifest(content: String, opfDir: String): Map<String, String> {
        val manifest = mutableMapOf<String, String>()
        val itemRegex = Regex("""<item[^>]*id\s*=\s*"([^"]+)"[^>]*href\s*=\s*"([^"]+)"[^>]*/?>""")

        for (match in itemRegex.findAll(content)) {
            val id = match.groupValues[1]
            val href = match.groupValues[2]
            val fullPath = if (opfDir.isNotEmpty()) "$opfDir/$href" else href
            manifest[id] = fullPath
        }

        return manifest
    }

    /**
     * تحليل ترتيب spine من OPF
     * @return قائمة معرفات الفصول بالترتيب
     */
    private fun parseSpine(content: String): List<String> {
        val spineItems = mutableListOf<String>()
        val itemrefRegex = Regex("""<itemref[^>]*idref\s*=\s*"([^"]+)"[^>]*/?>""")

        for (match in itemrefRegex.findAll(content)) {
            spineItems.add(match.groupValues[1])
        }

        return spineItems
    }

    /**
     * تحليل ملف toc.ncx لاستخراج جدول المحتويات
     * @param tocPath مسار ملف NCX داخل ZIP
     * @param manifest قائمة manifest
     * @return قائمة عناصر الفهرس
     */
    private fun parseTocNcx(tocPath: String?, manifest: Map<String, String>): List<TocItem> {
        if (tocPath == null) return emptyList()

        val zip = zipFile ?: return emptyList()

        // محاولة العثور على ملف NCX
        val ncxEntry = zip.getEntry(tocPath) ?: run {
            // البحث عن ملف NCX في جميع الإدخالات
            zip.entries().toList().find { it.name.endsWith(".ncx") }
        } ?: return emptyList()

        val ncxContent = zip.getInputStream(ncxEntry).bufferedReader().use { it.readText() }

        val tocItems = mutableListOf<TocItem>()
        val navPointRegex = Regex("""<navPoint[^>]*playOrder\s*=\s*"(\d+)"[^>]*>.*?<text>(.*?)</text>.*?<content[^>]*src\s*=\s*"([^"]*)"[^>]*/?>.*?</navPoint>""", RegexOption.DOT_MATCHES_ALL)

        for (match in navPointRegex.findAll(ncxContent)) {
            val title = stripHtml(match.groupValues[2]).trim()
            val src = match.groupValues[3].substringBefore("#")
            val playOrder = match.groupValues[1].toIntOrNull() ?: 0

            // البحث عن فهرس الصفحة من manifest
            val pageIndex = findPageIndexForSrc(src, manifest)

            tocItems.add(
                TocItem(
                    title = title.ifEmpty { "فصل $playOrder" },
                    pageIndex = pageIndex,
                    level = 0
                )
            )
        }

        // إذا لم يتم العثور على navPoint، محاولة تحليل nav.xhtml
        if (tocItems.isEmpty()) {
            return parseNavXhtml(manifest)
        }

        return tocItems
    }

    /**
     * تحليل ملف nav.xhtml لاستخراج جدول المحتويات (EPUB 3)
     * @param manifest قائمة manifest
     * @return قائمة عناصر الفهرس
     */
    private fun parseNavXhtml(manifest: Map<String, String>): List<TocItem> {
        val zip = zipFile ?: return emptyList()

        // البحث عن ملف nav.xhtml
        val navEntry = zip.entries().toList().find {
            it.name.endsWith("nav.xhtml") || it.name.endsWith("nav.html")
        } ?: return emptyList()

        val navContent = zip.getInputStream(navEntry).bufferedReader().use { it.readText() }

        val tocItems = mutableListOf<TocItem>()
        val linkRegex = Regex("""<a[^>]*href\s*=\s*"([^"]*)"[^>]*>(.*?)</a>""", RegexOption.DOT_MATCHES_ALL)

        var index = 0
        for (match in linkRegex.findAll(navContent)) {
            val src = match.groupValues[1].substringBefore("#")
            val title = stripHtml(match.groupValues[2]).trim()

            if (title.isNotEmpty() && src.isNotEmpty() && !src.startsWith("http")) {
                val pageIndex = findPageIndexForSrc(src, manifest)
                tocItems.add(
                    TocItem(
                        title = title,
                        pageIndex = pageIndex,
                        level = 0
                    )
                )
                index++
            }
        }

        return tocItems
    }

    /**
     * البحث عن فهرس الصفحة المقابل لملف مصدر
     */
    private fun findPageIndexForSrc(src: String, manifest: Map<String, String>): Int {
        // البحث في manifest عن المعرف المقابل
        for ((_, path) in manifest) {
            if (path.endsWith(src) || src.endsWith(path.substringAfterLast("/"))) {
                // البحث عن فهرس هذا الفصل في قائمة spine
                return chapters.indexOfFirst { it.src == path }.coerceAtLeast(0)
            }
        }
        return 0
    }

    /**
     * تحميل الفصول بترتيب Spine
     * @param spine قائمة معرفات الفصول بالترتيب
     * @param manifest خريطة manifest
     * @return قائمة الفصول
     */
    private fun loadChapters(spine: List<String>, manifest: Map<String, String>): List<EpubChapter> {
        val zip = zipFile ?: return emptyList()
        val loadedChapters = mutableListOf<EpubChapter>()

        for (itemId in spine) {
            val path = manifest[itemId] ?: continue

            try {
                val entry = zip.getEntry(path) ?: continue
                val htmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val plainText = stripHtml(htmlContent)

                if (plainText.isNotBlank()) {
                    loadedChapters.add(
                        EpubChapter(
                            id = itemId,
                            src = path,
                            title = extractChapterTitle(htmlContent) ?: "فصل ${loadedChapters.size + 1}",
                            text = plainText
                        )
                    )
                }
            } catch (_: Exception) {
                // تجاهل الفصول التي لا يمكن قراءتها
            }
        }

        // إذا لم يتم تحميل أي فصول، محاولة تحميل جميع ملفات XHTML
        if (loadedChapters.isEmpty()) {
            loadAllXhtmlFiles(loadedChapters)
        }

        return loadedChapters
    }

    /**
     * تحميل جميع ملفات XHTML من ZIP كحل بديل
     */
    private fun loadAllXhtmlFiles(loadedChapters: MutableList<EpubChapter>) {
        val zip = zipFile ?: return

        val xhtmlEntries = zip.entries().toList()
            .filter { it.name.endsWith(".xhtml") || it.name.endsWith(".html") || it.name.endsWith(".htm") }
            .sortedBy { it.name }

        for (entry in xhtmlEntries) {
            try {
                val htmlContent = zip.getInputStream(entry).bufferedReader().use { it.readText() }
                val plainText = stripHtml(htmlContent)

                if (plainText.isNotBlank()) {
                    loadedChapters.add(
                        EpubChapter(
                            id = entry.name,
                            src = entry.name,
                            title = extractChapterTitle(htmlContent) ?: "فصل ${loadedChapters.size + 1}",
                            text = plainText
                        )
                    )
                }
            } catch (_: Exception) {
                // تجاهل الملفات التي لا يمكن قراءتها
            }
        }
    }

    /**
     * استخراج عنوان الفصل من محتوى HTML
     */
    private fun extractChapterTitle(html: String): String? {
        // البحث عن أول عنوان h1-h6
        val headingRegex = Regex("""<h[1-6][^>]*>(.*?)</h[1-6]>""", RegexOption.DOT_MATCHES_ALL)
        val match = headingRegex.find(html) ?: return null
        val title = stripHtml(match.groupValues[1]).trim()
        return title.ifEmpty { null }
    }

    /**
     * إزالة علامات HTML وفك تشفير الكيانات
     * @param html نص HTML
     * @return نص عادي
     */
    private fun stripHtml(html: String): String {
        return html
            // إزالة علامات البرمجة النصية والأنماط
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            // تحويل بعض العلامات إلى أسطر جديدة
            .replace(Regex("<br\\s*/?>"), "\n")
            .replace(Regex("</p>"), "\n\n")
            .replace(Regex("</div>"), "\n")
            .replace(Regex("</h[1-6]>"), "\n\n")
            .replace(Regex("<li[^>]*>"), "• ")
            // إزالة جميع علامات HTML المتبقية
            .replace(Regex("<[^>]*>"), "")
            // فك تشفير كيانات HTML
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace(Regex("&#(\\d+);")) { match ->
                val code = match.groupValues[1].toIntOrNull() ?: 0
                code.toChar().toString()
            }
            .replace(Regex("&#x([0-9a-fA-F]+);")) { match ->
                val code = match.groupValues[1].toIntOrNull(16) ?: 0
                code.toChar().toString()
            }
            // تنظيف المسافات الزائدة
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()
    }

    /**
     * استخراج محتوى علامة XML
     */
    private fun extractXmlTag(content: String, tagName: String): String? {
        val regex = Regex("""<$tagName[^>]*>(.*?)</$tagName>""", RegexOption.DOT_MATCHES_ALL)
        return regex.find(content)?.groupValues?.get(1)?.trim()
    }

    /**
     * استخراج قيمة سمة XML
     */
    private fun extractXmlAttribute(content: String, attrName: String, attrValue: String): String? {
        val regex = Regex("""$attrName\s*=\s*"$attrValue"[^>]*content\s*=\s*"([^"]+)"""")
        return regex.find(content)?.groupValues?.get(1)
    }

    /**
     * استخراج قيمة سمة من علامة XML
     */
    private fun extractXmlTagAttribute(content: String, tagName: String, attrName: String): String? {
        val regex = Regex("""<$tagName[^>]*$attrName\s*=\s*"([^"]+)"[^>]*>""")
        return regex.find(content)?.groupValues?.get(1)
    }

    /**
     * الحصول على مسار إدخال manifest
     */
    private fun manifestEntryPath(content: String, itemId: String, opfDir: String): String? {
        val itemRegex = Regex("""<item[^>]*id\s*=\s*"${Regex.escape(itemId)}"[^>]*href\s*=\s*"([^"]+)"[^>]*/?>""")
        val href = itemRegex.find(content)?.groupValues?.get(1) ?: return null
        return if (opfDir.isNotEmpty()) "$opfDir/$href" else href
    }

    /**
     * بيانات فصل EPUB
     */
    private data class EpubChapter(
        /** معرف الفصل */
        val id: String,
        /** مسار ملف المصدر */
        val src: String,
        /** عنوان الفصل */
        val title: String,
        /** النص المستخرج */
        val text: String
    )
}
