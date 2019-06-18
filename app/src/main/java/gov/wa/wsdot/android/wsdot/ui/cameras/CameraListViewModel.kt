package gov.wa.wsdot.android.wsdot.ui.cameras

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import gov.wa.wsdot.android.wsdot.db.traffic.Camera
import gov.wa.wsdot.android.wsdot.repository.CameraRepository
import gov.wa.wsdot.android.wsdot.util.network.Resource
import javax.inject.Inject

class CameraListViewModel @Inject constructor(cameraRepository: CameraRepository) : ViewModel() {

    private val repo = cameraRepository

    // mediator handles resubscribe on refresh
    val cameras = MediatorLiveData<Resource<List<Camera>>>()

    private var camerasLiveData : LiveData<Resource<List<Camera>>> = cameraRepository.loadCameras(false)

    init {
        cameras.addSource(camerasLiveData) { cameras.value = it }
    }

    fun updateFavorite(routeId: Int, isFavorite: Boolean) {
      //  repo.updateFavorite(routeId, isFavorite)
    }

    fun refresh() {
        cameras.removeSource(camerasLiveData)
        camerasLiveData = repo.loadCameras(true)
        cameras.addSource(camerasLiveData) { cameras.value = it }
    }
}