package gov.wa.wsdot.android.wsdot.ui.mountainpasses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPass
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.db.traffic.HighwayAlert
import gov.wa.wsdot.android.wsdot.repository.MountainPassRepository
import gov.wa.wsdot.android.wsdot.model.common.Resource
import gov.wa.wsdot.android.wsdot.ui.highwayAlerts.HighwayAlertViewModel.AlertQuery
import gov.wa.wsdot.android.wsdot.util.AbsentLiveData
import javax.inject.Inject

class MountainPassViewModel @Inject constructor(mountainPassRepository: MountainPassRepository) : ViewModel() {

    private val repo = mountainPassRepository

    val passes = MediatorLiveData<Resource<List<MountainPass>>>()

    private var _passes: LiveData<Resource<List<MountainPass>>> = mountainPassRepository.loadPasses(false)

    private val _alertQuery: MutableLiveData<AlertQuery> = MutableLiveData()


    init {
        passes.addSource(_passes) { passes.value = it }
    }

    val alert: LiveData<Resource<MountainPassAlert>> = _alertQuery.switchMap { input ->
        input.ifExists {
            mountainPassRepository.loadMountainPassAlert(it)
        }
    }

//    fun refresh() {
//        repo.loadMountainPassAlerts(true)
//    }

    fun setAlertQuery(alertId: Int) {
        val update = AlertQuery(alertId)
        if (_alertQuery.value == update) {
            return
        }
        _alertQuery.value = update
    }


    data class AlertQuery(val alertId: Int) {
        fun <T> ifExists(f: (Int) -> LiveData<T>): LiveData<T> {
            return if (alertId == 0 ) {
                AbsentLiveData.create()
            } else {
                f(alertId)
            }
        }
    }


    fun updateFavorite(passId: Int, isFavorite: Boolean) {
        repo.updateFavorite(passId, isFavorite)
    }

    fun refresh() {
        passes.removeSource(_passes)
        _passes = repo.loadPasses(true)
        passes.addSource(_passes) { passes.value = it }
    }
}