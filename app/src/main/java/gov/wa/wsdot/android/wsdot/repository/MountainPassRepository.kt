package gov.wa.wsdot.android.wsdot.repository

import androidx.lifecycle.LiveData
import gov.wa.wsdot.android.wsdot.api.WebDataService
import gov.wa.wsdot.android.wsdot.api.response.mountainpass.MountainPassResponse
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPass
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassDao
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlertDao
import gov.wa.wsdot.android.wsdot.util.AppExecutors
import gov.wa.wsdot.android.wsdot.util.TimeUtils
import gov.wa.wsdot.android.wsdot.model.common.NetworkBoundResource
import gov.wa.wsdot.android.wsdot.model.common.Resource
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MountainPassRepository @Inject constructor(
    private val dataWebservice: WebDataService,
    private val appExecutors: AppExecutors,
    private val mountainPassDao: MountainPassDao,
    private val mountainPassAlertDao: MountainPassAlertDao

) {

    fun loadPasses(forceRefresh: Boolean): LiveData<Resource<List<MountainPass>>> {

        return object : NetworkBoundResource<List<MountainPass>, MountainPassResponse>(appExecutors) {

            override fun saveCallResult(item: MountainPassResponse) = savePasses(item)

            override fun shouldFetch(data: List<MountainPass>?): Boolean {

                if (forceRefresh) {
                    return true
                }

                var update = false

                if (data != null && data.isNotEmpty()) {
                    if (TimeUtils.isOverXMinOld(data[0].localCacheDate, x = 5)) {
                        update = true
                    }
                } else {
                    update = true
                }

                return update
            }

            override fun loadFromDb() = mountainPassDao.loadPasses()

            override fun createCall() = dataWebservice.getMountainPassReports()

            override fun onFetchFailed() {
                //repoListRateLimit.reset(owner)
            }

        }.asLiveData()
    }

    fun loadPass(passId: Int, forceRefresh: Boolean): LiveData<Resource<MountainPass>> {

        return object : NetworkBoundResource<MountainPass, MountainPassResponse>(appExecutors) {

            override fun saveCallResult(item: MountainPassResponse) = savePasses(item)

            override fun shouldFetch(data: MountainPass?): Boolean {

                if (forceRefresh) {
                    return true
                }

                var update = false

                if (data != null){
                    if (TimeUtils.isOverXMinOld(data.localCacheDate, x = 10080)) {
                        update = true
                    }
                } else {
                    update = true
                }

                return update
            }

            override fun loadFromDb() = mountainPassDao.loadPass(passId)

            override fun createCall() = dataWebservice.getMountainPassReports()

            override fun onFetchFailed() {
                //repoListRateLimit.reset(owner)
            }

        }.asLiveData()
    }

    fun loadMountainPassAlerts(passId: Int, forceRefresh: Boolean): LiveData<Resource<List<MountainPassAlert>>> {

        return object : NetworkBoundResource<List<MountainPassAlert>, MountainPassResponse>(appExecutors) {

            override fun saveCallResult(item: MountainPassResponse) = saveAlerts(item)

            override fun shouldFetch(data: List<MountainPassAlert>?): Boolean {

                if (forceRefresh) {
                    return true
                }

                var update = false

                if (data != null) {
                    if (data.isEmpty()) {
                        update = true
                    }
                } else {
                    update = true
                }

                return update

            }

            override fun loadFromDb() = mountainPassAlertDao.loadAlertsById(passId)

            override fun createCall() = dataWebservice.getMountainPassReports()

            override fun onFetchFailed() {
                //repoListRateLimit.reset(owner)
            }

        }.asLiveData()
    }

    fun loadMountainPassAlert(eventId: Int): LiveData<Resource<MountainPassAlert>> {

        return object : NetworkBoundResource<MountainPassAlert, MountainPassResponse>(appExecutors) {

            override fun saveCallResult(item: MountainPassResponse) = saveAlerts(item)

            override fun shouldFetch(data: MountainPassAlert?): Boolean {
                return true
            }

            override fun loadFromDb() = mountainPassAlertDao.loadAlertById(eventId)

            override fun createCall() = dataWebservice.getMountainPassReports()

            override fun onFetchFailed() {
                //repoListRateLimit.reset(owner)
            }

        }.asLiveData()
    }

    fun loadFavoritePasses(forceRefresh: Boolean): LiveData<Resource<List<MountainPass>>> {

        return object : NetworkBoundResource<List<MountainPass>, MountainPassResponse>(appExecutors) {

            override fun saveCallResult(item: MountainPassResponse) = savePasses(item)

            override fun shouldFetch(data: List<MountainPass>?): Boolean {

                var update = false

                if (data != null && data.isNotEmpty()) {
                    if (TimeUtils.isOverXMinOld(data[0].localCacheDate, x = 15)) {
                        update = true
                    }
                } else {
                    update = true
                }

                return forceRefresh || update
            }

            override fun loadFromDb() = mountainPassDao.loadFavoritePasses()

            override fun createCall() = dataWebservice.getMountainPassReports()

            override fun onFetchFailed() {
                //repoListRateLimit.reset(owner)
            }

        }.asLiveData()
    }



    private fun saveAlerts(passAlertsResponse: MountainPassResponse) {

        val dbAlertList = arrayListOf<MountainPassAlert>()
        
        for (passAlertResponse in passAlertsResponse.passConditions.items) {
            for (passAlertResponse in passAlertResponse.alerts!!) {
                dbAlertList.add(MountainPassAlert(
                    passAlertResponse.eventId,
                    passAlertResponse.passId,
                    passAlertResponse.mountainPass,
                    passAlertResponse.travelCenterPriorityId,
                    passAlertResponse.eventCategoryTypeDescription,
                    passAlertResponse.headlineMessage,
                    passAlertResponse.roadName,
                    passAlertResponse.roadDirection,
                    passAlertResponse.displayLatitude,
                    passAlertResponse.displayLongitude,
                    Date(passAlertResponse.createdDate.substring(6, 19).toLong())
                ))
            }

        }

        mountainPassAlertDao.updateAlerts(dbAlertList.distinct())

    }

    private fun savePasses(passResponse: MountainPassResponse) {

        var dbPassList = arrayListOf<MountainPass>()

        for (passItem in passResponse.passConditions.items) {

            val pass = MountainPass(
                passId = passItem.mountainPassId,
                passName = passItem.mountainPassName,
                roadCondition = passItem.roadCondition,
                weatherCondition = passItem.weatherCondition,
                temperatureInFahrenheit = passItem.temperatureInFahrenheit,
                elevationInFeet = passItem.elevationInFeet,
                travelAdvisoryActive = passItem.travelAdvisoryActive,
                latitude = passItem.latitude,
                longitude = passItem.longitude,
                restrictionOneText = passItem.restrictionOne.restrictionText,
                restrictionOneDirection = passItem.restrictionOne.travelDirection,
                restrictionTwoText = passItem.restrictionTwo.restrictionText,
                restrictionTwoDirection = passItem.restrictionTwo.travelDirection,
                serverCacheDate = parsePassDate(passItem.dateUpdated),
                localCacheDate = Date(),
                cameras = passItem.cameras,
                forecasts = passItem.forecast,
                alerts = passItem.alerts,
                favorite = false,
                remove = false
            )
            dbPassList.add(pass)

        }
        mountainPassDao.updateMountainPasses(dbPassList)
    }



    fun updateFavorite(passId: Int, isFavorite: Boolean) {
        appExecutors.diskIO().execute {
            mountainPassDao.updateFavorite(passId, isFavorite)
        }
    }

    private fun parsePassDate(passDate: List<Int>): Date {
        // DateUpdated: [2019,6,24,20,46]
        val parseDateFormat = SimpleDateFormat("yyyy,M,d,HH,m") //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]

        val sb = StringBuilder()
        for (dateItem in passDate) {
            sb.append(dateItem)
            sb.append(",")
        }

        val dateString = sb.toString()
        parseDateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))
        return parseDateFormat.parse(dateString)

    }

}