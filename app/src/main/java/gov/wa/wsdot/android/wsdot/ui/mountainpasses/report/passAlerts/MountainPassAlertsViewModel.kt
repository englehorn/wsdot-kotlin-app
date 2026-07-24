package gov.wa.wsdot.android.wsdot.ui.mountainpasses.report.passAlerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.model.common.Resource
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository
import javax.inject.Inject

class MountainPassAlertsViewModel @Inject constructor(mountainPassRepository: MountainPassRepository): ViewModel() {

    private val _mountainPassAlertsQuery: MutableLiveData<MountainPassAlertsQuery> = MutableLiveData()

    val passAlerts: LiveData<Resource<List<MountainPassAlert>>> = _mountainPassAlertsQuery.switchMap { input ->
            input.ifExists { passId, forceRefresh ->
                mountainPassRepository.loadMountainPassAlerts(passId, forceRefresh)
            }
        }

    fun refresh() {
        val passId = _mountainPassAlertsQuery.value?.passId
        if (passId != null) {
            _mountainPassAlertsQuery.value = MountainPassAlertsQuery(passId, true)
        }
    }

    fun setMountainPassAlertsQuery(passId: Int) {
        val update = MountainPassAlertsQuery(passId, false)
        if (_mountainPassAlertsQuery.value == update) { return }
        _mountainPassAlertsQuery.value = update
    }

    data class MountainPassAlertsQuery(val passId: Int, val forceRefresh: Boolean) {
        fun <T> ifExists(f: (Int, Boolean) -> LiveData<T>): LiveData<T> {
            return f(passId, forceRefresh)
        }
    }

}