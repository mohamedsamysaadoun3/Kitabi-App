package com.kitabi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.core.navigation.KitabiNavHost
import com.kitabi.app.domain.repository.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * النشاط الرئيسي لتطبيق كتابي
 * يحتوي على شاشة البداية والتنقل الرئيسي
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // حالة الاستمرار في عرض شاشة البداية
    private var keepSplashScreen by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        // تثبيت شاشة البداية قبل إنشاء النشاط
        val splashScreen = installSplashScreen()

        // الحفاظ على شاشة البداية حتى اكتمال التهيئة
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        super.onCreate(savedInstanceState)

        // تفعيل وضع الحواف إلى الحواف
        enableEdgeToEdge()

        // تهيئة Firebase Auth بشكل مجهول إذا لم يكن المستخدم مسجلاً
        initializeUser()

        // تفعيل الإعداد البعيد
        remoteConfig.activate()

        setContent {
            val isDarkMode by userPreferencesRepository.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            
            KitabiTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KitabiNavHost()
                }
            }
            
            // إخفاء شاشة البداية بعد اكتمال التهيئة
            keepSplashScreen = false
        }
    }

    /**
     * تهيئة المستخدم
     * تسجيل الدخول بشكل مجهول إذا لم يكن هناك مستخدم
     */
    private fun initializeUser() {
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        android.util.Log.d(TAG, "تم تسجيل الدخول بشكل مجهول بنجاح")
                    } else {
                        android.util.Log.e(TAG, "فشل تسجيل الدخول المجهول", task.exception)
                    }
                }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
