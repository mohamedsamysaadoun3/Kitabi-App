package com.kitabi.app.di

import android.content.Context
import androidx.room.Room
import com.kitabi.app.data.local.KitabiDatabase
import com.kitabi.app.data.local.dao.BookDao
import com.kitabi.app.data.local.dao.BookmarkDao
import com.kitabi.app.data.local.dao.ReadingProgressDao
import com.kitabi.app.data.local.dao.ReadingStatsDao
import com.kitabi.app.data.remote.store.GoogleBooksApiService
import com.kitabi.app.data.remote.store.GutenbergApiService
import com.kitabi.app.data.remote.store.OpenLibraryApiService
import com.kitabi.app.data.remote.store.StoreRepositoryImpl
import com.kitabi.app.data.repository.BookRepositoryImpl
import com.kitabi.app.data.repository.ReadingProgressRepositoryImpl
import com.kitabi.app.data.repository.UserPreferencesRepositoryImpl
import com.kitabi.app.domain.repository.BookRepository
import com.kitabi.app.domain.repository.OnlineStoreRepository
import com.kitabi.app.domain.repository.ReadingProgressRepository
import com.kitabi.app.domain.repository.UserPreferencesRepository
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Binds
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * وحدة حقن التبعيات لقاعدة البيانات وخدمات API
 * توفر Room، DAOs، Retrofit API services والمستودعات
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    /**
     * ربط مستودع الكتب
     */
    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    /**
     * ربط مستودع تقدم القراءة
     */
    @Binds
    @Singleton
    abstract fun bindReadingProgressRepository(impl: ReadingProgressRepositoryImpl): ReadingProgressRepository

    /**
     * ربط مستودع المتجر الإلكتروني
     */
    @Binds
    @Singleton
    abstract fun bindOnlineStoreRepository(impl: StoreRepositoryImpl): OnlineStoreRepository

    /**
     * ربط مستودع تفضيلات المستخدم
     */
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    companion object {

        /**
         * توفير قاعدة بيانات Room
         */
        @Provides
        @Singleton
        fun provideKitabiDatabase(
            @ApplicationContext context: Context
        ): KitabiDatabase {
            return Room.databaseBuilder(
                context,
                KitabiDatabase::class.java,
                KitabiDatabase.DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }

        /**
         * توفير كائن الوصول لبيانات الكتب
         */
        @Provides
        @Singleton
        fun provideBookDao(database: KitabiDatabase): BookDao {
            return database.bookDao()
        }

        /**
         * توفير كائن الوصول لبيانات تقدم القراءة
         */
        @Provides
        @Singleton
        fun provideReadingProgressDao(database: KitabiDatabase): ReadingProgressDao {
            return database.readingProgressDao()
        }

        /**
         * توفير كائن الوصول لبيانات الإشارات المرجعية
         */
        @Provides
        @Singleton
        fun provideBookmarkDao(database: KitabiDatabase): BookmarkDao {
            return database.bookmarkDao()
        }

        /**
         * توفير كائن الوصول لبيانات إحصائيات القراءة
         */
        @Provides
        @Singleton
        fun provideReadingStatsDao(database: KitabiDatabase): ReadingStatsDao {
            return database.readingStatsDao()
        }

        /**
         * توفير واجهة Open Library API
         */
        @Provides
        @Singleton
        fun provideOpenLibraryApiService(moshi: Moshi, okHttpClient: okhttp3.OkHttpClient): OpenLibraryApiService {
            return Retrofit.Builder()
                .baseUrl(OpenLibraryApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenLibraryApiService::class.java)
        }

        /**
         * توفير واجهة Google Books API
         */
        @Provides
        @Singleton
        fun provideGoogleBooksApiService(moshi: Moshi, okHttpClient: okhttp3.OkHttpClient): GoogleBooksApiService {
            return Retrofit.Builder()
                .baseUrl(GoogleBooksApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GoogleBooksApiService::class.java)
        }

        /**
         * توفير واجهة Gutenberg API
         */
        @Provides
        @Singleton
        fun provideGutenbergApiService(moshi: Moshi, okHttpClient: okhttp3.OkHttpClient): GutenbergApiService {
            return Retrofit.Builder()
                .baseUrl(GutenbergApiService.BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GutenbergApiService::class.java)
        }
    }
}
