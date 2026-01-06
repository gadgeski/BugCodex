package com.gadgeski.bugcodex.data.db

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import com.gadgeski.bugcodex.data.Note

/**
 * 全文検索 (FTS4) 用の仮想テーブル定義
 * - contentEntity = Note::class を指定することで、
 * Note テーブルと連動してインデックスが自動更新されます。
 */

@Fts4(
    contentEntity = Note::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
)
@Entity(tableName = "notesFts")
data class NoteFts(
    val title: String,
    val content: String,
)
