package com.kitabi.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان الإشارة المرجعية
 * يمثل إشارة مرجعية في كتاب معين
 */
@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bookId"], name = "index_bookmarks_book_id"),
        Index(value = ["page"], name = "index_bookmarks_page")
    ]
)
data class BookmarkEntity(
    /** المعرف الفريد */
    @PrimaryKey
    val id: String,

    /** معرف الكتاب */
    val bookId: String,

    /** رقم الصفحة */
    val page: Int,

    /** ملاحظة الإشارة المرجعية */
    val note: String = "",

    /** النص المحدد */
    val selectedText: String = "",

    /** تاريخ الإنشاء */
    val createdAt: Long = System.currentTimeMillis()
)
