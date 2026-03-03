package com.gadgeski.bugcodex.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.gadgeski.bugcodex.data.NotesRepository
import com.gadgeski.bugcodex.data.RoomNotesRepository
import com.gadgeski.bugcodex.data.db.AppDatabase
import com.gadgeski.bugcodex.data.db.MindMapDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * BugMemo Mode: Core Application Module
 * システムの基盤となる Database、DataStore、および各 Repository の依存関係を定義します。
 */
@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object AppModule {

    private const val SETTINGS_DATASTORE_NAME = "settings"

    // ───────────── Database ─────────────

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.get(context as Application)

    @Provides
    @Singleton
    fun provideMindMapDao(db: AppDatabase): MindMapDao = db.mindMapDao()

    // ───────────── Storage (DataStore) ─────────────

    /**
     * SettingsRepository の構築に不可欠な DataStore インスタンスを提供します。
     */
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(SETTINGS_DATASTORE_NAME) },
    )

    // ───────────── Repositories ─────────────

    @Provides
    @Singleton
    fun provideNotesRepository(db: AppDatabase): NotesRepository = RoomNotesRepository(db.noteDao(), db.folderDao())

    /* * [CRITICAL FIX]
     * SettingsRepository は自ら @Inject constructor を持つため、
     * ここでの provideSettingsRepository メソッドは不要となりました。
     * 以前の .get() 呼び出しが Unresolved reference 'get' の原因です。
     * Hilt は provideDataStore と StorageModule からのリソースを使い、
     * 自動的に SettingsRepository を生成します。
     */
}
