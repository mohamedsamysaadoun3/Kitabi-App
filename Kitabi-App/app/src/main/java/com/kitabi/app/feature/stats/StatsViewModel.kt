package com.kitabi.app.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kitabi.app.data.local.dao.ReadingStatsDao
import com.kitabi.app.data.local.entity.ReadingStatsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * نموذج عرض إحصائيات القراءة
 * يوفر بيانات الإحصائيات من قاعدة البيانات المحلية
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val readingStatsDao: ReadingStatsDao
) : ViewModel() {

    /** إجمالي الصفحات المقروءة */
    val totalPagesRead: StateFlow<Int> = readingStatsDao.getTotalPagesRead()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** التابع الحالي */
    val currentStreak: StateFlow<Int> = readingStatsDao.getCurrentStreak()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** إحصائيات الأسبوع */
    val weeklyStats: StateFlow<List<ReadingStatsEntity>> = readingStatsDao.getWeeklyStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** إجمالي وقت القراءة (بالثواني) */
    val totalReadingTime: StateFlow<Long> = readingStatsDao.getMonthlyStats()
        .map { stats -> stats.sumOf { it.readingTimeSeconds } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** عدد الكتب المكتملة */
    private val _booksCompleted = kotlinx.coroutines.flow.MutableStateFlow(0)
    val booksCompleted: StateFlow<Int> = _booksCompleted
}
