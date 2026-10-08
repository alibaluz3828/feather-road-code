package com.AsDfGhJ.jMiKoLp.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.ext.ViewExt
import com.AsDfGhJ.jMiKoLp.databinding.DialogSettingsBinding
import com.AsDfGhJ.jMiKoLp.domain.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsDialog : DialogFragment() {

    private var binding: DialogSettingsBinding? = null
    private var confirmJob: Job? = null
    private var confirmArmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_FeatherRoadCode_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bound = DialogSettingsBinding.inflate(inflater, container, false)
        binding = bound
        return bound.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        val container = ServiceLocator.container(requireContext())
        val settings = container.settingsRepository
        val current = settings.settings()
        bound.settingsHaptics.isChecked = current.haptics
        bound.settingsContrast.isChecked = current.highContrast
        bound.settingsMotion.isChecked = current.reducedMotion
        describe(bound.settingsHaptics, current.haptics)
        describe(bound.settingsContrast, current.highContrast)
        describe(bound.settingsMotion, current.reducedMotion)
        wireSwitches(bound, settings)
        ViewExt.bindPressFeedback(bound.settingsReset)
        ViewExt.bindPressFeedback(bound.settingsClose)
        bound.settingsReset.setOnClickListener {
            onResetTapped(container.progressRepository::reset)
        }
        bound.settingsClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun wireSwitches(bound: DialogSettingsBinding, settings: SettingsRepository) {
        bound.settingsHaptics.setOnCheckedChangeListener { button, checked ->
            settings.setHaptics(checked)
            describe(button, checked)
        }
        bound.settingsContrast.setOnCheckedChangeListener { button, checked ->
            settings.setHighContrast(checked)
            describe(button, checked)
        }
        bound.settingsMotion.setOnCheckedChangeListener { button, checked ->
            settings.setReducedMotion(checked)
            describe(button, checked)
        }
    }

    private fun describe(button: CompoundButton, checked: Boolean) {
        ViewCompat.setStateDescription(
            button,
            getString(if (checked) R.string.sd_switch_on else R.string.sd_switch_off)
        )
    }

    private fun onResetTapped(action: () -> Unit) {
        val bound = binding ?: return
        if (!confirmArmed) {
            confirmArmed = true
            bound.settingsReset.setText(R.string.settings_reset_confirm)
            confirmJob?.cancel()
            confirmJob = viewLifecycleOwner.lifecycleScope.launch {
                delay(CONFIRM_WINDOW_MS)
                val active = binding ?: return@launch
                confirmArmed = false
                active.settingsReset.setText(R.string.settings_reset)
            }
            return
        }
        confirmJob?.cancel()
        confirmJob = null
        confirmArmed = false
        action()
        bound.settingsReset.setText(R.string.settings_reset)
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        confirmJob?.cancel()
        confirmJob = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
        private const val CONFIRM_WINDOW_MS = 4000L
    }
}
