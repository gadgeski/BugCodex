package com.gadgeski.bugcodex.data.db

// 必要最小の import のみ（ワイルドカード・重複なし）
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ★ Removed: Fts4 / FtsOptions の import（FTS は NoteFts.kt に集約）

/* ===============================
   Folder
   =============================== */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true) val name: String,
    // 既存どおり。name は @ColumnInfo(index=true) で単一インデックスを付与済み。
    // 必要に応じて UNIQUE 制約をかけたい場合は、@Entity(indices=[Index(value=["name"], unique=true)]) に変更。
)

/* ===============================
   Note（通常テーブル）
   - フォルダ絞り込み・並び替え・スター切替の頻出を想定し、補助インデックスを付与
   =============================== */
@Entity(
    tableName = "notes",
    indices = [
        Index("folderId"),
        // 既存：フォルダ絞り込み用
        Index("updatedAt"),
        // ★ 更新日の降順表示が多いため（ORDER BY 最適化）
        Index("isStarred"),
        // ★ スター付きフィルタ用
        // 必要に応じて title 先頭一致などを高速化したい場合は Index("title") を検討（LIKE '%...' には効かない）
    ],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    // 未分類は null（外部キー未設定）
    val folderId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    // v2 で追加した列。DB では 0/1、Kotlin 側は Boolean。
    @ColumnInfo(name = "isStarred", defaultValue = "0")
    val isStarred: Boolean = false,
)

// ★ Removed: NoteFtsEntity（FTS 定義は NoteFts.kt へ移動・一本化）
