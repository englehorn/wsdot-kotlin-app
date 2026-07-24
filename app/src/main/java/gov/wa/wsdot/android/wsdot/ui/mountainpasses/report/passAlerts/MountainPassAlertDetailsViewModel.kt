package gov.wa.wsdot.android.wsdot.ui.mountainpasses.report.passAlerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository
import gov.wa.wsdot.android.wsdot.model.common.Resource
import javax.inject.Inject

class MountainPassAlertDetailsViewModel @Inject constructor(passRepository: MountainPassRepository): ViewModel() {

    private val _passAlertQuery: MutableLiveData<PassAlertsQuery> = MutableLiveData()

    val passAlert: LiveData<Resource<MountainPassAlert>> = _passAlertQuery.switchMap { input ->
        input.ifExists { alertId ->
            passRepository.loadMountainPassAlert(alertId)
        }
    }

    fun refresh() {
        val passId = _passAlertQuery.value?.alertId
        if (passId != null) {
            _passAlertQuery.value = PassAlertsQuery(passId)
        }
    }

    fun setPassAlertRouteQuery(alertId: Int) {
        val update = PassAlertsQuery(alertId)
        if (_passAlertQuery.value == update) { return }
        _passAlertQuery.value = update
    }

    data class PassAlertsQuery(val alertId: Int) {
        fun <T> ifExists(f: (Int) -> LiveData<T>): LiveData<T> {
            return f(alertId)
        }
    }

}