package com.gadgeski.bugcodex.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
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
import com.gadgeski.bugcodex.ui.utils.HingePosture
import com.gadgeski.bugcodex.ui.utils.rememberHingePosture

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
    val hingePosture by rememberHingePosture()

    // CHANGED: Configuration.screenWidthDp -> LocalWindowInfo.current.containerSize
    val containerSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val windowWidthDp = with(density) { containerSize.width.toDp() }

    val isTwoPane = hingePosture == HingePosture.BOOK_MODE || windowWidthDp >= 600.dp

    NavHost(
        navController = navController,
        startDestination = Routes.ALL_NOTES,
        modifier = modifier.fillMaxSize(),
    ) {
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

        composable(Routes.SEARCH) {
            SearchScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }

        composable(Routes.FOLDERS) {
            FoldersScreen(
                vm = vm,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenNotes = { navController.navigateTopLevel(Routes.ALL_NOTES) },
            )
        }

        composable(Routes.EDITOR) {
            NoteEditorScreen(
                vm = vm,
                onBack = { navController.navigateUp() },
            )
        }

        composable(Routes.MINDMAP) {
            val mindVm: MindMapViewModel = hiltViewModel()
            MindMapScreen(
                onClose = { navController.navigateUp() },
                vm = mindVm,
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.navigateUp() })
        }

        composable(Routes.ALL_NOTES) {
            if (isTwoPane) {
                TwoPaneNoteEditor(vm = vm)
            } else {
                AllNotesScreen(
                    vm = vm,
                    onOpenEditor = { navController.navigate(Routes.EDITOR) },
                )
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    this.navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(this@navigateTopLevel.graph.findStartDestination().id) {
            saveState = true
        }
    }
}
