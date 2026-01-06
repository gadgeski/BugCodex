package com.gadgeski.bugcodex.di

import android.app.Application
import android.content.Context
import com.gadgeski.bugcodex.data.NotesRepository
import com.gadgeski.bugcodex.data.RoomNotesRepository
import com.gadgeski.bugcodex.data.db.AppDatabase
import com.gadgeski.bugcodex.data.db.MindMapDao
import com.gadgeski.bugcodex.data.prefs.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
// ★ Fix: Hiltの生成コードからの参照をIDEが誤検知するのを防ぐために警告を抑制
@Suppress("unused")
object AppModule {

    // ───────────── Database ─────────────
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.get(context as Application)

    // ★ Added: MindMapDao を提供
    @Provides
    @Singleton
    fun provideMindMapDao(db: AppDatabase): MindMapDao = db.mindMapDao()

    // ───────────── Repositories ─────────────

    @Provides
    @Singleton
    fun provideNotesRepository(db: AppDatabase): NotesRepository = RoomNotesRepository(db.noteDao(), db.folderDao())

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository = SettingsRepository.get(context as Application)
}
