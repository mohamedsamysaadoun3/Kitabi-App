package com.kitabi.app.di

import com.kitabi.app.domain.repository.DownloadRepository
import com.kitabi.app.data.repository.DownloadRepositoryImpl
import com.kitabi.app.feature.library.components.ImportBookHandler
import com.kitabi.app.provider.book.ContentProviderFactory
import com.kitabi.app.provider.book.EpubContentProvider
import com.kitabi.app.provider.book.PdfContentProvider
import com.kitabi.app.provider.book.TxtContentProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * وحدة حقن التبعيات لمزودي المحتوى
 * توفر مزودي PDF و EPUB و TXT ومصنع المزودات
 * و معالج الاستيراد ومستودع التحميل
 */
@Module
@InstallIn(SingletonComponent::class)
object ProviderModule {

    /**
     * توفير مصنع مزودي المحتوى
     * يجمع بين جميع المزودات ويوفر المزود المناسب حسب الصيغة
     */
    @Provides
    @Singleton
    fun provideContentProviderFactory(
        pdfProvider: PdfContentProvider,
        epubProvider: EpubContentProvider,
        txtProvider: TxtContentProvider
    ): ContentProviderFactory {
        return ContentProviderFactory(
            pdfProvider = pdfProvider,
            epubProvider = epubProvider,
            txtProvider = txtProvider
        )
    }

    /**
     * توفير معالج استيراد الكتب
     * يستخدم مصنع المزودات ومستودع الكتب لاستيراد الملفات المحلية
     */
    @Provides
    @Singleton
    fun provideImportBookHandler(
        contentProviderFactory: ContentProviderFactory,
        bookRepository: com.kitabi.app.domain.repository.BookRepository
    ): ImportBookHandler {
        return ImportBookHandler(
            contentProviderFactory = contentProviderFactory,
            bookRepository = bookRepository
        )
    }

    /**
     * توفير مستودع التحميل
     * يستخدم WorkManager لعمليات التحميل في الخلفية
     */
    @Provides
    @Singleton
    fun provideDownloadRepository(
        impl: DownloadRepositoryImpl
    ): DownloadRepository {
        return impl
    }
}
