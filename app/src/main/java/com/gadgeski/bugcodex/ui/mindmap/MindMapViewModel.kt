package com.gadgeski.bugcodex.ui.mindmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gadgeski.bugcodex.data.Note
import com.gadgeski.bugcodex.data.NotesRepository
import com.gadgeski.bugcodex.data.db.MindMapDao
import com.gadgeski.bugcodex.data.db.MindMapEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MindNode(
    val id: Long,
    val title: String,
    val parentId: Long? = null,
    // 連携ノートID
    val noteId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@HiltViewModel
class MindMapViewModel @Inject constructor(
    private val dao: MindMapDao,
    // ノート作成用
    private val notesRepo: NotesRepository,
) : ViewModel() {

    // DB からのデータを監視して MindNode に変換
    val nodes: StateFlow<List<MindNode>> = dao.getAllNodes()
        .map { list ->
            list.map { entity ->
                MindNode(
                    id = entity.id,
                    title = entity.title,
                    parentId = entity.parentId,
                    noteId = entity.noteId,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 削除Undo用の一時保存（DB削除前にメモリに退避）
    private var lastDeletedSnapshot: List<MindMapEntity> = emptyList()

    fun addRootNode(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insert(MindMapEntity(title = t, parentId = null, createdAt = now, updatedAt = now))
        }
    }

    fun addChildNode(parentId: Long, title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dao.insert(MindMapEntity(title = t, parentId = parentId, createdAt = now, updatedAt = now))
        }
    }

    fun renameNode(id: Long, newTitle: String) {
        val t = newTitle.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            // 現在の状態から対象を探す（簡易実装）
            val target = nodes.value.find { it.id == id } ?: return@launch
            val entity = MindMapEntity(
                id = target.id,
                title = t,
                parentId = target.parentId,
                noteId = target.noteId,
                createdAt = target.createdAt,
                updatedAt = System.currentTimeMillis(),
            )
            dao.update(entity)
        }
    }

    fun deleteNode(id: Long) {
        val all = nodes.value
        // メモリ上で子孫を特定して一括削除
        val toDeleteIds = collectWithChildren(all, id)

        // Undo用に退避（Entityに戻す）
        lastDeletedSnapshot = all.filter { it.id in toDeleteIds }.map {
            MindMapEntity(it.id, it.title, it.parentId, it.noteId, it.createdAt, it.updatedAt)
        }

        viewModelScope.launch {
            toDeleteIds.forEach { dao.delete(it) }
        }
    }

    fun undoDelete(): Boolean {
        if (lastDeletedSnapshot.isEmpty()) return false
        viewModelScope.launch {
            // IDを維持して再インサート
            lastDeletedSnapshot.forEach { dao.insert(it) }
            lastDeletedSnapshot = emptyList()
        }
        return true
    }

    // このノードの内容で新規ノートを作成し、紐付ける
    fun createNoteFromNode(nodeId: Long) {
        viewModelScope.launch {
            val node = nodes.value.find { it.id == nodeId } ?: return@launch
            if (node.noteId != null) return@launch
            // 既に紐付いている

            // 1. ノート作成
            val note = Note(
                id = 0,
                title = node.title,
                content = "# ${node.title}\n\nCreated from MindMap.",
                folderId = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isStarred = false,
            )
            val newNoteId = notesRepo.upsert(note)

            // 2. ノードに紐付け更新
            val entity = MindMapEntity(
                id = node.id,
                title = node.title,
                parentId = node.parentId,
                noteId = newNoteId, // Link!
                createdAt = node.createdAt,
                updatedAt = System.currentTimeMillis(),
            )
            dao.update(entity)
        }
    }

    // フラットリスト変換（再帰ロジックは維持）
    fun flatList(): List<Pair<MindNode, Int>> {
        val currentNodes = nodes.value
        val children = currentNodes.groupBy { it.parentId }
        val result = mutableListOf<Pair<MindNode, Int>>()

        fun dfs(parentId: Long?, depth: Int) {
            val list = children[parentId].orEmpty().sortedBy { it.createdAt }
            list.forEach { n ->
                result += n to depth
                dfs(n.id, depth + 1)
            }
        }
        dfs(null, 0)
        return result
    }

    private fun collectWithChildren(all: List<MindNode>, rootId: Long): Set<Long> {
        val children = all.groupBy { it.parentId }
        val bag = linkedSetOf<Long>()
        fun rec(id: Long) {
            bag += id
            children[id].orEmpty().forEach { rec(it.id) }
        }
        rec(rootId)
        return bag
    }

    fun canUndoDelete(): Boolean = lastDeletedSnapshot.isNotEmpty()
}
