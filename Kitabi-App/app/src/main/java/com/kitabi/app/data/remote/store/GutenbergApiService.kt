package com.kitabi.app.data.remote.store

import com.kitabi.app.data.remote.store.dto.GutenbergResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * واجهة برمجة تطبيقات Project Gutenberg
 * مجانية - كتب ملكية عامة مع نص كامل للتحميل
 */
interface GutenbergApiService {

    /**
     * الحصول على كتب
     * @param languages رمز اللغة (ar للعربية)
     * @param page رقم الصفحة
     * @param search نص البحث
     * @param topic الموضوع
     */
    @GET("books")
    suspend fun getBooks(
        @Query("languages") languages: String = "ar",
        @Query("page") page: Int = 1,
        @Query("search") search: String? = null,
        @Query("topic") topic: String? = null
    ): GutenbergResponse

    companion object {
        /** عنوان الخادم الأساسي */
        const val BASE_URL = "https://gutendex.com/"
    }
}
