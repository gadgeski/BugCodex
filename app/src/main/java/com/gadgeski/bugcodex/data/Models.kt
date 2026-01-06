package com.gadgeski.bugcodex.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ドメインモデル兼Roomエンティティ
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,

    // Entity と揃える（未分類は null）
    @ColumnInfo(name = "folder_id")
    val folderId: Long?,

    val createdAt: Long,
    val updatedAt: Long,

    // v2 で追加（既定: 未スター）
    @ColumnInfo(name = "is_starred")
    val isStarred: Boolean = false,

    // ★ Added: Gist連携用情報
    @ColumnInfo(name = "image_paths")
    val imagePaths: List<String> = emptyList(), // ※以前の計画にあった画像用も定義しておきます

    @ColumnInfo(name = "gist_id")
    val gistId: String? = null, // GistのID (例: "aa5...")

    @ColumnInfo(name = "gist_url")
    val gistUrl: String? = null, // GistのURL
)

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
)

// NotesViewModel の seedIfEmpty で使用するヘルパークラス
data class SeedNote(
    val title: String,
    val content: String,
    val folderName: String?,
    val starred: Boolean,
)
