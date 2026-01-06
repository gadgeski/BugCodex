package com.gadgeski.bugcodex.data.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gadgeski.bugcodex.data.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Note?

    // Repositoryで FTS 版に切り替えたため、未使用なら削除しても良いですが
    // Flow版の検索として残す場合はこのまま
    @Query(
        """
        SELECT * FROM notes
        WHERE title LIKE :q OR content LIKE :q
        ORDER BY updatedAt DESC
        """,
    )
    fun search(q: String): Flow<List<Note>>

    // ─────────── PagingSource ──────────

    // フォルダ絞り込み（カラム名は folder_id）
    @Query(
        """
        SELECT * FROM notes
        WHERE (:folderId IS NULL OR folder_id = :folderId)
        ORDER BY updatedAt DESC
        """,
    )
    fun pagingSourceByFolder(folderId: Long?): PagingSource<Int, Note>

    // ★ Fix: 未使用になった pagingSourceLike を削除しました

    // FTS 高速検索
    @Query(
        """
        SELECT n.*
        FROM notes AS n
        JOIN notesFts AS fts ON fts.rowid = n.id
        WHERE notesFts MATCH :query
        ORDER BY n.updatedAt DESC
        """,
    )
    fun pagingSourceFts(query: String): PagingSource<Int, Note>

    // ─────────── CRUD ──────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Suppress("unused")
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(notes: List<Note>): List<Long>

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("UPDATE notes SET is_starred = :starred, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStarred(
        id: Long,
        starred: Boolean,
        updatedAt: Long,
    )

    @Suppress("unused")
    @Query("SELECT COUNT(*) FROM notes")
    fun observeNoteCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun countNotes(): Long

    @Suppress("unused")
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT COUNT(*) FROM notes WHERE folder_id = :folderId")
    suspend fun countNotesInFolder(folderId: Long): Long
}
