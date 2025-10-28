package com.example.placely.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.placely.data.entity.ReminderEntity
import com.example.placely.data.entity.TaskEntity
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.ReminderViewModel
import com.example.placely.ui.viewmodel.TaskViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyTime
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    reminderViewModel: ReminderViewModel = koinViewModel(),
    taskViewModel: TaskViewModel = koinViewModel()
) {
    val reminders by reminderViewModel.reminders.collectAsState(initial = emptyList())
    val tasks by taskViewModel.tasks.collectAsState(initial = emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = {
                        currentMonth = YearMonth.now()
                        selectedDate = LocalDate.now()
                    }) {
                        Icon(Icons.Default.Today, contentDescription = "Today")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Month navigation
            MonthNavigation(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Divider()

            // Calendar grid
            CalendarGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                reminders = reminders,
                tasks = tasks,
                onDateSelected = { selectedDate = it }
            )

            Divider()

            // Events for selected date
            selectedDate?.let { date ->
                DayEventsSection(
                    date = date,
                    reminders = reminders,
                    tasks = tasks,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun MonthNavigation(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    reminders: List<ReminderEntity>,
    tasks: List<TaskEntity>,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.padding(8.dp)) {
        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar dates
        val firstDayOfMonth = currentMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        val weeks = (firstDayOfWeek + daysInMonth + 6) / 7

        for (week in 0 until weeks) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                for (dayOfWeek in 0..6) {
                    val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNumber)
                        val hasEvents = hasEventsOnDate(date, reminders, tasks)
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        CalendarDay(
                            day = dayNumber,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEvents = hasEvents,
                            onClick = { onDateSelected(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun DayEventsSection(
    date: LocalDate,
    reminders: List<ReminderEntity>,
    tasks: List<TaskEntity>,
    navController: NavController
) {
    val dayReminders = reminders.filter { reminder ->
        val reminderDate = java.time.Instant.ofEpochMilli(reminder.dateTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        reminderDate == date
    }

    val dayTasks = tasks.filter { task ->
        task.deadline?.let {
            val taskDate = java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            taskDate == date
        } ?: false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (dayReminders.isEmpty() && dayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events for this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show reminders
                items(dayReminders) { reminder ->
                    ReminderEventCard(reminder = reminder, navController = navController)
                }

                // Show tasks
                items(dayTasks) { task ->
                    TaskEventCard(task = task, navController = navController)
                }
            }
        }
    }
}

@Composable
fun ReminderEventCard(reminder: ReminderEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.AddEditReminder.createRouteWithArgs(reminder.id))
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${reminder.dateTime.toFriendlyTime()} • ${reminder.type}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun TaskEventCard(task: TaskEntity, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.AddEditTask.createRouteWithArgs(task.id))
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${task.priority} Priority ${if (task.isCompleted) "• Completed" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

fun hasEventsOnDate(
    date: LocalDate,
    reminders: List<ReminderEntity>,
    tasks: List<TaskEntity>
): Boolean {
    val hasReminder = reminders.any { reminder ->
        val reminderDate = java.time.Instant.ofEpochMilli(reminder.dateTime)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        reminderDate == date
    }

    val hasTask = tasks.any { task ->
        task.deadline?.let {
            val taskDate = java.time.Instant.ofEpochMilli(it)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            taskDate == date
        } ?: false
    }

    return hasReminder || hasTask
}