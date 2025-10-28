package com.example.placely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.data.entity.TaskEntity
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.HomeViewModel
import com.example.placely.ui.viewmodel.NoteViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.LocalTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = koinViewModel(),
    noteViewModel: NoteViewModel = koinViewModel()
) {
    val quote by viewModel.quote.collectAsState()
    val upcomingReminders by viewModel.upcomingReminders.collectAsState(initial = emptyList())
    val pendingTasks by viewModel.pendingTasks.collectAsState(initial = emptyList())
    val motivationalTip by viewModel.motivationalTip.collectAsState()
    val allNotes by noteViewModel.notes.collectAsState(initial = emptyList())

    // Filter for TODAY's items only
    val todayReminders = remember(upcomingReminders) {
        upcomingReminders.filter { isToday(it.dateTime) }
    }

    val todayTasks = remember(pendingTasks) {
        pendingTasks.filter { task ->
            task.deadline?.let { isToday(it) } ?: false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Placely",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Greeting Header with Gradient
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GradientGreetingCard()
            }

            // Quote Card
            item {
                ModernQuoteCard(quote, motivationalTip)
            }

            // Quick Navigation Cards
            item {
                Text(
                    text = "Go To",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavigationCard(
                        modifier = Modifier.weight(1f),
                        title = "Reminders",
                        count = upcomingReminders.size,
                        icon = Icons.Default.Notifications,
                        color = Color(0xFF6366F1)
                    ) {
                        navController.navigate(Screen.Reminders.route)
                    }

                    NavigationCard(
                        modifier = Modifier.weight(1f),
                        title = "Tasks",
                        count = pendingTasks.size,
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFFEC4899)
                    ) {
                        navController.navigate(Screen.Tasks.route)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavigationCard(
                        modifier = Modifier.weight(1f),
                        title = "Notes",
                        count = allNotes.size,
                        icon = Icons.Default.Create,
                        color = Color(0xFF10B981)
                    ) {
                        navController.navigate(Screen.Notes.route)
                    }

                    NavigationCard(
                        modifier = Modifier.weight(1f),
                        title = "Calendar",
                        count = null,
                        icon = Icons.Default.DateRange,
                        color = Color(0xFFF59E0B)
                    ) {
                        navController.navigate(Screen.Calendar.route)
                    }
                }
            }

            // Today's Events Section
            if (todayReminders.isNotEmpty() || todayTasks.isNotEmpty()) {
                item {
                    Text(
                        text = "Due Today",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Today's Reminders - FIXED: Now goes to ViewReminder screen
            if (todayReminders.isNotEmpty()) {
                items(todayReminders) { reminder ->
                    TodayReminderCard(reminder) {
                        try {
                            // Navigate to VIEW screen (not edit)
                            navController.navigate(Screen.ViewReminder.createRouteWithArgs(reminder.id))
                        } catch (e: Exception) {
                            // Fallback to edit if view screen doesn't exist
                            navController.navigate(Screen.AddEditReminder.createRouteWithArgs(reminder.id))
                        }
                    }
                }
            }

            // Today's Tasks
            if (todayTasks.isNotEmpty()) {
                items(todayTasks) { task ->
                    TodayTaskCard(task) {
                        navController.navigate(Screen.AddEditTask.createRouteWithArgs(task.id))
                    }
                }
            }

            // Empty state for today
            if (todayReminders.isEmpty() && todayTasks.isEmpty()) {
                item {
                    EmptyTodayCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GradientGreetingCard() {
    val currentHour = remember { LocalTime.now().hour }
    val greeting = when (currentHour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val gradientColors = when (currentHour) {
        in 0..11 -> listOf(Color(0xFFFFA726), Color(0xFFFF7043))
        in 12..17 -> listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
        else -> listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = greeting,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready to conquer your goals?",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp),
                tint = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun ModernQuoteCard(quote: String, tip: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Tip",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NavigationCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (count != null) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TodayReminderCard(reminder: ReminderEntity, onClick: () -> Unit) {
    val isPast = reminder.dateTime < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPast) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
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
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPast) {
                    Color(0xFFEF4444).copy(alpha = 0.2f)
                } else {
                    Color(0xFF6366F1).copy(alpha = 0.2f)
                },
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = if (isPast) Color(0xFFEF4444) else Color(0xFF6366F1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description ?: reminder.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isPast) {
                    Text(
                        text = "⏰ Deadline Over",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reminder.dateTime.toFriendlyDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TodayTaskCard(task: TaskEntity, onClick: () -> Unit) {
    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFEF4444)
        "Medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { /* TODO */ },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Due Today",
                    style = MaterialTheme.typography.bodySmall,
                    color = priorityColor,
                    fontWeight = FontWeight.Medium
                )
            }

            if (task.priority.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = priorityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = task.priority,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTodayCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF10B981).copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nothing due today!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enjoy your free time 🎉",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to check if timestamp is today
private fun isToday(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val todayYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = timestamp
    val itemDay = calendar.get(Calendar.DAY_OF_YEAR)
    val itemYear = calendar.get(Calendar.YEAR)

    return today == itemDay && todayYear == itemYear
}