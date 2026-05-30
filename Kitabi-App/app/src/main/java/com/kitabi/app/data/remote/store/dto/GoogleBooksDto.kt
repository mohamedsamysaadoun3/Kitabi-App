package com.kitabi.app.data.remote.store.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * استجابة بحث Google Books
 */
@JsonClass(generateAdapter = true)
data class GoogleBooksResponse(
    @Json(name = "kind") val kind: String = "",
    @Json(name = "totalItems") val totalItems: Int = 0,
    @Json(name = "items") val items: List<GoogleBookVolume>? = null
)

/**
 * كتاب من Google Books
 */
@JsonClass(generateAdapter = true)
data class GoogleBookVolume(
    @Json(name = "id") val id: String = "",
    @Json(name = "volumeInfo") val volumeInfo: GoogleVolumeInfo = GoogleVolumeInfo(),
    @Json(name = "accessInfo") val accessInfo: GoogleAccessInfo = GoogleAccessInfo(),
    @Json(name = "saleInfo") val saleInfo: GoogleSaleInfo = GoogleSaleInfo()
)

/**
 * معلومات الكتاب في Google Books
 */
@JsonClass(generateAdapter = true)
data class GoogleVolumeInfo(
    @Json(name = "title") val title: String = "",
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "authors") val authors: List<String>? = null,
    @Json(name = "publisher") val publisher: String? = null,
    @Json(name = "publishedDate") val publishedDate: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "industryIdentifiers") val industryIdentifiers: List<GoogleIndustryIdentifier>? = null,
    @Json(name = "pageCount") val pageCount: Int = 0,
    @Json(name = "printedPageCount") val printedPageCount: Int? = null,
    @Json(name = "dimensions") val dimensions: GoogleDimensions? = null,
    @Json(name = "printType") val printType: String = "",
    @Json(name = "categories") val categories: List<String>? = null,
    @Json(name = "averageRating") val averageRating: Double = 0.0,
    @Json(name = "ratingsCount") val ratingsCount: Int = 0,
    @Json(name = "maturityRating") val maturityRating: String = "",
    @Json(name = "allowAnonLogging") val allowAnonLogging: Boolean = false,
    @Json(name = "contentVersion") val contentVersion: String = "",
    @Json(name = "imageLinks") val imageLinks: GoogleImageLinks? = null,
    @Json(name = "language") val language: String = "",
    @Json(name = "previewLink") val previewLink: String? = null,
    @Json(name = "infoLink") val infoLink: String? = null,
    @Json(name = "canonicalVolumeLink") val canonicalVolumeLink: String? = null
)

/**
 * روابط صور الكتاب في Google Books
 */
@JsonClass(generateAdapter = true)
data class GoogleImageLinks(
    @Json(name = "smallThumbnail") val smallThumbnail: String? = null,
    @Json(name = "thumbnail") val thumbnail: String? = null,
    @Json(name = "small") val small: String? = null,
    @Json(name = "medium") val medium: String? = null,
    @Json(name = "large") val large: String? = null,
    @Json(name = "extraLarge") val extraLarge: String? = null
)

/**
 * معلومات الوصول في Google Books
 */
@JsonClass(generateAdapter = true)
data class GoogleAccessInfo(
    @Json(name = "country") val country: String = "",
    @Json(name = "viewability") val viewability: String = "",
    @Json(name = "embeddable") val embeddable: Boolean = false,
    @Json(name = "publicDomain") val publicDomain: Boolean = false,
    @Json(name = "textToSpeechPermission") val textToSpeechPermission: String = "",
    @Json(name = "epub") val epub: GoogleDownloadInfo = GoogleDownloadInfo(),
    @Json(name = "pdf") val pdf: GoogleDownloadInfo = GoogleDownloadInfo(),
    @Json(name = "webReaderLink") val webReaderLink: String? = null,
    @Json(name = "accessViewStatus") val accessViewStatus: String = "",
    @Json(name = "quoteSharingAllowed") val quoteSharingAllowed: Boolean = false
)

/**
 * معلومات التحميل
 */
@JsonClass(generateAdapter = true)
data class GoogleDownloadInfo(
    @Json(name = "isAvailable") val isAvailable: Boolean = false,
    @Json(name = "downloadLink") val downloadLink: String? = null,
    @Json(name = "acsTokenLink") val acsTokenLink: String? = null
)

/**
 * معلومات البيع
 */
@JsonClass(generateAdapter = true)
data class GoogleSaleInfo(
    @Json(name = "country") val country: String = "",
    @Json(name = "saleability") val saleability: String = "",
    @Json(name = "isEbook") val isEbook: Boolean = false,
    @Json(name = "listPrice") val listPrice: GooglePrice? = null,
    @Json(name = "retailPrice") val retailPrice: GooglePrice? = null,
    @Json(name = "buyLink") val buyLink: String? = null
)

/**
 * سعر الكتاب
 */
@JsonClass(generateAdapter = true)
data class GooglePrice(
    @Json(name = "amount") val amount: Double = 0.0,
    @Json(name = "currencyCode") val currencyCode: String = ""
)

/**
 * المعرف الدولي للكتاب
 */
@JsonClass(generateAdapter = true)
data class GoogleIndustryIdentifier(
    @Json(name = "type") val type: String = "",
    @Json(name = "identifier") val identifier: String = ""
)

/**
 * أبعاد الكتاب
 */
@JsonClass(generateAdapter = true)
data class GoogleDimensions(
    @Json(name = "height") val height: String? = null,
    @Json(name = "width") val width: String? = null,
    @Json(name = "thickness") val thickness: String? = null
)
