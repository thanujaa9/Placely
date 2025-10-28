package com.example.placely.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.placely.R
import com.example.placely.ui.screens.*

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    // Bottom Navigation Screens
    object Home : Screen("home", R.string.home_title, Icons.Filled.Home)
    object Reminders : Screen("reminders", R.string.reminders_title, Icons.Filled.Notifications)
    object Tasks : Screen("tasks", R.string.tasks_title, Icons.Filled.CheckCircle)
    object Notes : Screen("notes", R.string.notes_title, Icons.Filled.Create)
    object Calendar : Screen("calendar", R.string.calendar_title, Icons.Filled.DateRange)

    // Secondary routes (not in Bottom Navigation)
    object AddEditReminder : Screen("add_edit_reminder/{reminderId}", R.string.add_reminder_title, Icons.Filled.Add)
    object AddEditNote : Screen("add_edit_note/{noteId}", R.string.add_note_title, Icons.Filled.Edit)
    object ViewNote : Screen("view_note/{noteId}", R.string.view_note_title, Icons.Filled.Visibility)
    object ViewReminder : Screen("view_reminder/{reminderId}", R.string.view_reminder_title, Icons.Filled.Visibility)
    object AddEditTask : Screen("add_edit_task/{taskId}", R.string.add_task_title, Icons.Filled.Add)
    object Settings : Screen("settings", R.string.settings_title, Icons.Filled.Settings)

    // Functions for constructing routes with arguments
    fun createRouteWithArgs(id: Int): String {
        return when (this) {
            AddEditReminder -> "add_edit_reminder/$id"
            AddEditNote -> "add_edit_note/$id"
            ViewNote -> "view_note/$id"
            ViewReminder -> "view_reminder/$id"
            AddEditTask -> "add_edit_task/$id"
            else -> route
        }
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Reminders,
    Screen.Tasks,
    Screen.Notes,
    Screen.Calendar
)

@Composable
fun NavigationGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Main bottom navigation screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Reminders.route) {
            ReminderScreen(navController = navController)
        }

        composable(Screen.Tasks.route) {
            TaskScreen(navController = navController)
        }

        composable(Screen.Notes.route) {
            NoteScreen(navController = navController)
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }

        // Secondary screens (Add/Edit)
        composable(
            route = Screen.AddEditReminder.route,
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: 0
            AddEditReminderScreen(
                navController = navController,
                reminderId = reminderId
            )
        }

        composable(
            route = Screen.AddEditNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            AddEditNoteScreen(
                navController = navController,
                noteId = noteId
            )
        }

        composable(
            route = Screen.ViewNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            ViewNoteScreen(
                navController = navController,
                noteId = noteId
            )
        }

        composable(
            route = Screen.ViewReminder.route,
            arguments = listOf(
                navArgument("reminderId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: 0
            ViewReminderScreen(
                navController = navController,
                reminderId = reminderId
            )
        }

        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            AddEditTaskScreen(
                navController = navController,
                taskId = taskId
            )
        }

        // Settings screen (if needed later)
        composable(Screen.Settings.route) {
            // SettingsScreen(navController = navController)
        }
    }
}

@Composable
fun PlacelyBottomNavigation(navController: NavHostController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(stringResource(screen.resourceId)) },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}