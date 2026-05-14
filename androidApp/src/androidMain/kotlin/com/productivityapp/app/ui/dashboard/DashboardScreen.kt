package com.productivityapp.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

import com.productivityapp.app.ui.tasks.TaskListScreen
import com.productivityapp.app.ui.reminders.ReminderScreen
import com.productivityapp.app.ui.notes.NotesScreen
import com.productivityapp.model.NoteItem
import com.productivityapp.app.ui.vault.VaultScreen
import com.productivityapp.app.ui.alarm.AlarmScreen
import com.productivityapp.app.ui.notes.NoteEditorScreen
import com.productivityapp.app.ui.tasks.TasksRepository
import com.productivityapp.app.ui.tasks.AddTaskModal
import com.productivityapp.model.Task
import com.productivityapp.model.TaskPriority
import com.productivityapp.model.TaskStatus
import com.productivityapp.model.TaskCategory
import com.productivityapp.app.ui.ai.AIScreen
import com.productivityapp.app.ui.ai.AIRepository
import com.productivityapp.model.Reminder
import com.productivityapp.model.RepeatInterval
import com.productivityapp.app.ui.reminders.AddReminderModal
import com.productivityapp.app.ui.reminders.RemindersRepository
import com.productivityapp.app.ui.alarm.AlarmEntryModal
import com.productivityapp.model.Alarm
import com.productivityapp.app.ui.vault.AddVaultItemModal
import com.productivityapp.app.ui.vault.VaultRepository
import com.productivityapp.app.ui.vault.VaultItem

sealed class ZenithScreen {
    object Dashboard : ZenithScreen()
    object Tasks : ZenithScreen()
    object AI : ZenithScreen()
    object Vault : ZenithScreen()
    object Notes : ZenithScreen()
    object NoteEditor : ZenithScreen()
    object Draw : ZenithScreen()
    object Alarm : ZenithScreen()
    object Reminder : ZenithScreen()
    object Settings : ZenithScreen()
}

object ProductivityTracker {
    val completionMap = mutableStateMapOf<java.time.LocalDate, Int>()

    fun recordCompletion(date: java.time.LocalDate = java.time.LocalDate.now()) {
        val current = completionMap[date] ?: 0
        completionMap[date] = current + 1
    }

    fun removeCompletion(date: java.time.LocalDate = java.time.LocalDate.now()) {
        val current = completionMap[date] ?: 0
        if (current > 0) completionMap[date] = current - 1
    }

    fun getCompletionsForDate(date: java.time.LocalDate): Int {
        return completionMap[date] ?: 0
    }
}

@Composable
fun DashboardScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var isRightSidebarVisible by remember { mutableStateOf(false) }
    var isQuickActionsVisible by remember { mutableStateOf(false) }
    var showAddTaskModal by remember { mutableStateOf(false) }
    var showAddReminderModal by remember { mutableStateOf(false) }
    var showAddAlarmModal by remember { mutableStateOf(false) }
    var showAddVaultModal by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf<ZenithScreen>(ZenithScreen.Dashboard) }
    var selectedNote by remember { mutableStateOf<NoteItem?>(null) }
    // Custom Drawer State
    var isLeftSidebarVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            )
    ) {
        val isEditor = currentScreen == ZenithScreen.NoteEditor
        
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                if (!isEditor) {
                    ZenithTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onMenuClick = { isLeftSidebarVisible = true },
                        onUserClick = { isRightSidebarVisible = true }
                    )
                }
            },
            bottomBar = {
                if (!isEditor) {
                    ZenithBottomNav(
                        selectedTab = when(currentScreen) {
                            ZenithScreen.Dashboard -> 0
                            ZenithScreen.AI -> 1
                            ZenithScreen.Tasks -> 2
                            ZenithScreen.Settings -> 3
                            else -> 0
                        },
                        onTabSelected = { index ->
                            currentScreen = when(index) {
                                0 -> ZenithScreen.Dashboard
                                1 -> ZenithScreen.AI
                                2 -> ZenithScreen.Tasks
                                3 -> ZenithScreen.Settings
                                else -> currentScreen
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isEditor && currentScreen != ZenithScreen.Settings && currentScreen != ZenithScreen.AI) {
                    FloatingActionButton(
                        onClick = { isQuickActionsVisible = true },
                        backgroundColor = Color(0xFF818CF8),
                        contentColor = Color.White,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            },
            backgroundColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isEditor) PaddingValues(0.dp) else paddingValues)
                    .consumeWindowInsets(if (isEditor) PaddingValues(0.dp) else paddingValues)
            ) {
                when (currentScreen) {
                    ZenithScreen.Dashboard -> MainContent(onNavigate = { currentScreen = it })
                    ZenithScreen.Tasks -> TaskListScreen()
                    ZenithScreen.Vault -> VaultScreen()
                    ZenithScreen.Notes -> NotesScreen(
                        onNoteClick = { 
                            selectedNote = it
                            currentScreen = ZenithScreen.NoteEditor
                        },
                        onAddClick = {
                            selectedNote = null
                            currentScreen = ZenithScreen.NoteEditor
                        }
                    )
                    ZenithScreen.NoteEditor -> NoteEditorScreen(
                        initialNote = selectedNote,
                        onBack = { 
                            selectedNote = null
                            currentScreen = ZenithScreen.Notes 
                        }
                    )
                    ZenithScreen.AI -> AIScreen()
                    ZenithScreen.Alarm -> AlarmScreen()
                    ZenithScreen.Draw -> PlaceholderScreen("Draw")
                    ZenithScreen.Reminder -> ReminderScreen()
                    ZenithScreen.Settings -> PlaceholderScreen("Settings")
                }
            }
        }
        
        // Custom Left Sidebar Overlay (Refined Layered Animation)
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Scrim (Fades in place)
            AnimatedVisibility(
                visible = isLeftSidebarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isLeftSidebarVisible = false }
                )
            }

            // 2. Sidebar Panel (Slides in)
            AnimatedVisibility(
                visible = isLeftSidebarVisible,
                enter = slideInHorizontally(initialOffsetX = { -it }),
                exit = slideOutHorizontally(targetOffsetX = { -it })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(260.dp),
                    color = Color(0xFF0F172A),
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                ) {
                    LeftSidebarContent(
                        currentScreen = currentScreen,
                        onNavigate = { screen ->
                            currentScreen = screen
                            isLeftSidebarVisible = false
                        }
                    )
                }
            }
        }

        // Modals & Dialogs
        if (isQuickActionsVisible) {
            QuickActionsModal(
                onClose = { isQuickActionsVisible = false },
                onAction = { action ->
                    isQuickActionsVisible = false
                    when(action) {
                        "Task" -> { showAddTaskModal = true }
                        "Note" -> { currentScreen = ZenithScreen.NoteEditor }
                        "Vault" -> { showAddVaultModal = true }
                        "Alarm" -> { showAddAlarmModal = true }
                        "Draw" -> currentScreen = ZenithScreen.Draw
                        "Reminder" -> { showAddReminderModal = true }
                    }
                }
            )
        }

        if (showAddReminderModal) {
            AddReminderModal(
                onDismiss = { showAddReminderModal = false },
                onSave = { title: String, date: String, time: String, category: String, priority: String, repeatInterval: RepeatInterval, description: String ->
                    RemindersRepository.addReminder(title, date, time, category, priority, repeatInterval = repeatInterval, description = description)
                    showAddReminderModal = false
                }
            )
        }

        if (showAddAlarmModal) {
            AlarmEntryModal(
                onDismiss = { showAddAlarmModal = false },
                onSave = { time: String, label: String, days: List<String>, isVibrate: Boolean, escalationType: String, sound: String ->
                    // Logic to add alarm if repository exists
                    showAddAlarmModal = false
                }
            )
        }

        if (showAddTaskModal) {
            AddTaskModal(
                initialCategory = "Work",
                onDismiss = { showAddTaskModal = false },
                onTaskCreated = { title, cat, prio, desc, dueDate, dueTime, hasReminder, hasAlarm ->
                    TasksRepository.addTask(title, cat, prio, desc, dueDate, dueTime, hasReminder, hasAlarm)
                    showAddTaskModal = false
                }
            )
        }
        if (showAddVaultModal) {
            AddVaultItemModal(
                onDismiss = { showAddVaultModal = false },
                onAdd = { item ->
                    VaultRepository.addVaultItem(item)
                    showAddVaultModal = false
                }
            )
        }

        // Right Sidebar / Drawer Overlay
        if (isRightSidebarVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isRightSidebarVisible = false }
            )
        }

        AnimatedVisibility(
            visible = isRightSidebarVisible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            RightSidebarContent(
                currentScreen = currentScreen,
                onNavigate = { screen ->
                    currentScreen = screen
                    isRightSidebarVisible = false
                },
                onClose = { isRightSidebarVisible = false }
            )
        }

    }
}

@Composable
fun QuickActionsModal(onClose: () -> Unit, onAction: (String) -> Unit) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onClose,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.80f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF818CF8), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                val actions = listOf(
                    Triple(Icons.Default.Edit, "Note", { onAction("Note") }),
                    Triple(Icons.Default.CheckCircle, "Task", { onAction("Task") }),
                    Triple(Icons.Default.Lock, "Vault", { onAction("Vault") }),
                    Triple(Icons.Default.Brush, "Draw", { onAction("Draw") }),
                    Triple(Icons.Default.NotificationsActive, "Alarm", { onAction("Alarm") }),
                    Triple(Icons.Default.Notifications, "Reminder", { onAction("Reminder") })
                )
                
                val colors = listOf(
                    Color(0xFF818CF8), // Note (Indigo)
                    Color(0xFF818CF8), // Task (Indigo)
                    Color(0xFF22C55E), // Vault (Green)
                    Color(0xFF60A5FA), // Draw (Blue)
                    Color(0xFFFBBF24), // Alarm (Gold)
                    Color(0xFF818CF8)  // Reminder (Indigo)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val chunks = actions.chunked(3)
                    chunks.forEachIndexed { i, rowActions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowActions.forEachIndexed { j, action ->
                                val color = colors[i * 3 + j]
                                ActionItem(action.first, action.second, color, action.third)
                            }
                            if (rowActions.size < 3) {
                                repeat(3 - rowActions.size) { Spacer(modifier = Modifier.width(70.dp)) }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(10.dp),
                    elevation = null
                ) {
                    Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddEntryDialog(title: String, label: String, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF818CF8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ActionItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "$name Screen", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = "Coming soon...", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ZenithTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp), // Compact padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Search Bar (Compact)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp) // Shorter height
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconCustom(Icons.Default.Search, contentDescription = null, tint = Color.Gray, size = 16.dp)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search...", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = onUserClick, modifier = Modifier.size(40.dp)) {
            Icon(
                Icons.Default.Person,
                contentDescription = "User",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ZenithBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    BottomNavigation(
        backgroundColor = Color(0xFF0F172A), // Match background
        contentColor = Color.White,
        elevation = 0.dp, // Flat look
        modifier = Modifier
            .navigationBarsPadding()
            .height(56.dp)
    ) {
        BottomNavItem(icon = Icons.Default.Home, label = "Home", selected = selectedTab == 0, onClick = { onTabSelected(0) })
        BottomNavItem(icon = Icons.Default.Star, label = "AI", selected = selectedTab == 1, onClick = { onTabSelected(1) })
        BottomNavItem(icon = Icons.Default.Done, label = "Tasks", selected = selectedTab == 2, onClick = { onTabSelected(2) })
        BottomNavItem(icon = Icons.Default.Settings, label = "Settings", selected = selectedTab == 3, onClick = { onTabSelected(3) })
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    BottomNavigationItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 10.sp) },
        selected = selected,
        onClick = onClick,
        selectedContentColor = Color(0xFF818CF8),
        unselectedContentColor = Color.Gray
    )
}

@Composable
fun MainContent(onNavigate: (ZenithScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Zenith",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Compact Horizontal Calendar / Activity Track Switcher
        CalendarSection()
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Compact AI Nudge
        // New Chat Option on Dashboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF818CF8).copy(alpha = 0.15f), Color(0xFF818CF8).copy(alpha = 0.05f))),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { 
                    AIRepository.createNewSession()
                    onNavigate(ZenithScreen.AI)
                }
                .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF818CF8).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("New AI Conversation", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Start a fresh secure chat", color = Color.Gray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tasks",
            color = Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        val tasks = com.productivityapp.app.ui.tasks.TasksRepository.tasks
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No tasks for today", color = Color.Gray.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tasks.take(3).forEach { task ->
                    DashboardTaskRow(task)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 3x3 Module Grid Launcher
        ModuleGrid(onNavigate = onNavigate)
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ModuleGrid(onNavigate: (ZenithScreen) -> Unit) {
    val modules = listOf(
        Triple(Icons.Default.Done, "Tasks", ZenithScreen.Tasks),
        Triple(Icons.Default.Edit, "Notes", ZenithScreen.Notes),
        Triple(Icons.Default.Lock, "Vault", ZenithScreen.Vault),
        Triple(Icons.Default.NotificationsActive, "Alarms", ZenithScreen.Alarm),
        Triple(Icons.Default.Schedule, "Reminders", ZenithScreen.Reminder),
        Triple(Icons.Default.Star, "AI Nexus", ZenithScreen.AI),
        Triple(Icons.Default.Brush, "Draw", ZenithScreen.Draw),
        Triple(Icons.Default.Settings, "Settings", ZenithScreen.Settings),
        Triple(Icons.Default.Dashboard, "Home", ZenithScreen.Dashboard)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("MODULES", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        val rows = modules.chunked(3)
        rows.forEach { rowModules ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowModules.forEach { (icon, label, screen) ->
                    Box(modifier = Modifier.weight(1f)) {
                        ModuleGridItem(icon, label) { onNavigate(screen) }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleGridItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(85.dp).clickable { onClick() },
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DashboardTaskRow(task: com.productivityapp.model.Task) {
    val isDone = task.status == com.productivityapp.model.TaskStatus.DONE
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (isDone) Color(0xFF818CF8) else Color.Transparent)
                .border(1.dp, if (isDone) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = task.title, 
            color = if (isDone) Color.Gray else Color.White, 
            fontSize = 13.sp, 
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(task.category.name, color = Color(0xFF818CF8).copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarSection() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Global Slider Switcher (Outside the cards)
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(32.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                val pillWidth = 77.dp
                val offset by animateDpAsState(
                    targetValue = if (pagerState.currentPage == 1) pillWidth else 0.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )

                Box(
                    modifier = Modifier
                        .offset(x = offset)
                        .width(pillWidth)
                        .fillMaxHeight()
                        .background(Color(0xFF818CF8), RoundedCornerShape(8.dp))
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    listOf("Calendar", "Track").forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Text(
                if (pagerState.currentPage == 0) {
                    val now = java.time.LocalDate.now()
                    "${now.month.name.lowercase().capitalize()} ${now.year}"
                } else "Activity Heatmap", 
                color = Color.White.copy(alpha = 0.4f), 
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp) // Fixed height to prevent layout jumps
                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (page == 0) {
                    HorizontalCalendar()
                } else {
                    ActivityHeatmap()
                }
            }
        }
    }
}

@Composable
fun HorizontalCalendar() {
    val now = java.time.LocalDate.now()
    val yearMonth = java.time.YearMonth.from(now)
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0, Monday = 1...
    
    // Convert to Monday start for our UI (Mo=0, Tu=1... Su=6)
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1 + 7) % 7

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            days.forEach { day ->
                Text(day, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(34.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Dynamic Grid
        var dateCounter = 1
        val rows = 6
        val cols = 7
        
        for (i in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (j in 0 until cols) {
                    val dayIndex = i * cols + j
                    val dateValue = if (dayIndex < startOffset || dateCounter > daysInMonth) {
                        null
                    } else {
                        dateCounter++
                    }
                    
                    val isToday = dateValue == now.dayOfMonth && now.month == yearMonth.month && now.year == yearMonth.year
                    
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isToday) Color(0xFF818CF8) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dateValue != null) {
                            Text(
                                text = dateValue.toString(),
                                color = if (isToday) Color.White else Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
            if (dateCounter > daysInMonth && i >= 4) break // Skip last row if month ended
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun ActivityHeatmap() {
    var selectedYear by remember { mutableStateOf("2026") }
    val years = listOf("2024", "2025", "2026", "Stats")
    
    // In a real app, this would come from a database/repository
    // For now, we simulate with a map of dates to completion counts
    val taskCompletions = remember { mutableStateMapOf<java.time.LocalDate, Int>() }
    
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val scrollState = rememberScrollState()
    
    LaunchedEffect(selectedYear) {
        if (selectedYear != "Stats") {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Year Switcher (Pill Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            years.forEach { year ->
                val isSelected = selectedYear == year
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (isSelected) Color(0xFFF97316).copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedYear = year },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = year,
                        color = if (isSelected) Color(0xFFF97316) else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedYear == "Stats") {
            HeatmapStats()
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Fixed Day Labels
                Column(
                    modifier = Modifier.padding(top = 22.dp, end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp) // Fine-tuned for 14dp squares + 4dp spacing
                ) {
                    listOf("", "Mon", "", "Wed", "", "Fri", "").forEach { day ->
                        Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.CenterStart) {
                            Text(day, color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        months.forEach { month ->
                            Text(month, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        // 53 weeks
                        val now = java.time.LocalDate.now()
                        for (week in 0 until 53) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (day in 0 until 7) {
                                    val daysToSubtract = (52 - week) * 7 + (6 - day)
                                    val date = now.minusDays(daysToSubtract.toLong())
                                    val completions = ProductivityTracker.getCompletionsForDate(date)
                                    
                                    val color = when {
                                        completions == 0 -> Color.White.copy(alpha = 0.05f)
                                        completions == 1 -> Color(0xFFF97316).copy(alpha = 0.2f)
                                        completions == 2 -> Color(0xFFF97316).copy(alpha = 0.4f)
                                        completions == 3 -> Color(0xFFF97316).copy(alpha = 0.7f)
                                        else -> Color(0xFFF97316)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(color, RoundedCornerShape(3.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeatmapStats() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatRow("Total Tasks Done", "124", Color(0xFFF97316))
        StatRow("Daily Average", "4.2", Color(0xFFF97316).copy(alpha = 0.6f))
        StatRow("Best Streak", "12 Days", Color(0xFF22C55E))
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProductivityStatsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Hero Progress Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Lifetime Progress", color = Color.Gray, fontSize = 9.sp)
                    Text("Level 14", color = Color(0xFF818CF8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                    Box(modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(Color(0xFF818CF8), CircleShape))
                }
            }
        }

        // Dense Stat Grid
        val stats = listOf(
            Triple("Total Work", "1,284", Icons.Default.CheckCircle),
            Triple("Streak", "12d", Icons.Default.Whatshot),
            Triple("Avg Daily", "8.4", Icons.Default.Timer),
            Triple("Best Year", "2025", Icons.Default.EmojiEvents),
            Triple("Accuracy", "94%", Icons.AutoMirrored.Filled.TrendingUp),
            Triple("Efficiency", "High", Icons.Default.Speed)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.take(3).forEach { (label, value, icon) ->
                    CompactStatCard(label, value, icon, Color(0xFF818CF8))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                stats.drop(3).forEach { (label, value, icon) ->
                    CompactStatCard(label, value, icon, Color(0xFF34D399))
                }
            }
        }
    }
}

@Composable
fun CompactStatCard(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.04f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Column {
                Text(text = label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LeftSidebarContent(currentScreen: ZenithScreen, onNavigate: (ZenithScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compact Zenith Brand Header
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(28.dp).background(Color(0xFF818CF8), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Zenith", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 1. Dashboard (Same as it was)
        val isDashboard = currentScreen == ZenithScreen.Dashboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDashboard) Brush.horizontalGradient(listOf(Color(0xFF818CF8).copy(alpha = 0.2f), Color(0xFF818CF8).copy(alpha = 0.05f)))
                    else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.03f), Color.White.copy(alpha = 0.03f)))
                )
                .border(
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDashboard) Color(0xFF818CF8).copy(alpha = 0.3f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onNavigate(ZenithScreen.Dashboard) }
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Dashboard, 
                    contentDescription = null, 
                    tint = if (isDashboard) Color(0xFF818CF8) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Dashboard", 
                        color = Color.White, 
                        fontSize = 14.sp, 
                        fontWeight = if (isDashboard) FontWeight.Bold else FontWeight.Medium
                    )
                    Text("System Overview", color = Color.Gray, fontSize = 9.sp)
                }
                if (isDashboard) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF818CF8), CircleShape))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Chat History
        if (AIRepository.sessions.isNotEmpty()) {
            Text("AI HISTORY", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AIRepository.sessions.take(4).forEach { session ->
                    val isActive = AIRepository.currentSession.value.id == session.id && currentScreen == ZenithScreen.AI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) Color(0xFF818CF8).copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { 
                                AIRepository.switchSession(session)
                                onNavigate(ZenithScreen.AI)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline, 
                            contentDescription = null, 
                            tint = if (isActive) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = session.title,
                            color = if (isActive) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        // Push navigation grid to bottom
        Spacer(modifier = Modifier.weight(1f))
        
        // 3. Navigation Grid (Bottom Side, Top of Settings)
        Text("NAVIGATION", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        val navItems = listOf(
            Triple(Icons.Default.Edit, "Notes", Color(0xFF818CF8) to ZenithScreen.Notes),
            Triple(Icons.Default.CheckCircle, "Tasks", Color(0xFF818CF8) to ZenithScreen.Tasks),
            Triple(Icons.Default.Lock, "Vault", Color(0xFF22C55E) to ZenithScreen.Vault),
            Triple(Icons.Default.Brush, "Draw", Color(0xFF60A5FA) to ZenithScreen.Draw),
            Triple(Icons.Default.NotificationsActive, "Alarm", Color(0xFFFBBF24) to ZenithScreen.Alarm),
            Triple(Icons.Default.Schedule, "Reminders", Color(0xFF818CF8) to ZenithScreen.Reminder)
        )
        
        val rows = navItems.chunked(3)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { (icon, label, pair) ->
                    val (color, screen) = pair
                    val isActive = currentScreen == screen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(screen) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isActive) color else color.copy(alpha = 0.08f),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (isActive) color else color.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon, 
                                contentDescription = null, 
                                tint = if (isActive) Color.White else color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            color = if (isActive) Color.White else Color.Gray,
                            fontSize = 8.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 4. Settings
        SidebarItem(Icons.Default.Settings, "Settings", currentScreen == ZenithScreen.Settings) { onNavigate(ZenithScreen.Settings) }
        Spacer(modifier = Modifier.navigationBarsPadding())
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RightSidebarContent(currentScreen: ZenithScreen, onNavigate: (ZenithScreen) -> Unit, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp) // Narrower
            .background(Color(0xFF0F172A)) // Match theme
            .padding(20.dp)
            .clickable(enabled = false) {}
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(18.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Compact Profile Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF818CF8))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("User Name", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text("Level 5", color = Color.Gray, fontSize = 11.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text("STATS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        SidebarItem(Icons.Default.Star, "Achievements", false) { /* Action */ }
        SidebarItem(Icons.Default.Settings, "Account Settings", false) { /* Action */ }
        
        // Modules section removed (moved to left)
        Spacer(modifier = Modifier.height(32.dp))
        Text("SYSTEM", color = Color.Gray.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        SidebarItem(Icons.Default.CloudQueue, "Cloud Sync", false) { /* Action */ }
        SidebarItem(Icons.Default.Security, "Privacy", false) { /* Action */ }
    }
}

@Composable
fun SidebarItem(icon: ImageVector, label: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color(0xFF818CF8).copy(alpha = 0.08f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = if (isActive) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.6f), 
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            label, 
            color = if (isActive) Color.White else Color.Gray, 
            fontSize = 14.sp, 
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
        
        if (isActive) {
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(4.dp, 4.dp).background(Color(0xFF818CF8), CircleShape))
        }
    }
}

@Composable
fun TaskItemPlaceholder(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp) // Smaller indicator
                .background(
                    color = if (index == 0) Color(0xFFF87171) else Color(0xFF4ADE80),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (index == 0) "Finish Zenith UI" else if (index == 1) "Exercise" else "Read book",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.weight(1f))
        if (index == 0) {
            Text("High", color = Color(0xFFF87171).copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun IconCustom(icon: ImageVector, contentDescription: String?, tint: Color, size: androidx.compose.ui.unit.Dp) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size)
    )
}
