package com.kitabi.app.di

import com.kitabi.app.provider.ai.AiResponseCache
import com.kitabi.app.provider.ai.AiRouter
import com.kitabi.app.provider.ai.AndroidTtsProvider
import com.kitabi.app.provider.ai.HuggingFaceProvider
import com.kitabi.app.provider.ai.MistralProvider
import com.kitabi.app.data.remote.firebase.FirebaseChatRepository
import com.kitabi.app.data.remote.firebase.FirebaseAuthRepository
import com.kitabi.app.data.remote.firebase.FirebaseReviewRepository
import com.kitabi.app.data.remote.firebase.FirebaseUserCounterRepository
import com.kitabi.app.domain.repository.AuthRepository
import com.kitabi.app.domain.repository.ChatRepository
import com.kitabi.app.domain.repository.ReviewRepository
import com.kitabi.app.domain.repository.UserCounterRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * وحدة حقن تبعيات الذكاء الاصطناعي والمستودعات البعيدة
 * توفر مزودات AI والمستودعات عبر Hilt
 */
@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    /**
     * توفير ذاكرة التخزين المؤقت لاستجابات AI
     */
    @Provides
    @Singleton
    fun provideAiResponseCache(): AiResponseCache {
        return AiResponseCache(maxSize = 50)
    }

    /**
     * توفير مزود Mistral
     */
    @Provides
    @Singleton
    fun provideMistralProvider(
        okHttpClient: okhttp3.OkHttpClient,
        remoteConfig: com.google.firebase.remoteconfig.FirebaseRemoteConfig,
        cache: AiResponseCache
    ): MistralProvider {
        return MistralProvider(okHttpClient, remoteConfig, cache)
    }

    /**
     * توفير مزود HuggingFace
     */
    @Provides
    @Singleton
    fun provideHuggingFaceProvider(
        okHttpClient: okhttp3.OkHttpClient,
        remoteConfig: com.google.firebase.remoteconfig.FirebaseRemoteConfig,
        cache: AiResponseCache
    ): HuggingFaceProvider {
        return HuggingFaceProvider(okHttpClient, remoteConfig, cache)
    }

    /**
     * توفير موجه AI
     */
    @Provides
    @Singleton
    fun provideAiRouter(
        mistralProvider: MistralProvider,
        huggingFaceProvider: HuggingFaceProvider,
        ttsProvider: AndroidTtsProvider,
        cache: AiResponseCache
    ): AiRouter {
        return AiRouter(mistralProvider, huggingFaceProvider, ttsProvider, cache)
    }

    // ============ مستودعات Firebase ============

    /**
     * توفير مستودع المحادثات
     */
    @Provides
    @Singleton
    fun provideChatRepository(
        firebaseDatabase: com.google.firebase.database.FirebaseDatabase
    ): ChatRepository {
        return FirebaseChatRepository(firebaseDatabase)
    }

    /**
     * توفير مستودع المراجعات
     */
    @Provides
    @Singleton
    fun provideReviewRepository(
        firestore: com.google.firebase.firestore.FirebaseFirestore
    ): ReviewRepository {
        return FirebaseReviewRepository(firestore)
    }

    /**
     * توفير مستودع عداد المستخدمين
     */
    @Provides
    @Singleton
    fun provideUserCounterRepository(
        firebaseDatabase: com.google.firebase.database.FirebaseDatabase
    ): UserCounterRepository {
        return FirebaseUserCounterRepository(firebaseDatabase)
    }

    /**
     * توفير مستودع المصادقة
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: com.google.firebase.auth.FirebaseAuth
    ): AuthRepository {
        return FirebaseAuthRepository(firebaseAuth)
    }
}
