package com.kitabi.app.data.remote.store

import com.kitabi.app.data.remote.store.dto.GoogleBookVolume
import com.kitabi.app.data.remote.store.dto.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * واجهة برمجة تطبيقات Google Books
 * مجانية - 1000 طلب يومياً - تحتوي على كتب عربية
 */
interface GoogleBooksApiService {

    /**
     * البحث عن كتب
     * @param query نص البحث
     * @param langRestrict تقييد اللغة
     * @param maxResults عدد النتائج القصوى
     * @param startIndex فهرس البداية
     * @param orderBy ترتيب النتائج
     * @param apiKey مفتاح API (اختياري)
     */
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("langRestrict") langRestrict: String = "ar",
        @Query("maxResults") maxResults: Int = 20,
        @Query("startIndex") startIndex: Int = 0,
        @Query("orderBy") orderBy: String = "relevance",
        @Query("key") apiKey: String = ""
    ): GoogleBooksResponse

    /**
     * الحصول على تفاصيل كتاب
     * @param volumeId معرف الكتاب
     */
    @GET("volumes/{volumeId}")
    suspend fun getBookDetail(
        @Path("volumeId") volumeId: String
    ): GoogleBookVolume

    companion object {
        /** عنوان الخادم الأساسي */
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
    }
}
