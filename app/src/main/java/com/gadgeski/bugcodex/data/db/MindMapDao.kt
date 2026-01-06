package com.gadgeski.bugcodex.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MindMapDao {
    @Query("SELECT * FROM mind_map_nodes")
    fun getAllNodes(): Flow<List<MindMapEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: MindMapEntity): Long

    @Update
    suspend fun update(node: MindMapEntity)

    @Query("DELETE FROM mind_map_nodes WHERE id = :id")
    suspend fun delete(id: Long)

    // 指定したノートIDを持つノードの連携を解除する（ノート削除時などに使用）
    @Query("UPDATE mind_map_nodes SET note_id = NULL WHERE note_id = :noteId")
    suspend fun unlinkNote(noteId: Long)
}
