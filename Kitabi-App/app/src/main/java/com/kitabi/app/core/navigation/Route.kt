package com.kitabi.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * تعريف مسارات التنقل في التطبيق
 * كل مسار يمثل شاشة في التطبيق
 */
sealed class Route(val route: String) {

    /** شاشة التعريف الأولي */
    object Onboarding : Route("onboarding")

    /** شاشة المصادقة (تسجيل الدخول / إنشاء حساب) */
    object Auth : Route("auth")

    /** شاشة المكتبة الرئيسية */
    object Library : Route("library")

    /** شاشة المتجر */
    object Store : Route("store")

    /** شاشة قراءة الكتاب */
    object Reader : Route("reader/{bookId}") {
        /**
         * إنشاء مسار قراءة الكتاب مع معرف الكتاب
         * @param bookId معرف الكتاب
         */
        fun createRoute(bookId: String): String = "reader/$bookId"
    }

    /** شاشة تفاصيل الكتاب الإلكتروني */
    object BookDetail : Route("book_detail/{bookId}/{source}") {
        fun createRoute(bookId: String, source: String): String = "book_detail/$bookId/$source"
    }

    /** شاشة البحث */
    object Search : Route("search")

    /** شاشة الإشارات المرجعية */
    object Bookmarks : Route("bookmarks")

    /** شاشة قائمة غرف المحادثة */
    object ChatRooms : Route("chat_rooms")

    /** شاشة غرفة المحادثة */
    object ChatRoom : Route("chat_room/{roomId}") {
        /**
         * إنشاء مسار غرفة المحادثة مع معرف الغرفة
         * @param roomId معرف الغرفة
         */
        fun createRoute(roomId: String): String = "chat_room/$roomId"
    }

    /** شاشة مراجعات الكتاب */
    object Reviews : Route("reviews/{bookId}") {
        /**
         * إنشاء مسار المراجعات مع معرف الكتاب
         * @param bookId معرف الكتاب
         */
        fun createRoute(bookId: String): String = "reviews/$bookId"
    }

    /** شاشة كتابة مراجعة */
    object WriteReview : Route("write_review/{bookId}") {
        /**
         * إنشاء مسار كتابة مراجعة مع معرف الكتاب
         * @param bookId معرف الكتاب
         */
        fun createRoute(bookId: String): String = "write_review/$bookId"
    }

    /** شاشة الإعدادات */
    object Settings : Route("settings")

    /** شاشة إحصائيات القراءة */
    object Stats : Route("stats")

    /** شاشة مساعد الذكاء الاصطناعي */
    object AiAssistant : Route("ai_assistant/{bookId}") {
        /**
         * إنشاء مسار مساعد الذكاء الاصطناعي مع معرف الكتاب
         * @param bookId معرف الكتاب
         */
        fun createRoute(bookId: String): String = "ai_assistant/$bookId"
    }
}

/**
 * عناصر شريط التنقل السفلي
 * @param route مسار التنقل
 * @param label تسمية العرض
 * @param icon أيقونة العرض
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    /** عنصر المكتبة */
    LIBRARY(
        route = Route.Library.route,
        label = "المكتبة",
        icon = Icons.Filled.LibraryBooks
    ),

    /** عنصر المتجر */
    STORE(
        route = Route.Store.route,
        label = "المتجر",
        icon = Icons.Filled.Storefront
    ),

    /** عنصر البحث */
    SEARCH(
        route = Route.Search.route,
        label = "بحث",
        icon = Icons.Filled.Search
    ),

    /** عنصر الإشارات المرجعية */
    BOOKMARKS(
        route = Route.Bookmarks.route,
        label = "الإشارات",
        icon = Icons.Filled.Bookmark
    ),

    /** عنصر الإعدادات */
    SETTINGS(
        route = Route.Settings.route,
        label = "الإعدادات",
        icon = Icons.Filled.Settings
    )
}
