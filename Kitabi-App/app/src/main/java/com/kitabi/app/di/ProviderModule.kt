package com.kitabi.app.di

import com.kitabi.app.data.repository.DownloadRepositoryImpl
import com.kitabi.app.domain.repository.DownloadRepository
import dagger.Module
import dagger.Binds
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * وحدة حقن التبعيات لمزودي المحتوى
 * توفر ربط مستودع التحميل
 * مزودات PDF و EPUB و TXT ومعالج الاستيراد يتم توفيرها تلقائياً عبر @Inject constructor
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {

    /**
     * ربط مستودع التحميل
     */
    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
}
