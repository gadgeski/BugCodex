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
 * * [SECURITY UPGRADE]
 * - 一般的な検索クエリやフィルタ設定は DataStore (非暗号化) を継続利用。
 * - GitHub Token などの機密情報は EncryptedSharedPreferences (暗号化) へ物理的に隔離。
 * - 初回起動時に DataStore(平文) から暗号化ストレージへの自動移行を実行します。
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @Named("SecureStorage") private val secureStorage: SharedPreferences
) {
    // 既存のプロセス寿命に合わせたスコープ
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        // 非機密データ (DataStore)
        val filterFolderIdKey = longPreferencesKey("filter_folder_id")
        val lastQueryKey = stringPreferencesKey("last_query")

        // 旧・機密データ (DataStore / 平文保存されていたキー)
        val legacyGithubTokenKey = stringPreferencesKey("github_token")

        // 新・機密データ (EncryptedSharedPreferences)
        const val SECURE_GITHUB_TOKEN = "secure_github_token"
    }

    // --- GitHub Token State Management ---
    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    init {
        // 1. DataStore から暗号化ストレージへのマイグレーションを実行
        performMigration()

        // 2. 暗号化ストレージから現在のトークンを読み込み StateFlow を初期化
        _githubToken.value = secureStorage.getString(Keys.SECURE_GITHUB_TOKEN, "") ?: ""
    }

    // ─────────────────────────────────────────────────────────────
    // 一般設定 (DataStore Preferences)
    // ─────────────────────────────────────────────────────────────

    /** 現在のフォルダ絞り込みID */
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

    /** 検索クエリ */
    val lastQuery: Flow<String> = dataStore.data
        .map { prefs -> prefs[Keys.lastQueryKey] ?: "" }
        .distinctUntilChanged()

    suspend fun setLastQuery(q: String) {
        dataStore.edit { prefs ->
            prefs[Keys.lastQueryKey] = q
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GitHub Token (Encrypted Storage)
    // ─────────────────────────────────────────────────────────────

    /** * GitHub Token を安全に保存
     * ※ 同時にアプリ内の StateFlow も更新し、即時反映を担保します。
     */
    fun setGithubToken(token: String) {
        secureStorage.edit().putString(Keys.SECURE_GITHUB_TOKEN, token).apply()
        _githubToken.value = token
    }

    /** ログアウト時などのトークン削除 */
    fun clearGithubToken() {
        secureStorage.edit().remove(Keys.SECURE_GITHUB_TOKEN).apply()
        _githubToken.value = ""
    }

    // ─────────────────────────────────────────────────────────────
    // Migration Logic
    // ─────────────────────────────────────────────────────────────

    /**
     * データ移行シーケンス
     * 平文で保存されていたトークンを検知し、暗号化ストレージへ移動させた後、
     * DataStore からは跡形もなく消去します。
     */
    private fun performMigration() {
        scope.launch {
            val prefs = dataStore.data.first()
            val legacyToken = prefs[Keys.legacyGithubTokenKey]

            if (!legacyToken.isNullOrEmpty()) {
                // 1. 暗号化ストレージへ安全にコピー
                setGithubToken(legacyToken)

                // 2. DataStore 側の平文データを消去 (Wipe)
                dataStore.edit { it.remove(Keys.legacyGithubTokenKey) }

                // システムログ風の演出用（必要に応じて）
                // println("[BugMemo] Security Migration Completed: Token moved to EncryptedStorage.")
            }
        }
    }
}