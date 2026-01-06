// app/src/androidTest/java/com/example/bugmemo/data/db/NoteDaoTest.kt
package com.gadgeski.bugcodex.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ★ Added: 最小の Room DAO テスト
 * - In-Memory DB を使い、ディスクには書きません
 * - 検証点:
 *    1) insert → getById
 *    2) updateStarred → getById
 *    3) delete → getById(null)
 *    4) observeNotes（Flow）が挿入反映を返す
 *
 * 依存:
 *  - `androidTestImplementation(libs.androidx.junit)` など既存でOK
 *  - 追加依存は不要（runBlocking と Flow.first は kotlin-stdlib/coroutines で使えます）
 */
@RunWith(AndroidJUnit4::class)
class NoteDaoTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var folderDao: FolderDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries() // ★ Added: テスト簡略化のため許可
                .build()
        noteDao = db.noteDao()
        folderDao = db.folderDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getById() = runBlocking {
        val now = System.currentTimeMillis()
        // ★ Added: 必要ならフォルダを作って紐付けてもよい（ここでは null）
        val id =
            noteDao.insert(
                NoteEntity(
                    title = "title",
                    content = "content",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false, // ★ 重要: Entity に isStarred がある前提
                ),
            )

        val loaded = noteDao.getById(id)
        assertNotNull(loaded)
        assertEquals("title", loaded!!.title)
        assertEquals(false, loaded.isStarred)
    }

    @Test
    fun updateStarred_and_getById() = runBlocking {
        val now = System.currentTimeMillis()
        val id =
            noteDao.insert(
                NoteEntity(
                    title = "star target",
                    content = "",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false,
                ),
            )

        // ★ Added: スター更新の部分更新APIを検証
        val later = now + 1000
        noteDao.updateStarred(id = id, starred = true, updatedAt = later)

        val loaded = noteDao.getById(id)
        assertNotNull(loaded)
        assertTrue(loaded!!.isStarred)
        assertEquals(later, loaded.updatedAt)
    }

    @Test
    fun delete_and_getById_null() = runBlocking {
        val now = System.currentTimeMillis()
        val id =
            noteDao.insert(
                NoteEntity(
                    title = "to be deleted",
                    content = "",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false,
                ),
            )
        val before = noteDao.getById(id)
        assertNotNull(before)

        // ★ Added: 削除は Entity 指定
        noteDao.delete(before!!)
        val after = noteDao.getById(id)
        assertNull(after) // ★ 削除後は取得できない
    }

    @Test
    fun observeNotes_reflects_insert() = runBlocking {
        // ★ Added: Flow の初期値（空）
        val initial = noteDao.observeNotes().first()
        val baseSize = initial.size

        val now = System.currentTimeMillis()
        noteDao.insert(
            NoteEntity(
                title = "flow add",
                content = "",
                folderId = null,
                createdAt = now,
                updatedAt = now,
                isStarred = false,
            ),
        )

        // ★ Added: 次の first() で反映後のスナップショットを取る
        val afterInsert = noteDao.observeNotes().first()
        assertEquals(baseSize + 1, afterInsert.size)
        assertTrue(afterInsert.any { it.title == "flow add" })
    }

    @Test
    fun folders_basic_insert_observe() = runBlocking {
        val initial = folderDao.observeFolders().first()
        val baseSize = initial.size

        val newId = folderDao.insert(FolderEntity(name = "Kotlin"))
        assertTrue(newId > 0L)

        val after = folderDao.observeFolders().first()
        assertEquals(baseSize + 1, after.size)
        assertTrue(after.any { it.name == "Kotlin" })
    }
}
