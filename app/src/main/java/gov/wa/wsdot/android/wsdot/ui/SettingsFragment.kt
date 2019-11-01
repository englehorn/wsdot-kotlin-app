package gov.wa.wsdot.android.wsdot.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import gov.wa.wsdot.android.wsdot.R
import android.content.SharedPreferences

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(activity as SharedPreferences.OnSharedPreferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the listener whenever a key changes
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(activity as SharedPreferences.OnSharedPreferenceChangeListener)
    }

}