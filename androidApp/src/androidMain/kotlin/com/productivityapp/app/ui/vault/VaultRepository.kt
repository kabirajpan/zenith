package com.productivityapp.app.ui.vault

import androidx.compose.runtime.mutableStateListOf

object VaultRepository {
    val vaultItems = mutableStateListOf<VaultItem>(
        VaultItem(site = "Netflix", username = "user@example.com", password = "SecurePassword123", category = "Social", description = "Netflix streaming account"),
        VaultItem(site = "Google", username = "dev.productivity@gmail.com", password = "GoogleSafePass!88", category = "Work", description = "Main work email account")
    )

    fun addVaultItem(item: VaultItem) {
        vaultItems.add(0, item)
    }

    fun removeVaultItem(item: VaultItem) {
        vaultItems.remove(item)
    }

    fun searchVault(query: String): List<VaultItem> {
        val lowQuery = query.lowercase().trim()
        if (lowQuery.isBlank() || lowQuery == "password" || lowQuery == "passwords" || lowQuery == "vault" || lowQuery == "all") return vaultItems.toList()
        return vaultItems.filter { 
            it.site.lowercase().contains(lowQuery) || 
            it.description.lowercase().contains(lowQuery) 
        }
    }
}
