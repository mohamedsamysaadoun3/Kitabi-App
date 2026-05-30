package com.kitabi.app.provider.ai

import android.util.LruCache

/**
 * ذاكرة تخزين مؤقت لاستجابات الذكاء الاصطناعي
 * تستخدم LRU Cache لحفظ الاستجابات وتجنب الطلبات المتكررة
 * تدعم مفتاح مخصص لكل طلب بناءً على المحتوى والمزود
 */
class AiResponseCache(
    /** الحد الأقصى لعدد الإدخالات في الذاكرة */
    maxSize: Int = 50
) {

    /** ذاكرة التخزين المؤقت LRU */
    private val cache = LruCache<String, CacheEntry>(maxSize)

    /**
     * إدخال في الذاكرة المؤقتة
     */
    data class CacheEntry(
        /** الاستجابة المخزنة */
        val response: String,
        /** وقت التخزين */
        val timestamp: Long = System.currentTimeMillis(),
        /** مدة الصلاحية بالمللي ثانية (30 دقيقة افتراضياً) */
        val ttl: Long = 30 * 60 * 1000L
    ) {
        /** هل الإدخال لا يزال صالحاً */
        val isValid: Boolean
            get() = System.currentTimeMillis() - timestamp < ttl
    }

    /**
     * حفظ استجابة في الذاكرة المؤقتة
     * @param key المفتاح (يُنشأ من المحتوى)
     * @param response نص الاستجابة
     * @param ttl مدة الصلاحية بالمللي ثانية
     */
    fun put(key: String, response: String, ttl: Long = 30 * 60 * 1000L) {
        cache.put(key, CacheEntry(response = response, ttl = ttl))
    }

    /**
     * الحصول على استجابة من الذاكرة المؤقتة
     * @param key المفتاح
     * @return نص الاستجابة أو null إذا لم يكن موجوداً أو منتهي الصلاحية
     */
    fun get(key: String): String? {
        val entry = cache.get(key) ?: return null
        return if (entry.isValid) {
            entry.response
        } else {
            cache.remove(key)
            null
        }
    }

    /**
     * إنشاء مفتاح تخزين مؤقت
     * @param providerName اسم المزود
     * @param operation نوع العملية
     * @param input المدخلات
     * @return مفتاح فريد
     */
    fun createKey(providerName: String, operation: String, input: String): String {
        // استخدام رمز تجزئة بسيط لتقليل حجم المفتاح
        val inputHash = input.hashCode().toString(16)
        return "${providerName}_${operation}_${inputHash}"
    }

    /**
     * التحقق من وجود مفتاح في الذاكرة المؤقتة
     * @param key المفتاح
     * @return هل المفتاح موجود وصالح
     */
    fun contains(key: String): Boolean {
        val entry = cache.get(key) ?: return false
        return if (entry.isValid) true else {
            cache.remove(key)
            false
        }
    }

    /**
     * حذف إدخال من الذاكرة المؤقتة
     * @param key المفتاح
     */
    fun remove(key: String) {
        cache.remove(key)
    }

    /**
     * مسح جميع الإدخالات
     */
    fun clear() {
        cache.evictAll()
    }

    /**
     * عدد الإدخالات الصالحة
     */
    val size: Int
        get() = cache.size()
}
