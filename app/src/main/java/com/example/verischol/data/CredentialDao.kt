package com.example.verischol.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CredentialDao {

    @Insert
    suspend fun insert(credential: Credential)

    @Query("SELECT * FROM credentials ORDER BY timestamp DESC")
    suspend fun getAll(): List<Credential>
}
