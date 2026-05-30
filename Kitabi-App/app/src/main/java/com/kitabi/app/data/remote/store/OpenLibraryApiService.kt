package com.kitabi.app.data.remote.store

import com.kitabi.app.data.remote.store.dto.OpenLibrarySearchResponse
import com.kitabi.app.data.remote.store.dto.OpenLibrarySubjectResponse
import com.kitabi.app.data.remote.store.dto.OpenLibraryWork
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * واجهة برمجة تطبيقات Open Library
 * مجانية بدون مفتاح API - ملايين الكتب مع دعم العربية
 */
interface OpenLibraryApiService {

    /**
     * البحث عن كتب
     * @param query نص البحث
     * @param language رمز اللغة (ara للعربية)
     * @param limit عدد النتائج
     * @param page رقم الصفحة
     * @param subject التصنيف
     */
    @GET("search.json")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("language") language: String = "ara",
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1,
        @Query("subject") subject: String? = null
    ): OpenLibrarySearchResponse

    /**
     * الحصول على تفاصيل كتاب
     * @param workId معرف العمل
     */
    @GET("works/{workId}.json")
    suspend fun getBookDetail(
        @Path("workId") workId: String
    ): OpenLibraryWork

    /**
     * الحصول على كتب حسب الموضوع
     * @param subject اسم الموضوع
     * @param limit عدد النتائج
     */
    @GET("subjects/{subject}.json")
    suspend fun getBooksBySubject(
        @Path("subject") subject: String,
        @Query("limit") limit: Int = 20
    ): OpenLibrarySubjectResponse

    companion object {
        /** عنوان الخادم الأساسي */
        const val BASE_URL = "https://openlibrary.org/"

        /** رابط صورة الغلاف */
        fun getCoverUrl(coverId: Int, size: String = "M"): String {
            return "https://covers.openlibrary.org/b/id/$coverId-$size.jpg"
        }
    }
}
