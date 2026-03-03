package com.gadgeski.bugcodex.data.prefs

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * BugMemo Mode: Robust Settings Repository
 * [SECURITY UPGRADE]
 * - Hilt による DI への移行を完了。
 * - Annotation Use-site Target を指定し、Hilt の解決を確実にしました。
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:Named("SecureStorage") private val secureStorage: SharedPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val filterFolderIdKey = longPreferencesKey("filter_folder_id")
        val lastQueryKey = stringPreferencesKey("last_query")
        val legacyGithubTokenKey = stringPreferencesKey("github_token")
        const val SECURE_GITHUB_TOKEN = "secure_github_token"
    }

    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    init {
        // 1. 暗号化ストレージから現在の値を読み込み
        _githubToken.value = secureStorage.getString(Keys.SECURE_GITHUB_TOKEN, "") ?: ""

        // 2. DataStore (平文) からのマイグレーション実行
        performMigration()
    }

    // --- DataStore Accessors ---
    val filterFolderId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[Keys.filterFolderIdKey] }
        .distinctUntilChanged()

    suspend fun setFilterFolderId(id: Long?) {
        dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.filterFolderIdKey)
            } else {
                prefs[Keys.filterFolderIdKey] = id
            }
        }
    }

    val lastQuery: Flow<String> = dataStore.data
        .map { prefs -> prefs[Keys.lastQueryKey] ?: "" }
        .distinctUntilChanged()

    suspend fun setLastQuery(q: String) {
        dataStore.edit { prefs -> prefs[Keys.lastQueryKey] = q }
    }

    // --- Secure Accessors ---
    fun setGithubToken(token: String) {
        secureStorage.edit().putString(Keys.SECURE_GITHUB_TOKEN, token).apply()
        _githubToken.value = token
    }

    /**
     * 将来的なログアウトやトークンリセット機能のために定義。
     * 現時点では未使用のため警告を抑制。
     */
    @Suppress("unused")
    fun clearGithubToken() {
        secureStorage.edit().remove(Keys.SECURE_GITHUB_TOKEN).apply()
        _githubToken.value = ""
    }

    // --- Migration Logic ---
    private fun performMigration() {
        scope.launch {
            val prefs = dataStore.data.first()
            val legacyToken = prefs[Keys.legacyGithubTokenKey]

            if (!legacyToken.isNullOrEmpty()) {
                setGithubToken(legacyToken)
                dataStore.edit { it.remove(Keys.legacyGithubTokenKey) }
            }
        }
    }
}
