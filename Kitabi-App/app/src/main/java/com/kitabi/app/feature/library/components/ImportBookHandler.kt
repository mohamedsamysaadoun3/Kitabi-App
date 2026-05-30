package com.kitabi.app.feature.library.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.BookFormat
import com.kitabi.app.domain.model.BookSource
import com.kitabi.app.domain.model.Category
import com.kitabi.app.domain.model.DownloadState
import com.kitabi.app.domain.model.Language
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.provider.book.BookProviderException
import com.kitabi.app.provider.book.ContentProviderFactory
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * معالج استيراد الكتب
 * يتعامل مع استيراد ملفات الكتب من جهاز المستخدم
 * ينسخ الملف إلى تخزين التطبيق ويستخرج البيانات الوصفية
 * يدعم صيغ PDF و EPUB و TXT
 */
@Singleton
class ImportBookHandler @Inject constructor(
    /** مصنع مزودي المحتوى */
    private val contentProviderFactory: ContentProviderFactory,
    /** مستودع الكتب */
    private val bookRepository: BookRepository
) {
    companion object {
        private const val TAG = "ImportBookHandler"
        /** مجلد تخزين الكتب */
        private const val BOOKS_DIR_NAME = "kitabi_books"
    }

    /**
     * استيراد كتاب من مسار Uri
     * ينسخ الملف إلى تخزين التطبيق ويستخرج البيانات الوصفية ويحفظه في قاعدة البيانات
     * @param uri مسار الملف المراد استيراده
     * @param context سياق التطبيق
     * @return الكتاب المستورد أو خطأ
     */
    suspend fun importFromUri(uri: Uri, context: Context): Result<Book> {
        return try {
            Log.d(TAG, "بدء استيراد كتاب من: $uri")

            // 1. نسخ الملف إلى تخزين التطبيق
            val inputFile = copyToAppStorage(uri, context)
            Log.d(TAG, "تم نسخ الملف إلى: ${inputFile.absolutePath}")

            // 2. تحديد الصيغة والحصول على المزود المناسب
            val provider = contentProviderFactory.getProvider(uri)

            // 3. فتح الكتاب واستخراج البيانات
            val bookContent = try {
                provider.openBook(uri)
            } catch (e: BookProviderException) {
                Log.w(TAG, "فشل فتح الكتاب بالمزود: ${e.message}")
                // محاولة الفتح من الملف المنسوخ
                val fileUri = Uri.fromFile(inputFile)
                provider.openBook(fileUri)
            }

            // 4. استخراج البيانات الوصفية
            val metadata = bookContent.metadata
            Log.d(TAG, "البيانات الوصفية: العنوان=${metadata.title}, المؤلف=${metadata.author}")

            // 5. تحديد صيغة الكتاب
            val format = contentProviderFactory.detectFormat(uri)

            // 6. تحديد اللغة
            val language = try {
                when (metadata.language) {
                    "ar" -> Language.ARABIC
                    "en" -> Language.ENGLISH
                    "fr" -> Language.FRENCH
                    "de" -> Language.GERMAN
                    "tr" -> Language.TURKISH
                    "fa" -> Language.PERSIAN
                    "ur" -> Language.URDU
                    else -> Language.ARABIC
                }
            } catch (_: Exception) {
                Language.ARABIC
            }

            // 7. إنشاء كيان الكتاب
            val book = Book(
                id = UUID.randomUUID().toString(),
                title = metadata.title ?: inputFile.nameWithoutExtension,
                author = metadata.author ?: "غير معروف",
                source = BookSource.LOCAL,
                format = format,
                filePath = inputFile.absolutePath,
                coverPath = metadata.coverPath ?: "",
                pageCount = bookContent.pageCount,
                fileSize = inputFile.length(),
                language = language,
                description = metadata.description ?: "",
                publisher = metadata.publisher ?: "",
                year = metadata.year?.toIntOrNull() ?: 0,
                isbn = metadata.isbn ?: "",
                isDownloaded = true,
                downloadState = DownloadState.DOWNLOADED,
                category = Category.GENERAL,
                addedAt = System.currentTimeMillis()
            )

            // 8. حفظ في قاعدة البيانات
            val savedBook = bookRepository.addBook(book)
            Log.d(TAG, "تم استيراد الكتاب بنجاح: ${savedBook.title}")

            // 9. إغلاق الكتاب
            try {
                provider.closeBook()
            } catch (_: Exception) {
                // تجاهل أخطاء الإغلاق
            }

            Result.success(savedBook)
        } catch (e: BookProviderException) {
            Log.e(TAG, "خطأ في مزود المحتوى: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "فشل استيراد الكتاب: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * استيراد كتاب من مسار ملف محلي
     * @param filePath مسار الملف على الجهاز
     * @param context سياق التطبيق
     * @return الكتاب المستورد أو خطأ
     */
    suspend fun importFromPath(filePath: String, context: Context): Result<Book> {
        val file = File(filePath)
        if (!file.exists()) {
            return Result.failure(Exception("الملف غير موجود: $filePath"))
        }

        val uri = Uri.fromFile(file)
        return importFromUri(uri, context)
    }

    /**
     * نسخ الملف من Uri إلى تخزين التطبيق
     * @param uri مسار الملف المصدر
     * @param context سياق التطبيق
     * @return الملف المنسوخ
     */
    private fun copyToAppStorage(uri: Uri, context: Context): File {
        // إنشاء مجلد الكتب
        val booksDir = File(
            context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),
            BOOKS_DIR_NAME
        )
        booksDir.mkdirs()

        // الحصول على اسم الملف
        val fileName = getFileName(uri, context)
        var outputFile = File(booksDir, fileName)

        // تجنب الكتابة فوق ملف موجود
        if (outputFile.exists()) {
            val nameWithoutExt = fileName.substringBeforeLast(".")
            val extension = fileName.substringAfterLast(".", "epub")
            var counter = 1
            while (outputFile.exists()) {
                outputFile = File(booksDir, "${nameWithoutExt}_$counter.$extension")
                counter++
            }
        }

        // نسخ محتوى الملف
        context.contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw BookProviderException("فشل قراءة الملف من المسار المحدد")

        return outputFile
    }

    /**
     * الحصول على اسم الملف من Uri
     * @param uri مسار الملف
     * @param context سياق التطبيق
     * @return اسم الملف
     */
    private fun getFileName(uri: Uri, context: Context): String {
        // محاولة الحصول على اسم الملف من ContentResolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    if (name.isNotEmpty()) return name
                }
            }
        }

        // محاولة الحصول على اسم الملف من المسار
        uri.lastPathSegment?.let { path ->
            val name = path.substringAfterLast("/")
            if (name.isNotEmpty()) return name
        }

        // اسم افتراضي
        return "كتاب_${System.currentTimeMillis()}.epub"
    }

    /**
     * التحقق من إمكانية استيراد الملف
     * @param uri مسار الملف
     * @return هل يمكن استيراد الملف
     */
    fun canImport(uri: Uri): Boolean {
        val format = contentProviderFactory.detectFormat(uri)
        return contentProviderFactory.isFormatSupported(format)
    }

    /**
     * الحصول على صيغة الملف
     * @param uri مسار الملف
     * @return صيغة الكتاب
     */
    fun getFormat(uri: Uri): BookFormat {
        return contentProviderFactory.detectFormat(uri)
    }
}
