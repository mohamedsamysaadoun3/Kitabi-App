package com.kitabi.app.data.mapper

import com.kitabi.app.data.remote.store.dto.GutenbergBook
import com.kitabi.app.data.remote.store.dto.GoogleBookVolume
import com.kitabi.app.data.remote.store.dto.OpenLibraryDoc
import com.kitabi.app.data.remote.store.dto.OpenLibraryWorkSummary
import com.kitabi.app.domain.model.OnlineBook

/**
 * محول بيانات المتجر
 * يحول بيانات API إلى نموذج الكتاب الإلكتروني
 */
object StoreMapper {

    /**
     * تحويل مستند Open Library إلى نموذج الكتاب الإلكتروني
     * @param doc مستند Open Library
     * @return نموذج الكتاب الإلكتروني
     */
    fun openLibraryDocToOnlineBook(doc: OpenLibraryDoc): OnlineBook {
        val workId = doc.key.removePrefix("/works/")
        val coverUrl = doc.coverId?.let {
            "https://covers.openlibrary.org/b/id/$it-M.jpg"
        } ?: ""

        return OnlineBook(
            id = "ol_$workId",
            title = doc.title,
            author = doc.authorName?.firstOrNull() ?: "غير معروف",
            coverUrl = coverUrl,
            description = "",
            rating = doc.ratingsAverage?.toFloat() ?: 0f,
            ratingsCount = doc.ratingsCount ?: 0,
            pageCount = doc.pagesMedian ?: 0,
            category = mapSubjectToCategory(doc.subject?.firstOrNull()),
            language = doc.language?.firstOrNull() ?: "ara",
            isTranslated = false,
            isPublicDomain = false,
            publisher = doc.publisher?.firstOrNull() ?: "",
            publishYear = doc.firstPublishYear ?: 0,
            isbn = doc.isbn?.firstOrNull() ?: "",
            source = "openlibrary",
            sourceId = workId,
            previewUrl = "https://openlibrary.org$workId",
            infoUrl = "https://openlibrary.org$workId",
            subjects = doc.subject?.take(5) ?: emptyList()
        )
    }

    /**
     * تحويل ملخص عمل Open Library إلى نموذج الكتاب الإلكتروني
     * @param work ملخص العمل
     * @return نموذج الكتاب الإلكتروني
     */
    fun openLibraryWorkToOnlineBook(work: OpenLibraryWorkSummary): OnlineBook {
        val workId = work.key.removePrefix("/works/")
        val coverUrl = work.coverId?.let {
            "https://covers.openlibrary.org/b/id/$it-M.jpg"
        } ?: ""

        return OnlineBook(
            id = "ol_$workId",
            title = work.title,
            author = work.authors?.firstOrNull()?.name ?: "غير معروف",
            coverUrl = coverUrl,
            source = "openlibrary",
            sourceId = workId,
            publishYear = work.firstPublishYear ?: 0,
            subjects = work.subject?.take(5) ?: emptyList(),
            category = mapSubjectToCategory(work.subject?.firstOrNull())
        )
    }

    /**
     * تحويل كتاب Google Books إلى نموذج الكتاب الإلكتروني
     * @param volume كتاب Google Books
     * @return نموذج الكتاب الإلكتروني
     */
    fun googleBookToOnlineBook(volume: GoogleBookVolume): OnlineBook {
        val info = volume.volumeInfo
        val coverUrl = info.imageLinks?.let { images ->
            // اختيار أفضل حجم متاح
            images.extraLarge ?: images.large ?: images.medium
                ?: images.small ?: images.thumbnail ?: images.smallThumbnail ?: ""
        }?.replace("http://", "https://") ?: ""

        val isArabic = info.language == "ar"
        val isPublicDomain = volume.accessInfo.publicDomain

        return OnlineBook(
            id = "gb_${volume.id}",
            title = info.title,
            author = info.authors?.firstOrNull() ?: "غير معروف",
            coverUrl = coverUrl,
            description = info.description ?: "",
            rating = info.averageRating.toFloat(),
            ratingsCount = info.ratingsCount,
            pageCount = info.pageCount,
            category = mapGoogleCategoryToCategory(info.categories?.firstOrNull()),
            language = info.language,
            isTranslated = isArabic && info.authors?.any { author ->
                // تقدير بسيط: إذا كان الكتاب بالعربية والمؤلف ليس عربياً
                !isArabicName(author)
            } == true,
            isPublicDomain = isPublicDomain,
            publisher = info.publisher ?: "",
            publishYear = info.publishedDate?.take(4)?.toIntOrNull() ?: 0,
            isbn = info.industryIdentifiers?.firstOrNull()?.identifier ?: "",
            source = "google",
            sourceId = volume.id,
            previewUrl = info.previewLink ?: "",
            infoUrl = info.infoLink ?: "",
            downloadUrl = volume.accessInfo.pdf?.downloadLink
                ?: volume.accessInfo.epub?.downloadLink ?: "",
            subjects = info.categories ?: emptyList(),
            copyright = if (isPublicDomain) "ملكية عامة" else ""
        )
    }

    /**
     * تحويل كتاب Gutenberg إلى نموذج الكتاب الإلكتروني
     * @param book كتاب Gutenberg
     * @return نموذج الكتاب الإلكتروني
     */
    fun gutenbergBookToOnlineBook(book: GutenbergBook): OnlineBook {
        val coverUrl = book.formats["image/jpeg"] ?: ""
        val hasTranslator = book.translators.isNotEmpty()

        return OnlineBook(
            id = "gu_${book.id}",
            title = book.title,
            author = book.authors.firstOrNull()?.name ?: "غير معروف",
            coverUrl = coverUrl,
            description = "",
            rating = 0f,
            pageCount = 0,
            category = mapGutenbergSubjectToCategory(book.subjects.firstOrNull()),
            language = book.languages.firstOrNull() ?: "ar",
            isTranslated = hasTranslator,
            isPublicDomain = !book.copyright,
            publisher = "",
            publishYear = book.authors.firstOrNull()?.birthYear ?: 0,
            source = "gutenberg",
            sourceId = book.id.toString(),
            downloadUrl = book.formats["text/html"] ?: book.formats["text/plain"] ?: "",
            downloadFormats = book.formats,
            subjects = book.subjects.take(5),
            downloadCount = book.downloadCount,
            copyright = if (!book.copyright) "ملكية عامة - حقوق الطبع والنشر محفوظة" else ""
        )
    }

    /**
     * تحويل موضوع Open Library إلى تصنيف الكتاب (عام - يمكن استدعاؤه من خارج الكائن)
     */
    fun mapSubjectToCategoryPublic(subject: String?): String = mapSubjectToCategory(subject)

    /**
     * تحويل موضوع Open Library إلى تصنيف الكتاب
     */
    private fun mapSubjectToCategory(subject: String?): String {
        if (subject == null) return "GENERAL"
        val lower = subject.lowercase()
        return when {
            lower.contains("fiction") || lower.contains("novel") -> "LITERATURE"
            lower.contains("poem") || lower.contains("poetry") -> "POETRY"
            lower.contains("history") -> "HISTORY"
            lower.contains("philosophy") -> "PHILOSOPHY"
            lower.contains("psychology") -> "PSYCHOLOGY"
            lower.contains("religion") || lower.contains("islam") -> "RELIGION"
            lower.contains("science") -> "SCIENCE"
            lower.contains("technology") || lower.contains("computer") -> "TECHNOLOGY"
            lower.contains("business") || lower.contains("economics") -> "BUSINESS"
            lower.contains("education") -> "EDUCATION"
            lower.contains("children") -> "CHILDREN"
            lower.contains("biography") -> "BIOGRAPHY"
            else -> "GENERAL"
        }
    }

    /**
     * تحويل تصنيف Google Books إلى تصنيف الكتاب
     */
    private fun mapGoogleCategoryToCategory(category: String?): String {
        if (category == null) return "GENERAL"
        val lower = category.lowercase()
        return when {
            lower.contains("fiction") -> "LITERATURE"
            lower.contains("poetry") -> "POETRY"
            lower.contains("history") -> "HISTORY"
            lower.contains("philosophy") -> "PHILOSOPHY"
            lower.contains("psychology") -> "PSYCHOLOGY"
            lower.contains("religion") -> "RELIGION"
            lower.contains("science") -> "SCIENCE"
            lower.contains("technology") || lower.contains("computers") -> "TECHNOLOGY"
            lower.contains("business") || lower.contains("economic") -> "BUSINESS"
            lower.contains("education") -> "EDUCATION"
            lower.contains("children") || lower.contains("juvenile") -> "CHILDREN"
            lower.contains("biography") -> "BIOGRAPHY"
            lower.contains("art") -> "ARTS"
            lower.contains("health") -> "HEALTH"
            lower.contains("travel") -> "TRAVEL"
            lower.contains("cooking") || lower.contains("cook") -> "COOKING"
            lower.contains("law") -> "LAW"
            lower.contains("political") -> "POLITICS"
            lower.contains("language") || lower.contains("foreign") -> "LANGUAGES"
            else -> "GENERAL"
        }
    }

    /**
     * تحويل موضوع Gutenberg إلى تصنيف الكتاب
     */
    private fun mapGutenbergSubjectToCategory(subject: String?): String {
        return mapSubjectToCategory(subject)
    }

    /**
     * تقدير بسيط إذا كان الاسم عربياً
     */
    private fun isArabicName(name: String): Boolean {
        return name.any { it.code in 0x0600..0x06FF }
    }
}
