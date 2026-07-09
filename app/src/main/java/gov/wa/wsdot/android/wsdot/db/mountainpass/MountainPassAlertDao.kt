package gov.wa.wsdot.android.wsdot.db.mountainpass

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Interface for database access on Mountain Pass Alerts.
 */
@Dao
abstract class MountainPassAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAlerts(alerts: List<MountainPassAlert>)

    @Query("SELECT * FROM MountainPassAlert")
    abstract fun loadAlerts(): LiveData<List<MountainPassAlert>>

    @Query("SELECT * FROM MountainPassAlert WHERE passId in (:passId) ORDER BY createdDate DESC")
    abstract fun loadAlertsById(passId: Int): LiveData<List<MountainPassAlert>>

    @Query("SELECT * FROM MountainPassAlert WHERE eventId = (:eventId) LIMIT 1")
    abstract fun loadAlertById(eventId: Int): LiveData<MountainPassAlert>

    @Query("DELETE FROM MountainPassAlert")
    abstract fun deleteOldAlerts()

    @Transaction
    open fun updateAlerts(alerts: List<MountainPassAlert>) {
        deleteOldAlerts()
        insertAlerts(alerts)
    }

}