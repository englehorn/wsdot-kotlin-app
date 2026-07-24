package gov.wa.wsdot.android.wsdot.db.mountainpass

import androidx.room.Entity
import java.util.*

@Entity(
    primaryKeys = ["eventId", "passId"]
)
data class MountainPassAlert(
    val eventId: Int,
    val passId: Int,
    val mountainPass: String,
    val travelCenterPriorityId: Int,
    val eventCategoryTypeDescription: String,
    val headlineMessage: String,
    val roadName: String,
    val roadDirection: String,
    val displayLatitude: Double?,
    val displayLongitude: Double?,
    val createdDate: Date?
)