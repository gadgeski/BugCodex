package com.gadgeski.bugcodex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gadgeski.bugcodex.data.prefs.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BugMemo Mode: Settings Logic Layer
 * SettingsRepository を Hilt で注入し、UI 状態を管理します。
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** GitHub Token のリアルタイム監視 */
    val githubToken: StateFlow<String> = settingsRepository.githubToken

    /** Token の保存処理 */
    fun updateGithubToken(token: String) {
        settingsRepository.setGithubToken(token)
    }

    /**
     * フィルタ設定の保存
     * * 注記: 現在の UI では未使用ですが、フォルダ機能の統合時に必要となるため保持します。
     */
    @Suppress("unused")
    fun updateFilterFolderId(id: Long?) {
        viewModelScope.launch {
            settingsRepository.setFilterFolderId(id)
        }
    }
}
