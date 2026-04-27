package com.example.verischol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey val id: String,
    val issuerDid: String,
    val encryptedData: String,
    val timestamp: Long
)


