package com.AsDfGhJ.jMiKoLp.presentation.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.AsDfGhJ.jMiKoLp.MainActivity
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.di.ViewModelFactory
import com.AsDfGhJ.jMiKoLp.core.ext.ViewExt
import com.AsDfGhJ.jMiKoLp.databinding.FragmentMenuBinding
import com.AsDfGhJ.jMiKoLp.presentation.dialog.RouteMapDialog
import com.AsDfGhJ.jMiKoLp.presentation.dialog.SettingsDialog
import com.AsDfGhJ.jMiKoLp.presentation.dialog.TutorialDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private var binding: FragmentMenuBinding? = null

    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = FragmentMenuBinding.bind(view)
        binding = bound
        wireActions(bound)
        observeState()
        animateEntrance(bound)
        parentFragmentManager.setFragmentResultListener(
            RouteMapDialog.RESULT_KEY,
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.refresh()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun wireActions(bound: FragmentMenuBinding) {
        ViewExt.bindPressFeedback(bound.menuPlay)
        ViewExt.bindPressFeedback(bound.menuRouteMap)
        ViewExt.bindPressFeedback(bound.menuSignGuide)
        bound.menuPlay.setOnClickListener {
            val host = activity as? MainActivity ?: return@setOnClickListener
            val state = viewModel.state.value
            host.navigator.showGame(state.selectedTier, state.nextRouteNumber)
        }
        bound.menuRouteMap.setOnClickListener {
            RouteMapDialog.newInstance(false).show(parentFragmentManager, RouteMapDialog.TAG)
        }
        bound.menuSignGuide.setOnClickListener {
            TutorialDialog().show(parentFragmentManager, TutorialDialog.TAG)
        }
        bound.menuSettings.setOnClickListener {
            SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    render(state)
                }
            }
        }
    }

    private fun render(state: MenuUiState) {
        val bound = binding ?: return
        bound.menuStatRoutes.bind(
            value = if (state.routesCleared > 0) state.routesCleared.toString() else null,
            label = getString(R.string.menu_stat_routes),
            accentColorRes = R.color.accent_go
        )
        bound.menuStatStars.bind(
            value = if (state.totalStars > 0) state.totalStars.toString() else null,
            label = getString(R.string.menu_stat_stars),
            accentColorRes = R.color.accent_primary
        )
        bound.menuStatBest.bind(
            value = if (state.bestCodeLength > 0) state.bestCodeLength.toString() else null,
            label = getString(R.string.menu_stat_best),
            accentColorRes = R.color.accent_cool
        )
        bound.menuRouteChip.text = getString(
            R.string.fmt_route_chip,
            state.nextRouteNumber,
            state.selectedTier.codeLength
        )
    }

    private fun animateEntrance(bound: FragmentMenuBinding) {
        val reduced = ServiceLocator.container(requireContext())
            .settingsRepository.settings().reducedMotion
        ViewExt.riseIn(bound.menuSheet, 0L, ViewExt.dp(bound.menuSheet, 36f), reduced)
        ViewExt.popIn(bound.menuStatRoutes, 120L, reduced)
        ViewExt.popIn(bound.menuStatStars, 180L, reduced)
        ViewExt.popIn(bound.menuStatBest, 240L, reduced)
        ViewExt.popIn(bound.menuPlay, 300L, reduced)
    }

    override fun onDestroyView() {
        val bound = binding
        if (bound != null) {
            ViewExt.cancelAnimations(
                bound.menuSheet,
                bound.menuStatRoutes,
                bound.menuStatStars,
                bound.menuStatBest,
                bound.menuPlay
            )
        }
        binding = null
        super.onDestroyView()
    }
}
