package com.gadgeski.bugcodex.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val DS_NAME = "settings"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

class SettingsRepository private constructor(
    private val appContext: Context,
) {
    // Repository内でStateFlowを維持するためのスコープ（アプリプロセスと同寿命の想定）
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // キーは lower_snake_case（Preferencesのキー文字列）でOK。プロパティ名はlowerCamelでOK。
    private object Keys {
        val filterFolderIdKey = longPreferencesKey("filter_folder_id")
        val lastQueryKey = stringPreferencesKey("last_query")

        // ★ Migrated: GitHub Token を DataStore に保存（平文）
        val githubTokenKey = stringPreferencesKey("github_token")
    }

    /** 現在のフォルダ絞り込みID（なければ null） */
    val filterFolderId: Flow<Long?> =
        appContext.dataStore.data
            .map { prefs: Preferences -> prefs[Keys.filterFolderIdKey] }
            .distinctUntilChanged()

    /** フォルダ絞り込みIDを保存（null で削除） */
    suspend fun setFilterFolderId(id: Long?) {
        appContext.dataStore.edit { prefs ->
            if (id == null) {
                prefs.remove(Keys.filterFolderIdKey)
            } else {
                prefs[Keys.filterFolderIdKey] = id
            }
        }
    }

    /** 検索クエリ（空文字をデフォルトで返す） */
    val lastQuery: Flow<String> =
        appContext.dataStore.data
            .map { prefs -> prefs[Keys.lastQueryKey] ?: "" }
            .distinctUntilChanged()

    /** 検索クエリを保存 */
    suspend fun setLastQuery(q: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.lastQueryKey] = q
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GitHub Token (DataStore Preferences)  ※平文
    // ─────────────────────────────────────────────────────────────
    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    init {
        // DataStore → StateFlow にブリッジ（呼び出し元の変更を避けるため）
        scope.launch {
            appContext.dataStore.data
                .map { prefs -> prefs[Keys.githubTokenKey] ?: "" }
                .distinctUntilChanged()
                .collect { token ->
                    _githubToken.value = token
                }
        }
    }

    /** GitHub Token を保存（空文字も許容。空なら remove にしたい場合はここで分岐） */
    suspend fun setGithubToken(token: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.githubTokenKey] = token
        }
        // 即時反映（collectの反映を待たずUIが追従する）
        _githubToken.value = token
    }

    // ─────────────────────────────────────────────────────────────
    // デバッグ／テスト用途の全消し API（今は未使用）
    // ─────────────────────────────────────────────────────────────
    @Suppress("unused")
    suspend fun clearAll() {
        appContext.dataStore.edit { it.clear() }
        _githubToken.value = ""
    }

    companion object {
        @Volatile
        private var instance: SettingsRepository? = null

        fun get(context: Context): SettingsRepository = instance ?: synchronized(this) {
            instance ?: SettingsRepository(context.applicationContext).also { instance = it }
        }
    }
}
