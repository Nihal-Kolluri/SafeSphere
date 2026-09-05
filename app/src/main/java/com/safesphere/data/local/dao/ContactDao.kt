package com.safesphere.data.local.dao

import androidx.room.*
import com.safesphere.data.local.entity.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    suspend fun getAllContactsSync(): List<EmergencyContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Delete
    suspend fun deleteContact(contact: EmergencyContactEntity)

    @Query("SELECT COUNT(*) FROM emergency_contacts")
    suspend fun getContactCount(): Int
}
