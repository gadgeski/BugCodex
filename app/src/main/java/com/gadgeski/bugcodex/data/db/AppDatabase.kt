package com.gadgeski.bugcodex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gadgeski.bugcodex.data.Folder
import com.gadgeski.bugcodex.data.Note

// ★ Fix: NoteFts::class を追加しないと NoteDao のクエリが失敗します
@Database(
    entities = [Note::class, Folder::class, MindMapEntity::class, NoteFts::class],
    version = 4, // ★ Fix: テーブル構成が変わるのでバージョンを上げる
    exportSchema = true,
)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun mindMapDao(): MindMapDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            val newInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bugmemo_db",
            )
                .fallbackToDestructiveMigration(true)
                .build()
            instance = newInstance
            newInstance
        }
    }
}
