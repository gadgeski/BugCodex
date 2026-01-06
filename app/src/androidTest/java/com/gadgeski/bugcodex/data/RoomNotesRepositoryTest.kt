// app/src/androidTest/java/com/example/bugmemo/data/RoomNotesRepositoryTest.kt
package com.gadgeski.bugcodex.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gadgeski.bugcodex.data.db.AppDatabase
import com.gadgeski.bugcodex.data.db.FolderDao
import com.gadgeski.bugcodex.data.db.NoteDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ★ Added: RoomNotesRepository の最小テスト
 *  検証点:
 *   - upsert: 新規 → 採番IDが返る／更新 → 値が反映される
 *   - setStarred: スターのトグルが反映される
 *   - deleteNote: 削除後に取得できない
 */
@RunWith(AndroidJUnit4::class)
class RoomNotesRepositoryTest {
    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var noteDao: NoteDao
    private lateinit var folderDao: FolderDao

    // ★ Added: テスト対象
    private lateinit var repo: RoomNotesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                // ★ Added: テスト簡略化
                .allowMainThreadQueries()
                .build()
        noteDao = db.noteDao()
        folderDao = db.folderDao()
        repo = RoomNotesRepository(noteDao, folderDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_insert_then_update() = runBlocking {
        val now = System.currentTimeMillis()

        // ★ 新規作成（id=0L）→ upsert
        val newId =
            repo.upsert(
                Note(
                    id = 0L,
                    title = "first",
                    content = "content",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false,
                ),
            )
        assertTrue(newId > 0L)

        // 取得して確認
        val loaded1 = repo.getNote(newId)
        assertNotNull(loaded1)
        assertEquals("first", loaded1!!.title)
        assertEquals(false, loaded1.isStarred)

        // ★ 更新して上書き（id=採番済み）
        val updatedId =
            repo.upsert(
                loaded1.copy(
                    title = "updated",
                    content = "changed",
                ),
            )
        assertEquals(newId, updatedId)

        val loaded2 = repo.getNote(newId)
        assertNotNull(loaded2)
        assertEquals("updated", loaded2!!.title)
        assertEquals("changed", loaded2.content)
    }

    @Test
    fun setStarred_toggles_flag() = runBlocking {
        val now = System.currentTimeMillis()
        val id =
            repo.upsert(
                Note(
                    id = 0L,
                    title = "star",
                    content = "",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false,
                ),
            )
        // ★ true に変更
        repo.setStarred(id, true)
        val afterTrue = repo.getNote(id)
        assertNotNull(afterTrue)
        assertTrue(afterTrue!!.isStarred)

        // ★ false に戻す
        repo.setStarred(id, false)
        val afterFalse = repo.getNote(id)
        assertNotNull(afterFalse)
        assertFalse(afterFalse!!.isStarred)
    }

    @Test
    fun deleteNote_then_getNote_returns_null() = runBlocking {
        val now = System.currentTimeMillis()
        val id =
            repo.upsert(
                Note(
                    id = 0L,
                    title = "to delete",
                    content = "",
                    folderId = null,
                    createdAt = now,
                    updatedAt = now,
                    isStarred = false,
                ),
            )
        assertNotNull(repo.getNote(id))

        // ★ 削除
        repo.deleteNote(id)

        // 取得できないことを確認
        val after = repo.getNote(id)
        assertNull(after)
    }
}
