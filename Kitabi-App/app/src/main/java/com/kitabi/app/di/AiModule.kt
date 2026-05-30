package com.kitabi.app.di

import com.kitabi.app.provider.ai.AiResponseCache
import com.kitabi.app.data.remote.firebase.FirebaseChatRepository
import com.kitabi.app.data.remote.firebase.FirebaseAuthRepository
import com.kitabi.app.data.remote.firebase.FirebaseReviewRepository
import com.kitabi.app.data.remote.firebase.FirebaseUserCounterRepository
import com.kitabi.app.domain.repository.AuthRepository
import com.kitabi.app.domain.repository.ChatRepository
import com.kitabi.app.domain.repository.ReviewRepository
import com.kitabi.app.domain.repository.UserCounterRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * وحدة حقن تبعيات الذكاء الاصطناعي والمستودعات البعيدة
 * توفر مزودات AI والمستودعات عبر Hilt
 * يستخدم @Binds لربط التنفيذات بالواجهات
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    /**
     * ربط مستودع المحادثات
     */
    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: FirebaseChatRepository): ChatRepository

    /**
     * ربط مستودع المراجعات
     */
    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: FirebaseReviewRepository): ReviewRepository

    /**
     * ربط مستودع عداد المستخدمين
     */
    @Binds
    @Singleton
    abstract fun bindUserCounterRepository(impl: FirebaseUserCounterRepository): UserCounterRepository

    /**
     * ربط مستودع المصادقة
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    companion object {
        /**
         * توفير ذاكرة التخزين المؤقت لاستجابات AI
         */
        @Provides
        @Singleton
        fun provideAiResponseCache(): AiResponseCache {
            return AiResponseCache(maxSize = 50)
        }
    }
}
