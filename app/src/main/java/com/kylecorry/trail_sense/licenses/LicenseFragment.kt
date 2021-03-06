package com.kylecorry.trail_sense.licenses

import android.os.Bundle
import androidx.preference.*
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.system.UiUtils


class LicenseFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.licenses, rootKey)

        val licenseSection = preferenceManager.findPreference<PreferenceCategory>(getString(R.string.pref_category_licenses))

        for (library in Licenses.libraries){
            val pref = Preference(requireContext())
            pref.title = library.name
            pref.summary = library.url
            pref.isIconSpaceReserved = false
            pref.setOnPreferenceClickListener {
                UiUtils.alert(requireContext(), library.name, library.license)
                true
            }
            licenseSection?.addPreference(pref)
        }
    }



}