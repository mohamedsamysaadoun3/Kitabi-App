package com.kitabi.app.data.mapper

import com.kitabi.app.data.local.entity.BookEntity
import com.kitabi.app.domain.model.Book
import com.kitabi.app.domain.model.BookFormat
import com.kitabi.app.domain.model.BookSource
import com.kitabi.app.domain.model.Category
import com.kitabi.app.domain.model.DownloadState
import com.kitabi.app.domain.model.Language

/**
 * محول بيانات الكتاب
 * يحول بين كيان قاعدة البيانات ونموذج النطاق
 */
object BookMapper {

    /**
     * تحويل كيان قاعدة البيانات إلى نموذج النطاق
     * @param entity كيان قاعدة البيانات
     * @return نموذج الكتاب
     */
    fun entityToDomain(entity: BookEntity): Book {
        return Book(
            id = entity.id,
            title = entity.title,
            author = entity.author,
            source = try { BookSource.valueOf(entity.source) } catch (_: Exception) { BookSource.LOCAL },
            format = try { BookFormat.valueOf(entity.format) } catch (_: Exception) { BookFormat.PDF },
            coverPath = entity.coverPath,
            filePath = entity.filePath,
            onlineId = entity.onlineId,
            downloadUrl = entity.downloadUrl,
            downloadProgress = entity.downloadProgress,
            fileSize = entity.fileSize,
            pageCount = entity.pageCount,
            currentPage = entity.currentPage,
            lastReadAt = entity.lastReadAt,
            isDownloaded = entity.isDownloaded,
            category = try { Category.valueOf(entity.category) } catch (_: Exception) { Category.GENERAL },
            rating = entity.rating,
            description = entity.description,
            language = try { Language.valueOf(entity.language) } catch (_: Exception) { Language.ARABIC },
            publisher = entity.publisher,
            year = entity.year,
            isbn = entity.isbn,
            copyright = entity.copyright,
            addedAt = entity.addedAt,
            downloadState = try { DownloadState.valueOf(entity.downloadState) } catch (_: Exception) { DownloadState.NOT_DOWNLOADED }
        )
    }

    /**
     * تحويل نموذج النطاق إلى كيان قاعدة البيانات
     * @param domain نموذج الكتاب
     * @return كيان قاعدة البيانات
     */
    fun domainToEntity(domain: Book): BookEntity {
        return BookEntity(
            id = domain.id,
            title = domain.title,
            author = domain.author,
            source = domain.source.name,
            format = domain.format.name,
            coverPath = domain.coverPath,
            coverUrl = if (domain.source == BookSource.ONLINE) domain.coverPath else "",
            filePath = domain.filePath,
            onlineId = domain.onlineId,
            downloadUrl = domain.downloadUrl,
            downloadProgress = domain.downloadProgress,
            fileSize = domain.fileSize,
            pageCount = domain.pageCount,
            currentPage = domain.currentPage,
            lastReadAt = domain.lastReadAt,
            isDownloaded = domain.isDownloaded,
            category = domain.category.name,
            rating = domain.rating,
            description = domain.description,
            language = domain.language.name,
            publisher = domain.publisher,
            year = domain.year,
            isbn = domain.isbn,
            copyright = domain.copyright,
            addedAt = domain.addedAt,
            downloadState = domain.downloadState.name
        )
    }

    /**
     * تحويل قائمة كيانات إلى نماذج نطاق
     * @param entities قائمة كيانات قاعدة البيانات
     * @return قائمة نماذج الكتب
     */
    fun entityListToDomainList(entities: List<BookEntity>): List<Book> {
        return entities.map { entityToDomain(it) }
    }
}
