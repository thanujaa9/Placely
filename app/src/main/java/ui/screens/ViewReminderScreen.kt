package com.example.placely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.ReminderViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReminderScreen(
    navController: NavController,
    reminderId: Int,
    viewModel: ReminderViewModel = koinViewModel()
) {
    // Load the reminder when screen opens
    LaunchedEffect(reminderId) {
        if (reminderId > 0) {
            viewModel.loadReminder(reminderId)
        }
    }

    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val dateTime by viewModel.dateTime.collectAsState()
    val type by viewModel.type.collectAsState()
    val alert by viewModel.alert.collectAsState()

    val typeColor = when (type) {
        "Online Test" -> Color(0xFF6366F1)
        "Interview" -> Color(0xFFEC4899)
        "Deadline" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val typeIcon = when (type) {
        "Online Test" -> Icons.Default.Computer
        "Interview" -> Icons.Default.Person
        "Deadline" -> Icons.Default.Alarm
        else -> Icons.Default.Event
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.AddEditReminder.createRouteWithArgs(reminderId))
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Type Badge with Icon
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = typeColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = typeColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                }
            }

            // Title Card
            DetailCard(
                icon = Icons.Default.Title,
                label = "Title",
                value = title,
                iconColor = MaterialTheme.colorScheme.primary
            )

            // Description Card
            if (!description.isNullOrEmpty()) {
                DetailCard(
                    icon = Icons.Default.Description,
                    label = "Description",
                    value = description!!,
                    iconColor = MaterialTheme.colorScheme.secondary
                )
            }

            // Date & Time Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.DateRange,
                    label = "Date",
                    value = formatDate(dateTime),
                    iconColor = Color(0xFF6366F1)
                )

                DetailCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    value = formatTime(dateTime),
                    iconColor = Color(0xFFEC4899)
                )
            }

            // Alert Card
            DetailCard(
                icon = Icons.Default.NotificationsActive,
                label = "Reminder Alert",
                value = formatAlertTime(alert),
                iconColor = Color(0xFFF59E0B)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Delete Reminder?") },
            text = { Text("This action cannot be undone. Are you sure you want to delete this reminder?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReminder(navController.context)
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Helper functions
private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
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

private fun formatAlertTime(milliseconds: Long): String {
    val minutes = milliseconds / 60000
    return when {
        minutes < 60 -> "$minutes minutes before"
        minutes < 1440 -> "${minutes / 60} hour${if (minutes / 60 > 1) "s" else ""} before"
        else -> "${minutes / 1440} day${if (minutes / 1440 > 1) "s" else ""} before"
    }
}