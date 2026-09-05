package com.safesphere.data.local.dao

import androidx.room.*
import com.safesphere.data.local.entity.IncidentLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentLogDao {

    @Query("SELECT * FROM incident_logs ORDER BY startTimestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentLogEntity)

    @Update
    suspend fun updateIncident(incident: IncidentLogEntity)

    @Query("SELECT * FROM incident_logs WHERE incidentId = :id LIMIT 1")
    suspend fun getIncidentById(id: String): IncidentLogEntity?
}
