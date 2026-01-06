// app/src/main/java/com/gadgeski/bugmemo/core/AppLocaleManager.kt
package com.gadgeski.bugcodex.core

// ★ keep: アプリ内から言語を切り替える最小ユーティリティ（DataStore + AppCompatDelegate）
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// ★ フォントスケール用キー: (androidx.datastore.preferences.core.floatPreferencesKey)

private const val PREFS_NAME = "app_settings"
private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

// ★ keep: 空文字は「システムに追従」、それ以外は BCP47 の言語タグ（例: "ja", "en"）
object AppLocaleManager {

    private val KEY_LANGUAGE_TAG = stringPreferencesKey("language_tag")

    // ★ Added: エディタ文字スケール（1.0f=等倍）
    private val KEY_EDITOR_FONT_SCALE = floatPreferencesKey("editor_font_scale")
    // ★ Added

    fun languageTagFlow(context: Context): Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_LANGUAGE_TAG] ?: "" }

    suspend fun setLanguage(context: Context, languageTag: String) {
        // ★ keep: DataStore に保存
        context.dataStore.edit { it[KEY_LANGUAGE_TAG] = languageTag }

        // ★ keep: 反映（即時）。空文字はシステムに追従。
        val locales = if (languageTag.isBlank()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
        // ※ Activity 再生成は呼び出し側で必要に応じて行ってください
    }

    // --------------------------------------
    // ★ Added: エディタ文字スケール（購読・保存）
    // --------------------------------------

    // ★ Added: スケールの購読（未設定は 1.0f）
    fun editorFontScaleFlow(context: Context): Flow<Float> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[KEY_EDITOR_FONT_SCALE] ?: 1.0f }

    // ★ Added: スケールの保存（安全のため 0.75〜2.0 にクランプ）
    suspend fun setEditorFontScale(context: Context, scale: Float) {
        val clamped = scale.coerceIn(0.75f, 2.0f)
        // ★ Added
        context.dataStore.edit { it[KEY_EDITOR_FONT_SCALE] = clamped }
    }
}
