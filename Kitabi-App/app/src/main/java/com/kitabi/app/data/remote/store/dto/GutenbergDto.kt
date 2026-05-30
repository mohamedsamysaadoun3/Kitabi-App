package com.kitabi.app.data.remote.store.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * استجابة بحث Project Gutenberg
 */
@JsonClass(generateAdapter = true)
data class GutenbergResponse(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "next") val next: String? = null,
    @Json(name = "previous") val previous: String? = null,
    @Json(name = "results") val results: List<GutenbergBook> = emptyList()
)

/**
 * كتاب من Project Gutenberg
 */
@JsonClass(generateAdapter = true)
data class GutenbergBook(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "title") val title: String = "",
    @Json(name = "authors") val authors: List<GutenbergAuthor> = emptyList(),
    @Json(name = "translators") val translators: List<GutenbergAuthor> = emptyList(),
    @Json(name = "subjects") val subjects: List<String> = emptyList(),
    @Json(name = "bookshelves") val bookshelves: List<String> = emptyList(),
    @Json(name = "languages") val languages: List<String> = emptyList(),
    @Json(name = "copyright") val copyright: Boolean = false,
    @Json(name = "media_type") val mediaType: String = "",
    @Json(name = "formats") val formats: Map<String, String> = emptyMap(),
    @Json(name = "download_count") val downloadCount: Int = 0
)

/**
 * مؤلف من Project Gutenberg
 */
@JsonClass(generateAdapter = true)
data class GutenbergAuthor(
    @Json(name = "name") val name: String = "",
    @Json(name = "birth_year") val birthYear: Int? = null,
    @Json(name = "death_year") val deathYear: Int? = null
)
