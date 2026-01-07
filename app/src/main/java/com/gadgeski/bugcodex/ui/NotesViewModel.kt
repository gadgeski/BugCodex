@file:OptIn(
    kotlinx.coroutines.FlowPreview::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class,
)

package com.gadgeski.bugcodex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.gadgeski.bugcodex.data.Folder
import com.gadgeski.bugcodex.data.Note
import com.gadgeski.bugcodex.data.NotesRepository
import com.gadgeski.bugcodex.data.SeedNote
import com.gadgeski.bugcodex.data.prefs.SettingsRepository
import com.gadgeski.bugcodex.data.remote.GistRequest
import com.gadgeski.bugcodex.data.remote.GistService
import com.gadgeski.bugcodex.data.seedIfEmpty
import com.gadgeski.bugcodex.ui.utils.GistContentBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repo: NotesRepository,
    private val settings: SettingsRepository,
    private val gistService: GistService,
) : ViewModel() {

    // ─────────── UIイベント ───────────
    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
        data class UndoDelete(val text: String) : UiEvent
        data object NavigateToEditor : UiEvent
    }

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private fun sendEvent(event: UiEvent) {
        _events.trySend(event)
    }

    private var lastDeleted: Note? = null

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun setQuery(q: String) {
        _query.value = q
        viewModelScope.launch {
            runCatching { settings.setLastQuery(q) }
                .onFailure { e -> sendEvent(UiEvent.Message("検索語の保存に失敗しました: ${e.message}")) }
        }
    }

    private val debouncedQuery: Flow<String> =
        query.map { it.trim() }.debounce(250).distinctUntilChanged()

    private val _filterFolderId = MutableStateFlow<Long?>(null)
    val filterFolderId: StateFlow<Long?> = _filterFolderId.asStateFlow()

    fun setFolderFilter(id: Long?) {
        _filterFolderId.value = id
        viewModelScope.launch {
            runCatching { settings.setFilterFolderId(id) }
                .onFailure { e -> sendEvent(UiEvent.Message("絞り込みの保存に失敗しました: ${e.message}")) }
        }
    }

    init {
        viewModelScope.launch {
            settings.filterFolderId.collect { if (it != _filterFolderId.value) _filterFolderId.value = it }
        }
        viewModelScope.launch {
            settings.lastQuery.collect { if (it != _query.value) _query.value = it }
        }
    }

    private val pageSize = 50
    val pagedNotes: Flow<PagingData<Note>> =
        combine(debouncedQuery, filterFolderId) { q, folderId -> q to folderId }
            .flatMapLatest { (q, folderId) ->
                if (q.isBlank()) {
                    repo.pagedNotesByFolder(folderId = folderId, pageSize = pageSize)
                } else {
                    repo.pagedSearch(query = q, pageSize = pageSize)
                }
            }
            .cachedIn(viewModelScope)

    val folders: StateFlow<List<Folder>> =
        repo.observeFolders().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 編集中のノート（右ペインに表示されるノート）
    private val _editing = MutableStateFlow<Note?>(null)
    val editing: StateFlow<Note?> = _editing.asStateFlow()

    fun handleSharedText(sharedText: String) {
        if (sharedText.isBlank()) return
        newNote()
        setEditingContent(sharedText)

        val autoTitle = sharedText.trim().lineSequence().firstOrNull()?.take(30) ?: "Shared Note"
        setEditingTitle(if (sharedText.length > 30) "$autoTitle..." else autoTitle)

        // 2ペインモードの場合はナビゲーション不要だが、イベントは発行しておく（UI側で無視すれば良い）
        sendEvent(UiEvent.NavigateToEditor)
    }

    fun loadNote(id: Long) {
        viewModelScope.launch {
            runCatching { repo.getNote(id) }
                .onSuccess { _editing.value = it }
                .onFailure { sendEvent(UiEvent.Message("ノート読込に失敗しました")) }
        }
    }

    fun newNote() {
        val now = System.currentTimeMillis()
        _editing.value = Note(
            id = 0L,
            title = "",
            content = "",
            folderId = null,
            createdAt = now,
            updatedAt = now,
            isStarred = false,
            imagePaths = emptyList(),
        )
    }

    fun setEditingTitle(text: String) {
        _editing.update { it?.copy(title = text) }
    }
    fun setEditingContent(text: String) {
        _editing.update { it?.copy(content = text) }
    }
    fun setEditingFolder(folderId: Long?) {
        _editing.update { it?.copy(folderId = folderId) }
    }

    @Suppress("unused")
    fun addImagePath(path: String) {
        _editing.update { it?.copy(imagePaths = it.imagePaths + path) }
    }

    @Suppress("unused")
    fun removeImagePath(path: String) {
        _editing.update { it?.copy(imagePaths = it.imagePaths - path) }
    }

    fun saveEditing() {
        val note = _editing.value ?: return
        viewModelScope.launch {
            runCatching {
                val id = repo.upsert(note)
                if (note.id == 0L && id != 0L) _editing.value = note.copy(id = id)
                sendEvent(UiEvent.Message("保存しました"))
            }.onFailure { _ -> sendEvent(UiEvent.Message("保存に失敗しました")) }
        }
    }

    @Suppress("unused")
    fun deleteEditing() {
        val id = _editing.value?.id ?: return
        viewModelScope.launch {
            runCatching {
                lastDeleted = _editing.value
                repo.deleteNote(id)
                _editing.value = null
                sendEvent(UiEvent.UndoDelete("削除しました（元に戻す）"))
            }.onFailure { _ -> sendEvent(UiEvent.Message("削除に失敗しました")) }
        }
    }

    fun undoDelete() {
        val note = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching {
                repo.upsert(note.copy(id = 0L, updatedAt = System.currentTimeMillis()))
                sendEvent(UiEvent.Message("復元しました"))
            }.onFailure { _ -> sendEvent(UiEvent.Message("復元に失敗しました")) }
        }
    }

    fun toggleStar(noteId: Long, current: Boolean) {
        viewModelScope.launch {
            runCatching {
                repo.setStarred(noteId, !current)
                sendEvent(UiEvent.Message(if (current) "スターを外しました" else "スターを付けました"))
            }.onFailure { _ -> sendEvent(UiEvent.Message("スター変更に失敗しました")) }
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            runCatching { repo.addFolder(name) }
                .onSuccess { if (it != 0L) sendEvent(UiEvent.Message("フォルダを追加しました")) }
        }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch {
            runCatching {
                repo.deleteFolder(id)
                if (filterFolderId.value == id) _filterFolderId.value = null
                sendEvent(UiEvent.Message("フォルダを削除しました"))
            }
        }
    }

    @Suppress("unused")
    fun seedIfEmpty(folders: List<String> = listOf("Inbox"), notes: List<SeedNote> = emptyList()) {
        viewModelScope.launch {
            runCatching { repo.seedIfEmpty(folders, notes) }
        }
    }

    // 編集中のノートを個別に同期
    fun syncCurrentNoteToGist() {
        val note = _editing.value ?: return
        val token = settings.githubToken.value.trim()
        if (token.isBlank()) {
            sendEvent(UiEvent.Message("GitHub Tokenが設定されていません"))
            return
        }

        viewModelScope.launch {
            sendEvent(UiEvent.Message("Syncing to Gist..."))
            runCatching {
                withContext(Dispatchers.IO) {
                    val files = GistContentBuilder.buildSingleFileMap(note)
                    val description = GistContentBuilder.buildSyncDescription(isFullSync = false, title = note.title)

                    val request = GistRequest(
                        description = description,
                        public = false,
                        files = files,
                    )

                    val response = if (note.gistId == null) {
                        gistService.createGist("token $token", request)
                    } else {
                        try {
                            gistService.updateGist("token $token", note.gistId, request)
                        } catch (_: Exception) {
                            gistService.createGist("token $token", request)
                        }
                    }

                    val updatedNote = note.copy(
                        gistId = response.id,
                        gistUrl = response.htmlUrl,
                    )
                    repo.upsert(updatedNote)

                    _editing.value = updatedNote
                    response
                }
            }.onSuccess {
                sendEvent(UiEvent.Message("Sync Complete!"))
            }.onFailure { e ->
                e.printStackTrace()
                sendEvent(UiEvent.Message("Sync Failed: ${e.message}"))
            }
        }
    }

    // 全件同期
    fun syncToGist() {
        val token = settings.githubToken.value.trim()
        if (token.isBlank()) {
            sendEvent(UiEvent.Message("GitHub Tokenが設定されていません"))
            return
        }

        viewModelScope.launch {
            sendEvent(UiEvent.Message("Syncing all notes..."))
            runCatching {
                withContext(Dispatchers.IO) {
                    val allNotes = repo.observeNotes().first()
                    val files = GistContentBuilder.buildBatchFileMap(allNotes)
                    val description = GistContentBuilder.buildSyncDescription(isFullSync = true)

                    val request = GistRequest(
                        description = description,
                        public = false,
                        files = files,
                    )

                    val response = gistService.createGist("token $token", request)
                    response
                }
            }.onSuccess { response ->
                sendEvent(UiEvent.Message("Full Sync Success! ID: ${response.id}"))
            }.onFailure { e ->
                e.printStackTrace()
                sendEvent(UiEvent.Message("Full Sync Failed: ${e.message}"))
            }
        }
    }
}
