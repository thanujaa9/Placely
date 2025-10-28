package com.example.placely.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.placely.ui.viewmodel.ReminderViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyDate
import com.example.placely.util.DateTimeUtil.toFriendlyTime
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditReminderScreen(
    navController: NavController,
    reminderId: Int, // 0 for new, >0 for edit
    viewModel: ReminderViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val isEditMode = reminderId > 0

    // Load existing reminder data if in edit mode
    LaunchedEffect(reminderId) {
        if (isEditMode) {
            viewModel.loadReminder(reminderId)
        } else {
            viewModel.resetFormState()
        }
    }

    val titleState by viewModel.title.collectAsState()
    val descriptionState by viewModel.description.collectAsState()
    val dateTimeState by viewModel.dateTime.collectAsState()
    val typeState by viewModel.type.collectAsState()
    val alertState by viewModel.alert.collectAsState()

    // Dialog state management
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Reminder" else "Add New Reminder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Reminder")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Title Input
            OutlinedTextField(
                value = titleState,
                onValueChange = viewModel::setTitle,
                label = { Text("Reminder Title") },
                placeholder = { Text("E.g., TCS Online Test") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 2. Description/Comment Input - IMPROVED
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notification Message",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descriptionState ?: "",
                        onValueChange = { viewModel.setDescription(it.takeIf { it.isNotEmpty() }) },
                        label = { Text("Add note/comment (Optional)") },
                        placeholder = { Text("This will appear in your notification\nE.g., Bring resume, Practice DSA topics, Prepare for React questions...") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Helper text
                    if (descriptionState.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 Add important notes that will show when you get the reminder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // 3. Date and Time Pickers
            Text(
                "Date & Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Picker Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }
                ) {
                    OutlinedTextField(
                        value = dateTimeState.toFriendlyDate(),
                        onValueChange = {},
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Time Picker Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showTimePicker = true }
                ) {
                    OutlinedTextField(
                        value = dateTimeState.toFriendlyTime(),
                        onValueChange = {},
                        label = { Text("Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        readOnly = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // 4. Reminder Type Dropdown
            var expandedTypeMenu by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expandedTypeMenu,
                onExpandedChange = { expandedTypeMenu = it }
            ) {
                OutlinedTextField(
                    value = typeState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Reminder Type") },
                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedTypeMenu,
                    onDismissRequest = { expandedTypeMenu = false }
                ) {
                    listOf(
                        "Online Test",
                        "Interview",
                        "Resume Submission",
                        "Coding Contest",
                        "Deadline",
                        "Other"
                    ).forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.setType(type)
                                expandedTypeMenu = false
                            }
                        )
                    }
                }
            }

            // 5. Notification Alert with Chips
            Text(
                "Alert Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val alertOptions = mapOf(
                "No Alert" to 0L,
                "15 Min" to 900000L,
                "1 Hour" to 3600000L,
                "1 Day" to 86400000L
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                alertOptions.forEach { (label, value) ->
                    FilterChip(
                        selected = alertState == value,
                        onClick = { viewModel.setAlert(value) },
                        label = { Text(label) },
                        leadingIcon = if (alertState == value) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // Show preview of when notification will appear
            if (alertState > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You'll be notified ${getAlertTimeText(alertState)} before the event",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Save Button
            Button(
                onClick = {
                    viewModel.saveReminder(context)
                    navController.popBackStack()
                },
                enabled = titleState.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    if (isEditMode) Icons.Default.CheckCircle else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Update Reminder" else "Save Reminder",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    // --- Dialogs ---

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateTimeState.takeIf { it > 0 }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { newDateMillis ->
                            viewModel.setDateTimeDate(newDateMillis)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val time = Instant.ofEpochMilli(dateTimeState).atZone(ZoneId.systemDefault()).toLocalTime()
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateTimeTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete this reminder? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReminder(context)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// Helper function to get alert time text
private fun getAlertTimeText(alertMillis: Long): String {
    return when (alertMillis) {
        900000L -> "15 minutes"
        3600000L -> "1 hour"
        86400000L -> "1 day"
        else -> ""
    }
}