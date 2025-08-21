/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.display

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.os.SystemProperties
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.preference.PreferenceCategory
import com.android.settings.preferences.RadioButtonPreference
import com.android.settings.preferences.BasePreferenceFragment
import com.android.settings.R

class RefreshRateSettings : BasePreferenceFragment(R.xml.refresh_rate_settings),
    RadioButtonPreference.OnRadioButtonClickedListener {

    private lateinit var radioPrefs: MutableList<RadioButtonPreference>
    private val cr by lazy { requireContext().contentResolver }

    data class RefreshRateMode(
        val key: String,
        val titleRes: Int,
        val summaryRes: Int,
        val modeSummaryRes: Int,
        val modeValue: Int,
        val rateValue: Int
    )

    private val modeMap = mutableMapOf<String, RefreshRateMode>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupVRRModes()
        setupRadioButtons()
        refreshRadios()
    }

    private fun setupVRRModes() {
        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val refreshRates = display.supportedModes.map { it.refreshRate.toInt() }.distinct().sorted()
        if (refreshRates.isEmpty()) return

        val minRate = refreshRates.first()
        val maxRate = refreshRates.last()

        val supportsDynamic = SystemProperties.getBoolean(
            "ro.surface_flinger.use_content_detection_for_refresh_rate", false
        )

        if (supportsDynamic) {
            modeMap["refresh_rate_dynamic"] = RefreshRateMode(
                "refresh_rate_dynamic",
                R.string.refresh_rate_dynamic,
                R.string.refresh_rate_dynamic_summary,
                0,
                0,
                0
            )
        }

        modeMap["refresh_rate_high"] = RefreshRateMode(
            "refresh_rate_high",
            R.string.refresh_rate_high,
            R.string.refresh_rate_summary,
            R.string.refresh_rate_high_summary,
            1,
            maxRate
        )

        modeMap["refresh_rate_standard"] = RefreshRateMode(
            "refresh_rate_standard",
            R.string.refresh_rate_standard,
            R.string.refresh_rate_summary,
            R.string.refresh_rate_standard_summary,
            2,
            minRate
        )
    }

    private fun setupRadioButtons() {
        val category = findPreference<PreferenceCategory>("refresh_rate_category") ?: return
        radioPrefs = mutableListOf()

        modeMap.values.forEach { mode ->
            val radio = RadioButtonPreference(requireContext()).apply {
                key = mode.key
                title = getString(mode.titleRes)
                val summaryStart = getString(mode.summaryRes)
                val rates = if (mode.rateValue != 0) "${mode.rateValue}Hz " else ""
                val modeSummary = if (mode.modeSummaryRes != 0) getString(mode.modeSummaryRes) else ""
                // concatenation is intended for our tranlator script, it cant handle all format specifiers on some languages yet
                summary = "$summaryStart $rates$modeSummary"
                setOnRadioButtonClickedListener(this@RefreshRateSettings)
            }
            radioPrefs.add(radio)
            category.addPreference(radio)
        }
    }

    override fun onRadioButtonClicked(pref: RadioButtonPreference) {
        val mode = modeMap[pref.key]?.modeValue ?: return
        Settings.Global.putInt(cr, "display_refresh_rate_mode", mode)
        refreshRadios()
    }

    private fun refreshRadios() {
        val currentMode = Settings.Global.getInt(cr, "display_refresh_rate_mode", 0)
        radioPrefs.forEach { radio ->
            val modeValue = modeMap[radio.key]?.modeValue ?: 0
            radio.isSelected = currentMode == modeValue
        }
    }
}
