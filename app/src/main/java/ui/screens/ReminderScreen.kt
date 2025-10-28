package com.example.placely.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.ReminderViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyDateTime
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel = koinViewModel()
) {
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())

    val currentTime = System.currentTimeMillis()
    val upcomingReminders = reminders.filter { it.dateTime >= currentTime }.sortedBy { it.dateTime }
    val pastReminders = reminders.filter { it.dateTime < currentTime }.sortedByDescending { it.dateTime }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditReminder.createRouteWithArgs(0))
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Reminder") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        if (reminders.isEmpty()) {
            ModernEmptyState(paddingValues, navController)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Upcoming Section
                if (upcomingReminders.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Upcoming",
                            icon = Icons.Default.Schedule,
                            count = upcomingReminders.size
                        )
                    }

                    items(upcomingReminders, key = { it.id }) { reminder ->
                        ModernReminderCard(
                            reminder = reminder,
                            navController = navController,
                            isPast = false
                        )
                    }
                }

                // Past Section
                if (pastReminders.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Deadline Over",
                            icon = Icons.Default.History,
                            count = pastReminders.size
                        )
                    }

                    items(pastReminders, key = { it.id }) { reminder ->
                        ModernReminderCard(
                            reminder = reminder,
                            navController = navController,
                            isPast = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ModernEmptyState(paddingValues: PaddingValues, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Reminders Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add your first placement test or interview reminder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { navController.navigate(Screen.AddEditReminder.createRouteWithArgs(0)) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Reminder")
        }
    }
}

@Composable
fun ModernReminderCard(
    reminder: ReminderEntity,
    navController: NavController,
    isPast: Boolean = false
) {
    val typeColor = when (reminder.type) {
        "Online Test" -> Color(0xFF6366F1)
        "Interview" -> Color(0xFFEC4899)
        "Deadline" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val typeIcon = when (reminder.type) {
        "Online Test" -> Icons.Default.Computer
        "Interview" -> Icons.Default.Person
        "Deadline" -> Icons.Default.Alarm
        else -> Icons.Default.Event
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                try {
                    navController.navigate(Screen.ViewReminder.createRouteWithArgs(reminder.id))
                } catch (e: Exception) {
                    navController.navigate(Screen.AddEditReminder.createRouteWithArgs(reminder.id))
                }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = typeColor.copy(alpha = if (isPast) 0.1f else 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor.copy(alpha = if (isPast) 0.5f else 1f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            color = if (isPast)
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = typeColor.copy(alpha = if (isPast) 0.1f else 0.15f)
                        ) {
                            Text(
                                text = reminder.type,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = typeColor.copy(alpha = if (isPast) 0.6f else 1f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        navController.navigate(Screen.AddEditReminder.createRouteWithArgs(reminder.id))
                    }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isPast) 0.4f else 1f
                        )
                    )
                }
            }

            if (!reminder.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isPast) 0.4f else 1f
                    ),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(
                            alpha = if (isPast) 0.4f else 1f
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDate(reminder.dateTime),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (isPast) 0.4f else 1f
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.tertiary.copy(
                            alpha = if (isPast) 0.4f else 1f
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(reminder.dateTime),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (isPast) 0.4f else 1f
                        )
                    )
                }
            }
        }
    }
}

// Helper functions to format date and time
private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "No date"
    }
}

private fun formatTime(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "No time"
    }
}