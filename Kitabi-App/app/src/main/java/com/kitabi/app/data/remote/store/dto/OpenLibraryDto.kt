package com.kitabi.app.data.remote.store.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * استجابة بحث Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibrarySearchResponse(
    @Json(name = "numFound") val numFound: Int = 0,
    @Json(name = "start") val start: Int = 0,
    @Json(name = "docs") val docs: List<OpenLibraryDoc> = emptyList()
)

/**
 * مستند كتاب في Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryDoc(
    @Json(name = "key") val key: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "author_name") val authorName: List<String>? = null,
    @Json(name = "cover_i") val coverId: Int? = null,
    @Json(name = "cover_edition_key") val coverEditionKey: String? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "publisher") val publisher: List<String>? = null,
    @Json(name = "language") val language: List<String>? = null,
    @Json(name = "subject") val subject: List<String>? = null,
    @Json(name = "isbn") val isbn: List<String>? = null,
    @Json(name = "edition_count") val editionCount: Int = 0,
    @Json(name = "ratings_average") val ratingsAverage: Double? = null,
    @Json(name = "ratings_count") val ratingsCount: Int? = null,
    @Json(name = "number_of_pages_median") val pagesMedian: Int? = null,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "seed") val seed: List<String>? = null
)

/**
 * تفاصيل عمل في Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryWork(
    @Json(name = "key") val key: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: Description? = null,
    @Json(name = "covers") val covers: List<Int>? = null,
    @Json(name = "subjects") val subjects: List<String>? = null,
    @Json(name = "authors") val authors: List<OpenLibraryAuthorRef>? = null,
    @Json(name = "first_publish_date") val firstPublishDate: String? = null
)

/**
 * مرجع مؤلف في Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryAuthorRef(
    @Json(name = "author") val author: OpenLibraryAuthorKey? = null
)

/**
 * مفتاح المؤلف
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryAuthorKey(
    @Json(name = "key") val key: String = ""
)

/**
 * وصف العمل (يمكن أن يكون نص أو كائن)
 */
@JsonClass(generateAdapter = true)
data class Description(
    @Json(name = "value") val value: String? = null,
    @Json(name = "type") val type: String? = null
)

/**
 * استجابة كتب حسب الموضوع في Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibrarySubjectResponse(
    @Json(name = "name") val name: String = "",
    @Json(name = "work_count") val workCount: Int = 0,
    @Json(name = "works") val works: List<OpenLibraryWorkSummary> = emptyList()
)

/**
 * ملخص عمل في Open Library
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryWorkSummary(
    @Json(name = "key") val key: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "cover_id") val coverId: Int? = null,
    @Json(name = "authors") val authors: List<OpenLibraryWorkAuthor>? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "subject") val subject: List<String>? = null
)

/**
 * مؤلف ملخص العمل
 */
@JsonClass(generateAdapter = true)
data class OpenLibraryWorkAuthor(
    @Json(name = "name") val name: String = "",
    @Json(name = "key") val key: String = ""
)
