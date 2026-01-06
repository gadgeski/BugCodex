package com.gadgeski.bugcodex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gadgeski.bugcodex.ui.NotesViewModel
import com.gadgeski.bugcodex.ui.components.TwoPaneNoteEditor
import com.gadgeski.bugcodex.ui.mindmap.MindMapViewModel
import com.gadgeski.bugcodex.ui.screens.AllNotesScreen
import com.gadgeski.bugcodex.ui.screens.BugsScreen
import com.gadgeski.bugcodex.ui.screens.FoldersScreen
import com.gadgeski.bugcodex.ui.screens.MindMapScreen
import com.gadgeski.bugcodex.ui.screens.NoteEditorScreen
import com.gadgeski.bugcodex.ui.screens.SearchScreen
import com.gadgeski.bugcodex.ui.screens.SettingsScreen
import com.gadgeski.bugcodex.util.HingePosture
import com.gadgeski.bugcodex.util.rememberHingePosture

// keep: ルート定義
object Routes {
    const val BUGS = "bugs"
    const val SEARCH = "search"
    const val FOLDERS = "folders"
    const val EDITOR = "editor"
    const val MINDMAP = "mindmap"
    const val SETTINGS = "settings"
    const val ALL_NOTES = "all_notes"
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    vm: NotesViewModel,
) {
    // Foldableの状態を監視
    val hingePosture by rememberHingePosture()
    val isBookMode = hingePosture == HingePosture.BOOK_MODE

    NavHost(
        navController = navController,
        startDestination = Routes.ALL_NOTES,
        modifier = modifier,
    ) {
        // 一覧（Bugs）
        composable(Routes.BUGS) {
            BugsScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenSearch = { navController.navigateTopLevel(Routes.SEARCH) },
                onOpenFolders = { navController.navigateTopLevel(Routes.FOLDERS) },
                onOpenMindMap = { navController.navigate(Routes.MINDMAP) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenAllNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }
        // 検索
        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }
        // フォルダ
        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }
        // エディタ
        composable(Routes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                onBack = { navController.navigateUp() },
            )
        }
        // MindMap
        composable(Routes.MINDMAP) {
            val mindVm: MindMapViewModel = hiltViewModel()
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
                // Note: MindMapScreen 側に onOpenNote パラメータを追加した場合はここで設定します
                // onOpenNote = { noteId -> ... }
            )
        }
        // 設定
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                // ★ Fix: SettingsScreen 側の定義変更に合わせて vm 引数を削除しました
            )
        }
        // ALL_NOTES (Home)
        composable(Routes.ALL_NOTES) {
            // Fold対応: Book Mode (半開き) なら2画面エディタを表示
            if (isBookMode) {
                TwoPaneNoteEditor(
                    vm = vm,
                    modifier = Modifier,
                )
            } else {
                // 通常時 (スマホ/全開) はリストを表示し、タップで遷移
                AllNotesScreen(
                    vm = vm,
                    onOpenEditor = { navController.navigate(Routes.EDITOR) },
                )
            }
        }
    }
}

// keep: トップレベル遷移ヘルパ
private fun NavHostController.navigateTopLevel(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(this@navigateTopLevel.graph.findStartDestination().id) {
            saveState = true
        }
    }
}
