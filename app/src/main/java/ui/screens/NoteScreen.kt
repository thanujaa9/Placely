package com.example.placely.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.placely.data.entity.NoteEntity
import com.example.placely.navigation.Screen
import com.example.placely.ui.viewmodel.NoteViewModel
import com.example.placely.util.DateTimeUtil.toFriendlyDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    navController: NavController,
    viewModel: NoteViewModel = koinViewModel()
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }

    // Separate pinned and unpinned notes
    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val unpinnedNotes = remember(notes) { notes.filter { !it.isPinned } }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = showSearchBar,
                label = "search_animation"
            ) { isSearching ->
                if (isSearching) {
                    NoteSearchBar(
                        query = searchQuery,
                        onQueryChange = viewModel::setSearchQuery,
                        onClose = {
                            showSearchBar = false
                            viewModel.clearSearch()
                        }
                    )
                } else {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "My Notes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                )
                                if (notes.isNotEmpty()) {
                                    Text(
                                        "${notes.size} note${if (notes.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                            }) {
                                Icon(
                                    if (viewMode == ViewMode.GRID) Icons.Default.Menu else Icons.Default.Apps,
                                    contentDescription = "Toggle view"
                                )
                            }
                            IconButton(onClick = { showSearchBar = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.resetFormState()
                    navController.navigate(Screen.AddEditNote.createRouteWithArgs(0))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Note", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { paddingValues ->
        if (notes.isEmpty() && searchQuery.isEmpty()) {
            EmptyNoteState(paddingValues, navController)
        } else if (notes.isEmpty()) {
            SearchEmptyState(paddingValues)
        } else {
            when (viewMode) {
                ViewMode.GRID -> NotesGridView(
                    paddingValues = paddingValues,
                    pinnedNotes = pinnedNotes,
                    unpinnedNotes = unpinnedNotes,
                    navController = navController,
                    viewModel = viewModel
                )
                ViewMode.LIST -> NotesListView(
                    paddingValues = paddingValues,
                    pinnedNotes = pinnedNotes,
                    unpinnedNotes = unpinnedNotes,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

enum class ViewMode {
    GRID, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Close")
            }

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun NotesGridView(
    paddingValues: PaddingValues,
    pinnedNotes: List<NoteEntity>,
    unpinnedNotes: List<NoteEntity>,
    navController: NavController,
    viewModel: NoteViewModel
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        // Pinned notes section
        if (pinnedNotes.isNotEmpty()) {
            item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                Text(
                    text = "PINNED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(pinnedNotes, key = { it.id }) { note ->
                AnimatedNoteCard(
                    note = note,
                    onNoteClick = {
                        navController.navigate(Screen.ViewNote.createRouteWithArgs(note.id))
                    },
                    onTogglePin = { viewModel.toggleNotePin(note) }
                )
            }
        }

        // Regular notes section
        if (unpinnedNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item(span = androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan.FullLine) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ALL NOTES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(unpinnedNotes, key = { it.id }) { note ->
                AnimatedNoteCard(
                    note = note,
                    onNoteClick = {
                        navController.navigate(Screen.ViewNote.createRouteWithArgs(note.id))
                    },
                    onTogglePin = { viewModel.toggleNotePin(note) }
                )
            }
        }
    }
}

@Composable
fun NotesListView(
    paddingValues: PaddingValues,
    pinnedNotes: List<NoteEntity>,
    unpinnedNotes: List<NoteEntity>,
    navController: NavController,
    viewModel: NoteViewModel
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 8.dp
    ) {
        if (pinnedNotes.isNotEmpty()) {
            item {
                Text(
                    text = "PINNED",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(pinnedNotes, key = { it.id }) { note ->
                ListNoteCard(
                    note = note,
                    onNoteClick = {
                        navController.navigate(Screen.ViewNote.createRouteWithArgs(note.id))
                    },
                    onTogglePin = { viewModel.toggleNotePin(note) }
                )
            }
        }

        if (unpinnedNotes.isNotEmpty()) {
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ALL NOTES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(unpinnedNotes, key = { it.id }) { note ->
                ListNoteCard(
                    note = note,
                    onNoteClick = {
                        navController.navigate(Screen.ViewNote.createRouteWithArgs(note.id))
                    },
                    onTogglePin = { viewModel.toggleNotePin(note) }
                )
            }
        }
    }
}

@Composable
fun AnimatedNoteCard(
    note: NoteEntity,
    onNoteClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    val colors = listOf(
        Color(0xFFFFF9C4), Color(0xFFE1BEE7), Color(0xFFB2DFDB),
        Color(0xFFFFCCBC), Color(0xFFC5E1A5), Color(0xFFB3E5FC)
    )
    val cardColor = colors[note.id % colors.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    color = Color.Black.copy(alpha = 0.87f)
                )

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (note.isPinned) "Unpin" else "Pin",
                        tint = if (note.isPinned) Color(0xFFFFA000) else Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.7f),
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = note.timestamp.toFriendlyDateTime(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ListNoteCard(
    note: NoteEntity,
    onNoteClick: () -> Unit,
    onTogglePin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = note.timestamp.toFriendlyDateTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = if (note.isPinned) "Unpin" else "Pin",
                    tint = if (note.isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyNoteState(paddingValues: PaddingValues, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Create,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Notes Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start capturing your placement preparation notes, interview questions, and important concepts",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate(Screen.AddEditNote.createRouteWithArgs(0))
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Your First Note", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SearchEmptyState(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Notes Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try searching with different keywords",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}