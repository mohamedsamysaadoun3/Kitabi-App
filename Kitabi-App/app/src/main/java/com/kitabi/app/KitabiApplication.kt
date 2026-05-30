package com.kitabi.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * فئة التطبيق الرئيسية لكتابي
 * تهيئة Hilt و Firebase و WorkManager
 */
@HiltAndroidApp
class KitabiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // تهيئة Firebase
        FirebaseApp.initializeApp(this)

        // إعداد الإعداد البعيد لـ Firebase
        setupRemoteConfig()

        // جدولة المهام الدورية في الخلفية
        schedulePeriodicWork()

        // جدولة تنظيف المحادثات
        scheduleChatCleanup()
    }

    /**
     * إعداد Firebase Remote Config
     * لجلب الإعدادات من الخادم بدون تحديث التطبيق
     */
    private fun setupRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600L)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)

        // القيم الافتراضية للإعدادات البعيدة
        val defaults = mapOf(
            "ai_enabled" to true,
            "max_books_offline" to 50L,
            "chat_enabled" to true,
            "store_enabled" to true,
            "maintenance_mode" to false,
            "min_app_version" to "1.0.0",
            "ai_model_name" to "mistral-large-latest",
            "ai_max_tokens" to 2048L,
            "ai_temperature" to 0.7,
            "mistral_api_key" to "",
            "huggingface_api_key" to "",
            "tts_enabled" to true
        )

        remoteConfig.setDefaultsAsync(defaults)
        remoteConfig.fetchAndActivate()
    }

    /**
     * جدولة المهام الدورية
     * مزامنة بيانات الكتب وإحصائيات القراءة
     */
    private fun schedulePeriodicWork() {
        val syncWorkRequest = PeriodicWorkRequestBuilder<androidx.work.CoroutineWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kitabi_sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    /**
     * جدولة تنظيف المحادثات
     * حذف الرسائل الأقدم من 30 يوماً كل 24 ساعة
     */
    private fun scheduleChatCleanup() {
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<com.kitabi.app.worker.ChatCleanupWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kitabi_chat_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )
    }
}
