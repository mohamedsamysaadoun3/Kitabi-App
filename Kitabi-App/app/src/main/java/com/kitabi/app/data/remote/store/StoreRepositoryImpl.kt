package com.kitabi.app.data.remote.store

import com.kitabi.app.data.mapper.StoreMapper
import com.kitabi.app.domain.model.OnlineBook
import com.kitabi.app.domain.repository.OnlineStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع المتجر الإلكتروني
 * يجمع البيانات من مصادر متعددة (Open Library, Google Books, Gutenberg)
 */
@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val openLibraryApi: OpenLibraryApiService,
    private val googleBooksApi: GoogleBooksApiService,
    private val gutenbergApi: GutenbergApiService
) : OnlineStoreRepository {

    override suspend fun searchBooks(query: String, page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()

            // البحث في Open Library
            try {
                val olResponse = openLibraryApi.searchBooks(
                    query = query,
                    page = page
                )
                results.addAll(olResponse.docs.map { StoreMapper.openLibraryDocToOnlineBook(it) })
            } catch (_: Exception) { /* تجاهل أخطاء المصدر الفردي */ }

            // البحث في Google Books
            try {
                val gbResponse = googleBooksApi.searchBooks(
                    query = query,
                    startIndex = (page - 1) * 20
                )
                gbResponse.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { /* تجاهل أخطاء المصدر الفردي */ }

            // البحث في Gutenberg
            try {
                val guResponse = gutenbergApi.getBooks(
                    search = query,
                    page = page
                )
                results.addAll(guResponse.results.map { StoreMapper.gutenbergBookToOnlineBook(it) })
            } catch (_: Exception) { /* تجاهل أخطاء المصدر الفردي */ }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedBooks(): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()

            // كتب مميزة من Open Library - موضوعات متنوعة
            try {
                val subjects = listOf("fiction", "history", "philosophy", "poetry")
                for (subject in subjects) {
                    val response = openLibraryApi.getBooksBySubject(subject, limit = 5)
                    results.addAll(response.works.map { StoreMapper.openLibraryWorkToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            // كتب عربية مميزة من Google Books
            try {
                val response = googleBooksApi.searchBooks(
                    query = "bestseller arabic",
                    orderBy = "relevance",
                    maxResults = 10
                )
                response.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            Result.success(results.distinctBy { it.title })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBooksByCategory(category: String, page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()

            // البحث حسب التصنيف في Open Library
            try {
                val olResponse = openLibraryApi.searchBooks(
                    query = category,
                    subject = category,
                    page = page
                )
                results.addAll(olResponse.docs.map { StoreMapper.openLibraryDocToOnlineBook(it) })
            } catch (_: Exception) { }

            // البحث حسب التصنيف في Google Books
            try {
                val gbResponse = googleBooksApi.searchBooks(
                    query = "subject:$category",
                    startIndex = (page - 1) * 20
                )
                gbResponse.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMostReadBooks(page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()
            try {
                val response = openLibraryApi.searchBooks(
                    query = "popular arabic",
                    page = page
                )
                results.addAll(response.docs.map { StoreMapper.openLibraryDocToOnlineBook(it) })
            } catch (_: Exception) { }

            try {
                val response = googleBooksApi.searchBooks(
                    query = "arabic popular",
                    orderBy = "relevance",
                    startIndex = (page - 1) * 20
                )
                response.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNewBooks(page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()
            try {
                val response = googleBooksApi.searchBooks(
                    query = "arabic new",
                    orderBy = "newest",
                    startIndex = (page - 1) * 20
                )
                response.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            try {
                val response = openLibraryApi.searchBooks(
                    query = "arabic",
                    page = page
                )
                results.addAll(response.docs.map { StoreMapper.openLibraryDocToOnlineBook(it) })
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTranslatedBooks(page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()
            try {
                val response = googleBooksApi.searchBooks(
                    query = "arabic translated fiction",
                    startIndex = (page - 1) * 20
                )
                response.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            // كتب Gutenberg المترجمة
            try {
                val response = gutenbergApi.getBooks(page = page)
                results.addAll(
                    response.results
                        .filter { it.translators.isNotEmpty() }
                        .map { StoreMapper.gutenbergBookToOnlineBook(it) }
                )
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArabicOriginalBooks(page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()
            try {
                val response = openLibraryApi.searchBooks(
                    query = "arabic literature",
                    language = "ara",
                    page = page
                )
                results.addAll(response.docs.map { StoreMapper.openLibraryDocToOnlineBook(it) })
            } catch (_: Exception) { }

            try {
                val response = googleBooksApi.searchBooks(
                    query = "أدب عربي رواية",
                    langRestrict = "ar",
                    startIndex = (page - 1) * 20
                )
                response.items?.let { items ->
                    results.addAll(items.map { StoreMapper.googleBookToOnlineBook(it) })
                }
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPublicDomainBooks(page: Int): Result<List<OnlineBook>> {
        return try {
            val results = mutableListOf<OnlineBook>()

            // Gutenberg هو المصدر الرئيسي للكتب ملكية عامة
            try {
                val response = gutenbergApi.getBooks(
                    languages = "ar",
                    page = page
                )
                results.addAll(response.results.map { StoreMapper.gutenbergBookToOnlineBook(it) })
            } catch (_: Exception) { }

            // كتب ملكية عامة من Google Books
            try {
                val response = googleBooksApi.searchBooks(
                    query = "arabic public domain",
                    startIndex = (page - 1) * 20
                )
                response.items?.let { items ->
                    results.addAll(
                        items
                            .filter { it.accessInfo.publicDomain }
                            .map { StoreMapper.googleBookToOnlineBook(it) }
                    )
                }
            } catch (_: Exception) { }

            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBookDetail(sourceId: String, source: String): Result<OnlineBook> {
        return try {
            val book = when (source) {
                "openlibrary" -> {
                    val work = openLibraryApi.getBookDetail(sourceId)
                    OnlineBook(
                        id = "ol_$sourceId",
                        title = work.title,
                        author = "غير معروف",
                        source = "openlibrary",
                        sourceId = sourceId,
                        coverUrl = work.covers?.firstOrNull()?.let {
                            "https://covers.openlibrary.org/b/id/$it-L.jpg"
                        } ?: "",
                        description = work.description?.value ?: "",
                        subjects = work.subjects?.take(5) ?: emptyList(),
                        category = StoreMapper.mapSubjectToCategoryPublic(work.subjects?.firstOrNull())
                    )
                }
                "google" -> {
                    val volume = googleBooksApi.getBookDetail(sourceId)
                    StoreMapper.googleBookToOnlineBook(volume)
                }
                else -> {
                    return Result.failure(Exception("مصدر غير مدعوم: $source"))
                }
            }
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSuggestedBooks(): Flow<List<OnlineBook>> {
        return flowOf(emptyList())
    }
}
