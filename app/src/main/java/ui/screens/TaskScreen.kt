package com.example.placely.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.placely.data.entity.TaskEntity
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.TaskViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyDate
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    navController: NavController,
    viewModel: TaskViewModel = koinViewModel()
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val showCompleted by viewModel.showCompletedTasks.collectAsState()

    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tasks",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    // Filter button with badge
                    BadgedBox(
                        badge = {
                            if (completedTasks.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        completedTasks.size.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { viewModel.toggleShowCompleted() }) {
                            Icon(
                                imageVector = if (showCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = if (showCompleted) "Hide completed" else "Show completed",
                                tint = if (showCompleted) {
                                    Color(0xFF4CAF50)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditTask.createRouteWithArgs(0))
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Task", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            )
        }
    ) { paddingValues ->
        if (tasks.isEmpty()) {
            ModernEmptyTaskState(paddingValues, navController)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Statistics Card
                item {
                    StatisticsCard(
                        totalTasks = tasks.size,
                        completedTasks = completedTasks.size,
                        pendingTasks = pendingTasks.size
                    )
                }

                // Pending Tasks Section
                if (pendingTasks.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Pending",
                            count = pendingTasks.size,
                            icon = Icons.Default.PlayArrow
                        )
                    }

                    items(pendingTasks, key = { it.id }) { task ->
                        ModernTaskCard(
                            task = task,
                            onTaskClick = {
                                navController.navigate(Screen.AddEditTask.createRouteWithArgs(task.id))
                            },
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(task)
                            }
                        )
                    }
                }

                // Completed Tasks Section
                if (showCompleted && completedTasks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Completed",
                            count = completedTasks.size,
                            icon = Icons.Default.CheckCircle
                        )
                    }

                    items(completedTasks, key = { it.id }) { task ->
                        ModernTaskCard(
                            task = task,
                            onTaskClick = {
                                navController.navigate(Screen.AddEditTask.createRouteWithArgs(task.id))
                            },
                            onToggleComplete = {
                                viewModel.toggleTaskCompletion(task)
                            }
                        )
                    }
                }

                // Bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(
    totalTasks: Int,
    completedTasks: Int,
    pendingTasks: Int
) {
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$completedTasks of $totalTasks tasks completed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (progress == 1f) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            color = if (progress == 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (progress == 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    label = "Pending",
                    value = pendingTasks,
                    color = Color(0xFFFFA726)
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    label = "Done",
                    value = completedTasks,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ModernTaskCard(
    task: TaskEntity,
    onTaskClick: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFFF6B6B)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom Checkbox
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onToggleComplete() },
                shape = CircleShape,
                color = if (task.isCompleted) {
                    Color(0xFF4CAF50).copy(alpha = 0.2f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = if (!task.isCompleted) {
                    androidx.compose.foundation.BorderStroke(
                        2.dp,
                        MaterialTheme.colorScheme.outline
                    )
                } else null
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (task.isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority Badge
                    if (task.priority == "High" && !task.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = priorityColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "High",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = priorityColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Deadline
                    if (task.deadline != null) {
                        val isOverdue = task.deadline!! < System.currentTimeMillis() && !task.isCompleted
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOverdue) {
                                Color(0xFFFF6B6B).copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = if (isOverdue) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = formatDeadline(task.deadline!!),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModernEmptyTaskState(paddingValues: PaddingValues, navController: NavController) {
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Tasks Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start organizing your placement preparation by adding tasks",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate(Screen.AddEditTask.createRouteWithArgs(0))
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Task", fontWeight = FontWeight.SemiBold)
        }
    }
}

// Helper function to format deadline
private fun formatDeadline(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "No date"
    }
}