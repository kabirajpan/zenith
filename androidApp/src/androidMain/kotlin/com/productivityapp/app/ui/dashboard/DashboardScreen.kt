package com.productivityapp.app.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.productivityapp.app.ui.notes.NotesScreen
import com.productivityapp.model.NoteItem
import com.productivityapp.app.ui.vault.VaultScreen
import com.productivityapp.app.ui.alarm.AlarmScreen
import com.productivityapp.app.ui.notes.NoteEditorScreen
import com.productivityapp.app.ui.tasks.TasksRepository
import com.productivityapp.app.ui.tasks.AddTaskModal

sealed class ZenithScreen {
    object Dashboard : ZenithScreen()
    object Tasks : ZenithScreen()
    object Vault : ZenithScreen()
    object Notes : ZenithScreen()
    object NoteEditor : ZenithScreen()
    object Draw : ZenithScreen()
    object Alarm : ZenithScreen()
    object Reminder : ZenithScreen()
    object Settings : ZenithScreen()
}

@Composable
fun DashboardScreen() {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    var isRightSidebarVisible by remember { mutableStateOf(false) }
    var isQuickActionsVisible by remember { mutableStateOf(false) }
    var showAddTaskModal by remember { mutableStateOf(false) }
    var showAddVaultDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf<ZenithScreen>(ZenithScreen.Dashboard) }
    var selectedNote by remember { mutableStateOf<NoteItem?>(null) }

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
            drawerContent = {
                LeftSidebarContent(
                    onNavigate = { screen ->
                        currentScreen = screen
                        scope.launch { scaffoldState.drawerState.close() }
                    }
                )
            },
            drawerBackgroundColor = Color(0xFF0F172A),
            topBar = {
                if (!isEditor) {
                    ZenithTopBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onMenuClick = { scope.launch { scaffoldState.drawerState.open() } },
                        onUserClick = { isRightSidebarVisible = true }
                    )
                }
            },
            bottomBar = {
                if (!isEditor) {
                    ZenithBottomNav(
                        selectedTab = when(currentScreen) {
                            ZenithScreen.Dashboard -> 0
                            ZenithScreen.Tasks -> 1
                            ZenithScreen.Settings -> 3
                            else -> 0
                        },
                        onTabSelected = { index ->
                            currentScreen = when(index) {
                                0 -> ZenithScreen.Dashboard
                                1 -> ZenithScreen.Tasks
                                3 -> ZenithScreen.Settings
                                else -> ZenithScreen.Dashboard
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isEditor && currentScreen != ZenithScreen.Settings) {
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
            ) {
                when (currentScreen) {
                    ZenithScreen.Dashboard -> MainContent()
                    ZenithScreen.Tasks -> TaskListScreen()
                    ZenithScreen.Vault -> VaultScreen()
                    ZenithScreen.Notes -> NotesScreen(onNoteClick = { 
                        selectedNote = it
                        currentScreen = ZenithScreen.NoteEditor
                    })
                    ZenithScreen.NoteEditor -> NoteEditorScreen(
                        initialNote = selectedNote,
                        onBack = { 
                            selectedNote = null
                            currentScreen = ZenithScreen.Notes 
                        }
                    )
                    ZenithScreen.Alarm -> AlarmScreen()
                    ZenithScreen.Draw -> PlaceholderScreen("Draw")
                    ZenithScreen.Reminder -> PlaceholderScreen("Reminder")
                    ZenithScreen.Settings -> PlaceholderScreen("Settings")
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
                        "Vault" -> { currentScreen = ZenithScreen.Vault; showAddVaultDialog = true }
                        "Alarm" -> currentScreen = ZenithScreen.Alarm
                        "Draw" -> currentScreen = ZenithScreen.Draw
                        "Reminder" -> currentScreen = ZenithScreen.Reminder
                    }
                }
            )
        }

        if (showAddTaskModal) {
            AddTaskModal(
                initialCategory = "Work",
                onDismiss = { showAddTaskModal = false },
                onTaskCreated = { title, category, priority ->
                    TasksRepository.addTask(title, category, priority)
                    showAddTaskModal = false
                }
            )
        }
        if (showAddVaultDialog) {
            AddEntryDialog(title = "New Vault Entry", label = "Site Name", onDismiss = { showAddVaultDialog = false })
        }
        if (showAddVaultDialog) {
            AddEntryDialog(title = "New Vault Entry", label = "Site Name", onDismiss = { showAddVaultDialog = false })
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
            RightSidebarContent { isRightSidebarVisible = false }
        }

    }
}

@Composable
fun QuickActionsModal(onClose: () -> Unit, onAction: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Text("Create New", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            
            val actions = listOf(
                Triple(Icons.Default.Edit, "Note", { onAction("Note") }),
                Triple(Icons.Default.Done, "Task", { onAction("Task") }),
                Triple(Icons.Default.Lock, "Vault", { onAction("Vault") }),
                Triple(Icons.Default.PlayArrow, "Draw", { onAction("Draw") }),
                Triple(Icons.Default.Notifications, "Alarm", { onAction("Alarm") }),
                Triple(Icons.Default.Info, "Reminder", { onAction("Reminder") }),
                Triple(Icons.Default.Star, "AI Coach", { onClose() })
            )
            
            val colors = listOf(Color(0xFFF87171), Color(0xFF60A5FA), Color(0xFFFBBF24), Color(0xFF34D399), Color(0xFFA78BFA), Color(0xFFFB7185), Color(0xFF818CF8))
            
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val chunks = actions.chunked(3)
                for (i in chunks.indices) {
                    val rowActions = chunks[i]
                    val rowColors = colors.drop(i * 3).take(rowActions.size)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (j in rowActions.indices) {
                            val action = rowActions[j]
                            val color = rowColors[j]
                            ActionItem(action.first, action.second, color, action.third)
                        }
                        if (rowActions.size < 3) {
                            repeat(3 - rowActions.size) { Spacer(modifier = Modifier.width(80.dp)) }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
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
        BottomNavItem(icon = Icons.Default.Done, label = "Tasks", selected = selectedTab == 1, onClick = { onTabSelected(1) })
        BottomNavItem(icon = Icons.Default.Star, label = "AI", selected = selectedTab == 2, onClick = { onTabSelected(2) })
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
fun MainContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFF818CF8).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Stretch for 5 mins?",
                    color = Color.White,
                    fontSize = 13.sp
                )
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
        
        repeat(4) { index ->
            TaskItemPlaceholder(index)
            Spacer(modifier = Modifier.height(6.dp))
        }
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
                if (pagerState.currentPage == 0) "May 2026" else "Activity Heatmap", 
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
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            days.forEach { day ->
                Text(day, color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Month Grid
        val weeks = listOf(
            listOf("", "", "", "", 1, 2, 3),
            listOf(4, 5, 6, 7, 8, 9, 10),
            listOf(11, 12, 13, 14, 15, 16, 17),
            listOf(18, 19, 20, 21, 22, 23, 24),
            listOf(25, 26, 27, 28, 29, 30, 31)
        )
        
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { date ->
                    val isToday = date == 11
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isToday) Color(0xFF818CF8) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.toString(),
                            color = if (isToday) Color.White else if (date == "") Color.Transparent else Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
fun ActivityHeatmap() {
    var selectedYear by remember { mutableStateOf("2026") }
    val years = listOf("2024", "2025", "2026", "Stats")
    
    val activityLevels = remember(selectedYear) {
        List(7) { List(53) { (0..4).random() } }
    }
    
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(30.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                val pillWidth = 53.dp
                val targetIndex = years.indexOf(selectedYear)
                val offset by animateDpAsState(
                    targetValue = (53 * targetIndex).dp,
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
                    years.forEach { year ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedYear = year
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = year,
                                color = if (selectedYear == year) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = if (selectedYear == year) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (selectedYear == "Stats") {
            // Stats Dashboard stays as is (already high-density)
            ProductivityStatsView()
        } else {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Expanded Day Labels
                Column(
                    modifier = Modifier.width(32.dp).padding(top = 28.dp), 
                ) {
                    val dayLabels = listOf("", "Mon", "", "Wed", "", "Fri", "")
                    dayLabels.forEach { day ->
                        Box(modifier = Modifier.height(19.dp), contentAlignment = Alignment.CenterStart) {
                            Text(day, color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Column(modifier = Modifier.weight(1f).horizontalScroll(scrollState)) {
                    // Expanded Month Labels
                    Row(
                        modifier = Modifier.padding(start = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(78.dp) // Fine-tuned for 15dp blocks + 4dp spacing
                    ) {
                        months.forEach { month ->
                            Text(month, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Balanced Heatmap Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val activityColor = Color(0xFF22C55E)
                        activityLevels[0].indices.forEach { weekIndex ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                activityLevels.forEach { dayRow ->
                                    val level = dayRow[weekIndex]
                                    val color = when(level) {
                                        4 -> activityColor
                                        3 -> activityColor.copy(alpha = 0.7f)
                                        2 -> activityColor.copy(alpha = 0.4f)
                                        1 -> activityColor.copy(alpha = 0.2f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", color = Color.Gray, fontSize = 9.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val activityColor = Color(0xFF22C55E)
                    repeat(5) { i ->
                        val color = if (i == 0) Color.White.copy(alpha = 0.05f) 
                                   else activityColor.copy(alpha = if(i==1) 0.2f else if(i==2) 0.4f else if(i==3) 0.7f else 1.0f)
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("More", color = Color.Gray, fontSize = 9.sp)
            }
        }
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
fun LeftSidebarContent(onNavigate: (ZenithScreen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(Color(0xFF0F172A))
            .padding(20.dp)
    ) {
        Text("Zenith", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        SidebarItem(Icons.Default.Home, "Dashboard") { onNavigate(ZenithScreen.Dashboard) }
        SidebarItem(Icons.Default.Done, "Tasks") { onNavigate(ZenithScreen.Tasks) }
        SidebarItem(Icons.Default.Edit, "Notes") { onNavigate(ZenithScreen.Notes) }
        SidebarItem(Icons.Default.Lock, "Secure Vault") { onNavigate(ZenithScreen.Vault) }
        SidebarItem(Icons.Default.Notifications, "Alarms") { onNavigate(ZenithScreen.Alarm) }
        
        Spacer(modifier = Modifier.weight(1f))
        
        SidebarItem(Icons.Default.Settings, "Settings") { onNavigate(ZenithScreen.Settings) }
    }
}

@Composable
fun RightSidebarContent(onClose: () -> Unit) {
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
        SidebarItem(Icons.Default.Star, "Achievements") { /* Action */ }
        SidebarItem(Icons.Default.Settings, "Account Settings") { /* Action */ }
    }
}

@Composable
fun SidebarItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
