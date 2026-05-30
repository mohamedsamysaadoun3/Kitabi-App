package com.kitabi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kitabi.app.data.local.entity.ReadingStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * كائن الوصول لبيانات إحصائيات القراءة
 * يوفر عمليات تتبع إحصائيات القراءة اليومية
 */
@Dao
interface ReadingStatsDao {

    /**
     * إدراج أو تحديث إحصائيات يوم
     * @param stats كيان الإحصائيات
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: ReadingStatsEntity)

    /**
     * الحصول على إحصائيات يوم معين
     * @param date التاريخ بصيغة yyyy-MM-dd
     * @return كيان الإحصائيات أو null
     */
    @Query("SELECT * FROM reading_stats WHERE date = :date")
    suspend fun getStatsByDate(date: String): ReadingStatsEntity?

    /**
     * الحصول على إحصائيات آخر 7 أيام
     * @return تدفق قائمة الإحصائيات
     */
    @Query("SELECT * FROM reading_stats ORDER BY date DESC LIMIT 7")
    fun getWeeklyStats(): Flow<List<ReadingStatsEntity>>

    /**
     * الحصول على إحصائيات آخر 30 يوماً
     * @return تدفق قائمة الإحصائيات
     */
    @Query("SELECT * FROM reading_stats ORDER BY date DESC LIMIT 30")
    fun getMonthlyStats(): Flow<List<ReadingStatsEntity>>

    /**
     * إجمالي الصفحات المقروءة
     * @return إجمالي الصفحات
     */
    @Query("SELECT COALESCE(SUM(pagesRead), 0) FROM reading_stats")
    fun getTotalPagesRead(): Flow<Int>

    /**
     * تابع القراءة المتتالية الحالي
     * @return عدد الأيام المتتالية
     */
    @Query("SELECT COALESCE(MAX(streak), 0) FROM reading_stats")
    fun getCurrentStreak(): Flow<Int>

    /**
     * تحديث الصفحات المقروءة ليوم معين
     * @param date التاريخ
     * @param pages الصفحات الإضافية
     */
    @Query("UPDATE reading_stats SET pagesRead = pagesRead + :pages WHERE date = :date")
    suspend fun addPagesRead(date: String, pages: Int)

    /**
     * تحديث مدة القراءة ليوم معين
     * @param date التاريخ
     * @param seconds الثواني الإضافية
     */
    @Query("UPDATE reading_stats SET readingTimeSeconds = readingTimeSeconds + :seconds WHERE date = :date")
    suspend fun addReadingTime(date: String, seconds: Long)
}
