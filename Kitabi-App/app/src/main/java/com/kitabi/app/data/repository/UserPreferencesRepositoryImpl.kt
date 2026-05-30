package com.kitabi.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kitabi.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * تنفيذ مستودع تفضيلات المستخدم
 * يستخدم DataStore لتخزين الإعدادات المحلية
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val READER_FONT_SIZE = intPreferencesKey("reader_font_size")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
        val SELECTED_CATEGORY = stringPreferencesKey("selected_category")
    }

    override val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: false
    }

    override val appLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.APP_LANGUAGE] ?: "ar"
    }

    override val readerFontSize: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.READER_FONT_SIZE] ?: 18
    }

    override val readerTheme: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.READER_THEME] ?: "light"
    }

    override val isGridView: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.IS_GRID_VIEW] ?: true
    }

    override val selectedCategory: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_CATEGORY] ?: "ALL"
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_DARK_MODE] = isDark }
    }

    override suspend fun setAppLanguage(language: String) {
        dataStore.edit { prefs -> prefs[Keys.APP_LANGUAGE] = language }
    }

    override suspend fun setReaderFontSize(size: Int) {
        dataStore.edit { prefs -> prefs[Keys.READER_FONT_SIZE] = size }
    }

    override suspend fun setReaderTheme(theme: String) {
        dataStore.edit { prefs -> prefs[Keys.READER_THEME] = theme }
    }

    override suspend fun setGridView(isGrid: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_GRID_VIEW] = isGrid }
    }

    override suspend fun setSelectedCategory(category: String) {
        dataStore.edit { prefs -> prefs[Keys.SELECTED_CATEGORY] = category }
    }
}
