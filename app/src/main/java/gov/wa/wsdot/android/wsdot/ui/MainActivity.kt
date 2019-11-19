package gov.wa.wsdot.android.wsdot.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import dagger.android.support.HasSupportFragmentInjector
import gov.wa.wsdot.android.wsdot.NavGraphDirections
import gov.wa.wsdot.android.wsdot.R
import gov.wa.wsdot.android.wsdot.service.helpers.MyNotificationManager
import gov.wa.wsdot.android.wsdot.util.TimeUtils
import gov.wa.wsdot.android.wsdot.util.getDouble
import gov.wa.wsdot.android.wsdot.util.putDouble
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    HasSupportFragmentInjector, SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: EventBannerViewModel

    lateinit var drawerLayout: DrawerLayout

    private var eventTitle = "WSDOT"

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDarkMode(PreferenceManager.getDefaultSharedPreferences(this), getString(R.string.key_darkmodesystem))

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        drawerLayout = findViewById(R.id.drawer_layout)

        val navView: NavigationView = findViewById(R.id.drawer_nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Navigation component setup with drawer
        val config = AppBarConfiguration(
            setOf(
                R.id.navTrafficMapFragment,
                R.id.navFerriesHomeFragment,
                R.id.navMountainPassHomeFragment,
                R.id.navBorderCrossingsFragment,
                R.id.navTollRatesFragment,
                R.id.navFavoritesFragment,
                R.id.navAmtrakCascadesFragment,
                R.id.navAboutFragment,
                R.id.navSettingsFragment,
                R.id.navNotificationsFragment
            ), drawerLayout)


        // TODO: Let user set home screen ///////////////////////
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)
        graph.startDestination = R.id.navTrafficMapFragment
        navController.graph = graph

        NavigationUI.setupWithNavController(findViewById(R.id.toolbar), navController, config)
        NavigationUI.setupActionBarWithNavController(this, navController, config)

        navView.menu.findItem(R.id.nav_traffic_map).isChecked = true

        enableAds(resources.getString(R.string.ad_target_traffic))

        // handle event banner
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EventBannerViewModel::class.java)
        viewModel.eventStatus.observe(this, Observer { eventResponse ->
            eventResponse.data?.let {

                val settings = PreferenceManager.getDefaultSharedPreferences(this)
                val editor = settings.edit()

                if (TimeUtils.currentDateInRange(it.startDate, it.endDate, "yyyy-MM-dd")) {

                    eventTitle = it.title
                    navView.menu.setGroupVisible(R.id.event_banner_group, true)
                    navView.menu.findItem(R.id.event_banner).actionView.findViewById<TextView>(R.id.event_banner_text).text = it.bannerText

                    editor.putInt(getString(R.string.pref_key_theme), it.themeId)
                    editor.apply()

                } else {
                    navView.menu.setGroupVisible(R.id.event_banner_group, false)

                    editor.putInt(getString(R.string.pref_key_theme), 0)
                    editor.apply()
                }
            }
        })

        // sendTestNotification(1, "test", "its a test")

        intent?.extras?.let { handlePushNotifications(it) }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.extras?.let { handlePushNotifications(it) }
    }

    private fun handlePushNotifications(extras: Bundle) {
        if (extras.getBoolean(getString(R.string.push_alert_traffic_alert), false)) {

            val settings = PreferenceManager.getDefaultSharedPreferences(this)

            val latitude = settings.getDouble(getString(R.string.user_preference_traffic_map_latitude), 47.6062)
            val longitude = settings.getDouble(getString(R.string.user_preference_traffic_map_longitude), -122.3321)

            val lat = extras.getDouble(getString(R.string.push_alert_traffic_alert_latitude), latitude)
            val lng = extras.getDouble(getString(R.string.push_alert_traffic_alert_longitude), longitude)

            val editor = settings.edit()
            editor.putDouble(
                getString(R.string.user_preference_traffic_map_latitude),
                lat
            )
            editor.putDouble(
                getString(R.string.user_preference_traffic_map_longitude),
                lng
            )
            editor.putFloat(
                getString(R.string.user_preference_traffic_map_zoom),
                12.0f
            )
            editor.apply()

            val alertId = extras.getInt(getString(R.string.push_alert_traffic_alert_id), 0)

            // reset navigation to the traffic map
            findNavController(R.id.nav_host_fragment).navigate(R.id.navTrafficMapFragment)
            findNavController(R.id.nav_host_fragment).popBackStack(R.id.navTrafficMapFragment, false)

            val action = NavGraphDirections.actionGlobalNavHighwayAlertFragment(alertId, "Traffic Alert")
            findNavController(R.id.nav_host_fragment).navigate(action)


        } else if (extras.getBoolean(getString(R.string.push_alert_ferry_alert), false)) {
            Log.e("debug", "got a ferry alert")
            // TODO

            val alertId = extras.getInt(getString(R.string.push_alert_ferry_alert_id), 0)
            val routeId = extras.getInt(getString(R.string.push_alert_ferry_route_id), 0)
            val routeTitle = extras.getString(getString(R.string.push_alert_ferry_route_title), "")

            findNavController(R.id.nav_host_fragment).navigate(R.id.navFerriesHomeFragment)
            findNavController(R.id.nav_host_fragment).popBackStack(R.id.navFerriesHomeFragment, false)

            val actionOne = NavGraphDirections.actionGlobalNavFerriesRouteFragment(routeId, routeTitle)
            findNavController(R.id.nav_host_fragment).navigate(actionOne)

            val actionTwo = NavGraphDirections.actionGlobalNavFerryAlertDetailsFragment(alertId)
            findNavController(R.id.nav_host_fragment).navigate(actionTwo)


        }
    }
    
    override fun getTheme(): Resources.Theme {

        val theme = super.getTheme()

        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val themeId = settings.getInt(getString(R.string.pref_key_theme), 0)

        if (themeId == 1) {
            theme.applyStyle(R.style.ThemeWSDOTOrange, true)
        } else {
            theme.applyStyle(R.style.ThemeWSDOTGreen, true)
        }

        return theme
    }


    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        when (item.itemId) {
            R.id.nav_traffic_map -> {
                if (navController.currentDestination?.id != R.id.navTrafficMapFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navTrafficMapFragment)
                }
            }
            R.id.nav_ferries -> {
                if (navController.currentDestination?.id != R.id.navFerriesHomeFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navFerriesHomeFragment)
                }
            }
            R.id.nav_mountain_passes -> {
                if (navController.currentDestination?.id != R.id.navMountainPassHomeFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navMountainPassHomeFragment)
                }
            }
            R.id.nav_toll_rates -> {
                if (navController.currentDestination?.id != R.id.navTollRatesFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navTollRatesFragment)
                }
            }
            R.id.nav_border_waits -> {
                if (navController.currentDestination?.id != R.id.navBorderCrossingsFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navBorderCrossingsFragment)
                }
            }
            R.id.nav_amtrak_cascades -> {
                if (navController.currentDestination?.id != R.id.navAmtrakCascadesFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navAmtrakCascadesFragment)
                }
            }
            R.id.nav_favorites -> {
                if (navController.currentDestination?.id != R.id.navFavoritesFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navFavoritesFragment)
                }
            }
            R.id.nav_settings -> {
                if(navController.currentDestination?.id != R.id.navSettingsFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navSettingsFragment)
                }
            }
            R.id.nav_about -> {
                if(navController.currentDestination?.id != R.id.navAboutFragment) {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_global_navAboutFragment)
                }
            }
            R.id.event_banner -> {
                if(navController.currentDestination?.id != R.id.navEventDetailsFragment) {
                    val action = NavGraphDirections.actionGlobalNavEventDetailsFragment(eventTitle)
                    findNavController(R.id.nav_host_fragment).navigate(action)
                }
            }
            R.id.nav_notifications -> {
                if(navController.currentDestination?.id != R.id.navNotificationsFragment) {
                    val action = NavGraphDirections.actionGlobalNavNotificationsFragment()
                    findNavController(R.id.nav_host_fragment).navigate(action)
                }
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    /**
     * Initialize and display ads.
     */
     fun enableAds(target: String) {
        val mAdViewBox: LinearLayout = drawerLayout.findViewById(R.id.ad_banner_box)
        mAdViewBox.visibility = VISIBLE

        val mAdView: PublisherAdView = drawerLayout.findViewById(R.id.publisherAdView)

        val adRequest = PublisherAdRequest.Builder()
            .addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR) // All emulators
            .addCustomTargeting("wsdotapp", target)
            .build()

       // mAdView.visibility = View.GONE
        mAdView.adListener = null

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                mAdView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(error: Int) {
                super.onAdFailedToLoad(error)
                when (error) {
                    AdRequest.ERROR_CODE_NO_FILL -> Log.e("debug", "no fill")
                    AdRequest.ERROR_CODE_INVALID_REQUEST -> Log.e("debug", "invalid request")
                    AdRequest.ERROR_CODE_NETWORK_ERROR -> Log.e("debug", "network error")
                    AdRequest.ERROR_CODE_INTERNAL_ERROR -> Log.e("debug", "internal error")
                }
            }
        }
        mAdView.loadAd(adRequest)
    }

    /**
     * Remove the ad so it doesn't take up any space.
     */
    fun disableAds() {
        val mAdView: PublisherAdView = drawerLayout.findViewById(R.id.publisherAdView)
        val mAdViewBox: LinearLayout = drawerLayout.findViewById(R.id.ad_banner_box)
        mAdViewBox.visibility = GONE
        mAdView.visibility = GONE
        mAdView.pause()
    }

    fun setScreenName(screenName: String) {
        firebaseAnalytics.setCurrentScreen(this, screenName, null)
    }

    // Pref change listener
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, prefKey: String?) {
        setDarkMode(sharedPreferences, prefKey)
    }

    private fun setDarkMode(sharedPreferences: SharedPreferences?, prefKey: String?) {

        sharedPreferences?.let { prefs ->
            if (prefKey == resources.getString(R.string.key_darkmode)) {
                val darkmode: Boolean = prefs.getBoolean(prefKey, false)

                if (darkmode) {
                    setDefaultNightMode(MODE_NIGHT_YES)
                } else {
                    setDefaultNightMode(MODE_NIGHT_NO)
                }

            } else if (prefKey == resources.getString(R.string.key_darkmodesystem)) {
                val useSystem = prefs.getBoolean(prefKey, true)
                if (useSystem) {
                    setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    if (prefs.getBoolean(resources.getString(R.string.key_darkmode), false)) {
                        setDefaultNightMode(MODE_NIGHT_YES)
                    } else {
                        setDefaultNightMode(MODE_NIGHT_NO)
                    }
                }
            }
        }
    }

}