package com.productivityapp.app.ui.vault

import androidx.compose.runtime.mutableStateListOf

object VaultRepository {
    val vaultItems = mutableStateListOf<VaultItem>(
        VaultItem(type = VaultType.PASSWORD, title = "Netflix", encryptedData = "user@example.com:SecurePassword123", icon = "Netflix"),
        VaultItem(type = VaultType.PASSWORD, title = "Google", encryptedData = "dev.productivity@gmail.com:GoogleSafePass!88", icon = "Google")
    )

    fun addVaultItem(item: VaultItem) {
        vaultItems.add(0, item)
    }

    fun removeVaultItem(item: VaultItem) {
        vaultItems.remove(item)
    }

    fun searchVault(query: String): List<VaultItem> {
        val lowQuery = query.lowercase().trim()
        if (lowQuery.isBlank() || lowQuery == "all") return vaultItems.toList()
        return vaultItems.filter { 
            it.title.lowercase().contains(lowQuery) || 
            it.type.name.lowercase().contains(lowQuery) 
        }
    }
}
