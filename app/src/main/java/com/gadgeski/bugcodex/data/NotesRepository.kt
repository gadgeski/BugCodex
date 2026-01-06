package com.gadgeski.bugcodex.data

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ──────────────────────────────────────────────────────────────
// Flow ベースのリポジトリIF
// ──────────────────────────────────────────────────────────────
interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    fun observeFolders(): Flow<List<Folder>>
    suspend fun getNote(id: Long): Note?
    suspend fun upsert(note: Note): Long
    suspend fun deleteNote(id: Long)
    suspend fun addFolder(name: String): Long
    suspend fun deleteFolder(id: Long)
    suspend fun setStarred(id: Long, starred: Boolean)

    // 件数の監視／単発取得 API
    fun observeNoteCount(): Flow<Long>
    fun observeFolderCount(): Flow<Long>
    suspend fun countNotes(): Long
    suspend fun countFolders(): Long
    suspend fun countNotesInFolder(folderId: Long): Long
    suspend fun getAllNotes(): List<Note>

    // バルク挿入
    suspend fun insertAllNotes(notes: List<Note>): List<Long>
    suspend fun insertAllFolders(folders: List<Folder>): List<Long>

    // ─────────── Paging 3 IF（デフォルト実装付き）───────────
    @Suppress("UNUSED_PARAMETER")
    fun pagedNotes(pageSize: Int = 30): Flow<PagingData<Note>> = observeNotes()
        .distinctUntilChanged()
        .map { list -> PagingData.from(list) }

    @Suppress("UNUSED_PARAMETER")
    fun pagedSearch(query: String, pageSize: Int = 30): Flow<PagingData<Note>> = searchNotes(query)
        .distinctUntilChanged()
        .map { list -> PagingData.from(list) }

    @Suppress("UNUSED_PARAMETER")
    fun pagedNotesByFolder(folderId: Long?, pageSize: Int = 30): Flow<PagingData<Note>> = observeNotes()
        .distinctUntilChanged()
        .map { list -> list.filter { folderId == null || it.folderId == folderId } }
        .map { filtered -> PagingData.from(filtered) }
}

/* ===============================
   ★ Repository の最小 API だけで動く “シード” ユーティリティ
   =============================== */
// ★ Fix: ここにあった data class SeedNote 定義を削除しました（Models.kt 側の定義を使用するため）

suspend fun NotesRepository.seedIfEmpty(
    folders: List<String> = listOf("Inbox"),
    notes: List<SeedNote> = emptyList(),
) {
    // 既存データがあれば終了（重複投入防止）
    val hasNotes = observeNotes().first().isNotEmpty()
    if (hasNotes) return

    // フォルダ投入
    val nameToId = mutableMapOf<String, Long>()
    for (name in folders.distinct()) {
        val id = addFolder(name)
        if (id != 0L) {
            nameToId[name] = id
        }
    }

    val now = System.currentTimeMillis()
    for (n in notes) {
        val folderId = n.folderName?.let { nameToId[it] }
        val insertedId = upsert(
            Note(
                id = 0L,
                title = n.title,
                content = n.content,
                folderId = folderId,
                createdAt = now,
                updatedAt = now,
                isStarred = n.starred,
            ),
        )
        if (n.starred && insertedId != 0L) {
            setStarred(insertedId, true)
        }
    }
}
