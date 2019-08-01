package gov.wa.wsdot.android.wsdot.ui.highwayAlerts

import androidx.lifecycle.*
import gov.wa.wsdot.android.wsdot.db.traffic.HighwayAlert
import gov.wa.wsdot.android.wsdot.repository.HighwayAlertRepository
import gov.wa.wsdot.android.wsdot.util.network.Resource
import javax.inject.Inject

class HighwayAlertsViewModel @Inject constructor(highwayAlertRepository: HighwayAlertRepository) : ViewModel() {

    private val repo = highwayAlertRepository

    // mediator handles resubscribe on refresh
    val alerts = MediatorLiveData<Resource<List<HighwayAlert>>>()

    private var alertsLiveData : LiveData<Resource<List<HighwayAlert>>> = highwayAlertRepository.loadHighwayAlerts(false)

    init {
        alerts.addSource(alertsLiveData) { alerts.value = it }
    }

    fun refresh() {
        alerts.removeSource(alertsLiveData)
        alertsLiveData = repo.loadHighwayAlerts(true)
        alerts.addSource(alertsLiveData) { alerts.value = it }
    }

}