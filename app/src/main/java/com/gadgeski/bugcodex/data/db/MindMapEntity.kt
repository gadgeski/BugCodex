package com.gadgeski.bugcodex.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mind_map_nodes")
data class MindMapEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "parent_id") val parentId: Long?,
    // ★ Added: 連携するノートのID（Nullなら連携なし）
    @ColumnInfo(name = "note_id") val noteId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
