package com.gadgeski.bugcodex.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.gadgeski.bugcodex.data.db.FolderDao
import com.gadgeski.bugcodex.data.db.NoteDao
import kotlinx.coroutines.flow.Flow

class RoomNotesRepository(
    private val notes: NoteDao,
    private val folders: FolderDao,
) : NotesRepository {

    override fun observeNotes(): Flow<List<Note>> = notes.observeNotes()

    override fun searchNotes(query: String): Flow<List<Note>> = notes.search("%$query%")

    override fun observeFolders(): Flow<List<Folder>> = folders.observeFolders()

    override suspend fun getNote(id: Long): Note? = notes.getById(id)

    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            notes.insert(
                note.copy(
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        } else {
            notes.update(
                note.copy(
                    updatedAt = now,
                ),
            )
            note.id
        }
    }

    override suspend fun deleteNote(id: Long) {
        notes.getById(id)?.let { notes.delete(it) }
    }

    override suspend fun addFolder(name: String): Long = folders.insert(Folder(name = name))

    override suspend fun deleteFolder(id: Long) {
        folders.delete(Folder(id = id, name = ""))
    }

    override suspend fun setStarred(id: Long, starred: Boolean) {
        val now = System.currentTimeMillis()
        notes.updateStarred(id = id, starred = starred, updatedAt = now)
    }

    override fun observeNoteCount(): Flow<Long> = notes.observeNoteCount()
    override fun observeFolderCount(): Flow<Long> = folders.observeFolderCount()
    override suspend fun countNotes(): Long = notes.countNotes()
    override suspend fun countFolders(): Long = folders.countFolders()
    override suspend fun countNotesInFolder(folderId: Long): Long = notes.countNotesInFolder(folderId)

    override suspend fun insertAllNotes(notes: List<Note>): List<Long> {
        val now = System.currentTimeMillis()
        val entities = notes.map { n ->
            n.copy(
                createdAt = if (n.createdAt == 0L) now else n.createdAt,
                updatedAt = if (n.updatedAt == 0L) now else n.updatedAt,
            )
        }
        return this.notes.insertAll(entities)
    }

    override suspend fun getAllNotes(): List<Note> = notes.getAllNotes()

    override suspend fun insertAllFolders(folders: List<Folder>): List<Long> = this.folders.insertAll(folders)

    // ─────────── Paging 3 ───────────

    override fun pagedNotesByFolder(
        folderId: Long?,
        pageSize: Int,
    ): Flow<PagingData<Note>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize / 2,
        ),
        pagingSourceFactory = {
            notes.pagingSourceByFolder(folderId)
        },
    ).flow

    override fun pagedNotes(pageSize: Int): Flow<PagingData<Note>> = pagedNotesByFolder(folderId = null, pageSize = pageSize)

    override fun pagedSearch(query: String, pageSize: Int): Flow<PagingData<Note>> = Pager(
        config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = pageSize * 2,
            prefetchDistance = pageSize / 2,
        ),
        pagingSourceFactory = {
            // ★ Fix: 高速な FTS 検索を使用する
            // FTS の MATCH クエリ用として、末尾に * (ワイルドカード) を付けて前方一致対応にする
            // クエリが空の場合は全件表示などを別途ハンドリングしても良いが、VM側で制御済み想定
            val ftsQuery = "$query*"
            notes.pagingSourceFts(ftsQuery)
        },
    ).flow
}
