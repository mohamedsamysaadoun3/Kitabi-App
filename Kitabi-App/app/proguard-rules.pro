# Kitabi ProGuard Rules

# Keep domain models
-keepclassmembers class com.kitabi.app.domain.model.** { *; }

# Keep Firebase models
-keepclassmembers class com.kitabi.app.data.remote.store.dto.** { *; }

# Keep Room entities
-keepclassmembers class com.kitabi.app.data.local.entity.** { *; }

# Moshi
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.moshi.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**
