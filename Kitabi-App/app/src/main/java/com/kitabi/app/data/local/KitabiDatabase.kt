package com.kitabi.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kitabi.app.data.local.dao.BookDao
import com.kitabi.app.data.local.dao.BookmarkDao
import com.kitabi.app.data.local.dao.ReadingProgressDao
import com.kitabi.app.data.local.dao.ReadingStatsDao
import com.kitabi.app.data.local.entity.BookEntity
import com.kitabi.app.data.local.entity.BookmarkEntity
import com.kitabi.app.data.local.entity.ReadingProgressEntity
import com.kitabi.app.data.local.entity.ReadingStatsEntity

/**
 * قاعدة بيانات كتابي المحلية
 * تستخدم Room لتخزين الكتب وتقدم القراءة والإشارات المرجعية والإحصائيات
 */
@Database(
    entities = [
        BookEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        ReadingStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KitabiDatabase : RoomDatabase() {

    /** كائن الوصول لبيانات الكتب */
    abstract fun bookDao(): BookDao

    /** كائن الوصول لبيانات تقدم القراءة */
    abstract fun readingProgressDao(): ReadingProgressDao

    /** كائن الوصول لبيانات الإشارات المرجعية */
    abstract fun bookmarkDao(): BookmarkDao

    /** كائن الوصول لبيانات إحصائيات القراءة */
    abstract fun readingStatsDao(): ReadingStatsDao

    companion object {
        /** اسم قاعدة البيانات */
        const val DATABASE_NAME = "kitabi_database"
    }
}
