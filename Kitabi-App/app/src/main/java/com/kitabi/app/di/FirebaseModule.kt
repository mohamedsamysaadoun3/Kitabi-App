package com.kitabi.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * وحدة حقن تبعيات Firebase
 * توفر جميع مثيلات Firebase عبر Hilt
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    /**
     * توفير FirebaseAuth لمصادقة المستخدمين
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    /**
     * توفير FirebaseFirestore لقاعدة البيانات السحابية
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    /**
     * توفير FirebaseDatabase لقاعدة البيانات في الوقت الحقيقي
     * مع تفعيل الاستمرار في وضع عدم الاتصال
     */
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase {
        val database = Firebase.database(
            "https://kitabi-20c92-default-rtdb.firebaseio.com"
        )
        database.setPersistenceEnabled(true)
        return database
    }

    /**
     * توفير FirebaseRemoteConfig للإعداد البعيد
     */
    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return Firebase.remoteConfig
    }
}
