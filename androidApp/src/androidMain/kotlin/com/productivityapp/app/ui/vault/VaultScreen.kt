package com.productivityapp.app.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VaultScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Secure Vault",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dummyVaultItems) { item ->
                VaultCard(item)
            }
        }
    }
}

@Composable
fun VaultCard(item: VaultItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.site, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = item.username, color = Color.Gray, fontSize = 13.sp)
        }
        
        IconButton(onClick = { /* Toggle Visibility */ }) {
            Icon(Icons.Default.Search, contentDescription = "View", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

data class VaultItem(
    val site: String,
    val username: String
)

val dummyVaultItems = listOf(
    VaultItem("Google Account", "user@gmail.com"),
    VaultItem("GitHub", "zenith_dev"),
    VaultItem("LinkedIn", "user_profile"),
    VaultItem("Netflix", "home_account"),
    VaultItem("Bank of Zenith", "acc_12345")
)
