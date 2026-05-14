package com.productivityapp.app.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VaultScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddModal by remember { mutableStateOf(false) }
    
    val vaultItems = VaultRepository.vaultItems
    val categories = listOf("All", "Social", "Work", "Finance")
    
    val filteredItems = remember(searchQuery, selectedCategory, vaultItems.size) {
        vaultItems.filter { item ->
            val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || 
                               item.encryptedData.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.type.name.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vault",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            IconButton(onClick = { showAddModal = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
        
        if (showAddModal) {
            AddVaultItemModal(
                onDismiss = { showAddModal = false },
                onAdd = { item ->
                    VaultRepository.addVaultItem(item)
                    showAddModal = false
                }
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Compact Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF22C55E).copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text("Search...", color = Color.Gray, fontSize = 13.sp)
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF22C55E)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Compact Categories
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF22C55E).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredItems) { item ->
                VaultCard(item)
            }
        }
    }
}

@Composable
fun VaultCard(item: VaultItem) {
    var isDataVisible by remember { mutableStateOf(false) }
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Type Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF22C55E).copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.icon.isNotEmpty()) {
                    Text(item.icon.take(1), color = Color(0xFF22C55E), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = when(item.type) {
                            VaultType.PASSWORD -> Icons.Default.VpnKey
                            VaultType.CARD -> Icons.Default.CreditCard
                            VaultType.NOTE -> Icons.Default.Notes
                            VaultType.ID -> Icons.Default.Badge
                        },
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (item.isFavorite) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                    }
                }
                if (item.username.isNotEmpty()) {
                    Text(text = item.username, color = Color.Gray.copy(alpha = 0.7f), fontSize = 11.sp)
                }
                Text(
                    text = if (isDataVisible) item.encryptedData else "••••••••", 
                    color = Color.Gray, 
                    fontSize = 13.sp
                )
            }
            
            Row {
                IconButton(onClick = { isDataVisible = !isDataVisible }) {
                    Icon(
                        imageVector = if (isDataVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = if (isDataVisible) Color(0xFF22C55E) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(onClick = { 
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(item.encryptedData))
                }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

enum class VaultType { PASSWORD, CARD, NOTE, ID }

data class VaultItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: VaultType = VaultType.PASSWORD,
    val title: String,
    val username: String = "", // For passwords/emails
    val encryptedData: String, // For the secret password, card details, or note
    val icon: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

val dummyVaultItems = emptyList<VaultItem>()

@Composable
fun AddVaultItemModal(onDismiss: () -> Unit, onAdd: (VaultItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var encryptedData by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VaultType.PASSWORD) }
    var isFavorite by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF22C55E), CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Secure Entry", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                VaultInputField(label = "NAME", value = title, onValueChange = { title = it }, placeholder = "netflix")
                VaultInputField(label = "USERNAME / EMAIL", value = username, onValueChange = { username = it }, placeholder = "mail id")
                VaultInputField(label = "PASSWORD", value = encryptedData, onValueChange = { encryptedData = it }, isPassword = true, placeholder = "pw")
                
                // Fused Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Text("VAULT TYPE", color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VaultType.values().forEach { type ->
                                val isSelected = selectedType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF22C55E).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .clickable { selectedType = type },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type.name, 
                                        color = if (isSelected) Color(0xFF22C55E) else Color.Gray, 
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Fused Favorite Row
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = if (isFavorite) Color(0xFFFBBF24) else Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mark as Favorite", color = Color.White, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF22C55E),
                                checkedTrackColor = Color(0xFF22C55E).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.4f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(10.dp),
                        elevation = null
                    ) {
                        Text("Cancel", color = Color.Gray, fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = { 
                            if (title.isNotBlank() && encryptedData.isNotBlank()) {
                                onAdd(VaultItem(
                                    type = selectedType,
                                    title = title,
                                    username = username,
                                    encryptedData = encryptedData,
                                    isFavorite = isFavorite,
                                    icon = title
                                ))
                            }
                        },
                        modifier = Modifier.weight(0.6f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF22C55E),
                            disabledBackgroundColor = Color(0xFF22C55E)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        enabled = title.isNotBlank() && encryptedData.isNotBlank()
                    ) {
                        Text(
                            "Save Entry", 
                            color = if (title.isNotBlank() && encryptedData.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f), 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultInputField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String = "", isPassword: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(label.uppercase(), color = Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.04f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
        ) {
            Box(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                if (value.isEmpty()) {
                    Text(placeholder, color = Color.Gray.copy(alpha = 0.4f), fontSize = 11.sp)
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF22C55E)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
