@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gadgeski.bugcodex.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gadgeski.bugcodex.ui.navigation.AppNavHost
import com.gadgeski.bugcodex.ui.navigation.Routes
import com.gadgeski.bugcodex.ui.theme.IceCyan
import com.gadgeski.bugcodex.ui.theme.IceDeepNavy
import com.gadgeski.bugcodex.ui.theme.IceGlassSurface
import com.gadgeski.bugcodex.ui.theme.IceSilver
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppScaffold(
    vm: NotesViewModel,
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { e ->
            when (e) {
                is NotesViewModel.UiEvent.Message -> {
                    snackbarHostState.showSnackbar(
                        message = e.text,
                        withDismissAction = true,
                    )
                }
                is NotesViewModel.UiEvent.UndoDelete -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "削除しました",
                        actionLabel = "取り消す",
                        withDismissAction = true,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        vm.undoDelete()
                    }
                }
                // ★ Added: 共有インテント等によるエディタへの自動遷移
                is NotesViewModel.UiEvent.NavigateToEditor -> {
                    navController.navigate(Routes.EDITOR)
                }
            }
        }
    }

    val navItems = listOf(
        NavItem("Notes", Routes.ALL_NOTES) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Notes")
        },
        NavItem("Bugs", Routes.BUGS) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Bugs")
        },
        NavItem("Search", Routes.SEARCH) {
            Icon(Icons.Filled.Search, contentDescription = "Search")
        },
        NavItem("Folders", Routes.FOLDERS) {
            Icon(Icons.Filled.Folder, contentDescription = "Folders")
        },
    )

    val bottomBarRoutes = setOf(Routes.BUGS, Routes.SEARCH, Routes.FOLDERS, Routes.ALL_NOTES)
    val showBottomBar = shouldShowBottomBar(currentDestination, bottomBarRoutes)

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = IceDeepNavy,
                    contentColor = IceSilver,
                    tonalElevation = 0.dp,
                ) {
                    navItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = { if (!selected) navigateTopLevel(item.route) },
                            icon = { item.icon() },
                            label = { Text(item.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IceCyan,
                                selectedTextColor = IceCyan,
                                indicatorColor = IceGlassSurface,
                                unselectedIconColor = IceSilver,
                                unselectedTextColor = IceSilver,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            vm = vm,
            modifier = Modifier.padding(innerPadding).imePadding(),
        )
    }
}

private data class NavItem(val label: String, val route: String, val icon: @Composable () -> Unit)

private fun shouldShowBottomBar(destination: NavDestination?, bottomBarRoutes: Set<String>): Boolean {
    if (destination == null) return true
    return destination.hierarchy.any { it.route in bottomBarRoutes }
}
