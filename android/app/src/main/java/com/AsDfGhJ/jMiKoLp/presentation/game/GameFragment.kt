package com.AsDfGhJ.jMiKoLp.presentation.game

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
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
import com.AsDfGhJ.jMiKoLp.core.ui.SignTokenView
import com.AsDfGhJ.jMiKoLp.databinding.FragmentGameBinding
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameFragment : Fragment(R.layout.fragment_game) {

    private var binding: FragmentGameBinding? = null
    private var animator: CartRunAnimator? = null
    private var lastCrashPulse = 0
    private var lastClearPulse = 0
    private var lastActions = -1

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = FragmentGameBinding.bind(view)
        binding = bound
        animator = CartRunAnimator(resources.displayMetrics.density)
        wireActions(bound)
        observeState()
        observeResult()
        val tier = RouteTier.fromId(arguments?.getString(ARG_TIER))
        val routeNumber = arguments?.getInt(ARG_ROUTE, 1) ?: 1
        viewModel.start(tier, routeNumber)
    }

    private fun wireActions(bound: FragmentGameBinding) {
        ViewExt.bindPressFeedback(bound.gameGo)
        bound.gameGo.setOnClickListener {
            viewModel.onRealInput()
            viewModel.launch()
        }
        bound.gameBack.setOnClickListener {
            viewModel.onRealInput()
            (activity as? MainActivity)?.navigator?.back()
        }
        bound.gameBoard.onSocketTapped = { cell ->
            viewModel.onRealInput()
            viewModel.tapSocket(cell)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest { state ->
                    if (state != null) {
                        render(state)
                    }
                }
            }
        }
    }

    private fun observeResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.result.collectLatest { result ->
                    if (result != null) {
                        navigateToResult(result)
                    }
                }
            }
        }
    }

    private fun navigateToResult(result: GameResult) {
        if (!isAdded || view == null) {
            return
        }
        val host = activity as? MainActivity ?: return
        val moved = host.navigator.showResult(
            tier = result.tier,
            routeNumber = result.routeNumber,
            cleared = result.cleared,
            stars = result.stars,
            actionsUsed = result.actionsUsed,
            launches = result.launches,
            clearedSegments = result.clearedSegments,
            failedAt = result.failedAt,
            codeOrdinals = result.codeOrdinals,
            outOfActions = result.outOfActions
        )
        if (moved) {
            viewModel.consumeResult()
        }
    }

    private fun render(state: GameUiState) {
        val bound = binding ?: return
        bound.gameRouteName.text = getString(R.string.fmt_route, state.plan.routeNumber)
        bound.gameCodeLabel.text = getString(R.string.fmt_code, state.plan.codeLength)
        bound.gameActionsValue.text = state.actionsLeft.toString()
        ViewCompat.setStateDescription(
            bound.gameActionsValue,
            getString(R.string.sd_actions_left, state.actionsLeft)
        )
        bound.gameCodeStrip.bind(
            label = getString(R.string.game_route_code),
            code = state.plan.code,
            states = state.chipStates
        )
        bound.gameBoard.submitPlan(state.plan)
        bound.gameBoard.submitState(
            placements = state.placements,
            trail = state.trail,
            passedCheckpoints = state.passedCheckpoints,
            cartCell = state.cartCell,
            cartHeading = state.cartHeading,
            selectedSocket = state.selectedSocket,
            highContrast = state.highContrast
        )
        bound.gameStatLaunches.bind(
            value = getString(R.string.fmt_fraction, state.launches, MAX_LAUNCHES),
            label = getString(R.string.game_launches),
            accentColorRes = R.color.accent_hazard
        )
        bound.gameStatSigns.bind(
            value = getString(R.string.fmt_fraction, state.signsPlaced, state.socketCount),
            label = getString(R.string.game_signs),
            accentColorRes = R.color.accent_cool
        )
        ViewCompat.setStateDescription(
            bound.gameStatLaunches,
            getString(R.string.sd_launches_used, state.launches, MAX_LAUNCHES)
        )
        ViewCompat.setStateDescription(
            bound.gameStatSigns,
            getString(R.string.sd_signs_placed, state.signsPlaced, state.socketCount)
        )
        bound.gameCoach.visibility = if (state.coachVisible) View.VISIBLE else View.GONE
        bound.gameGo.isEnabled = true
        renderDock(bound.gameTray, state)
        renderPulses(bound, state)
    }

    private fun renderDock(tray: LinearLayout, state: GameUiState) {
        if (tray.childCount != state.dock.size) {
            tray.removeAllViews()
            val size = resources.getDimensionPixelSize(R.dimen.token_size)
            val gap = resources.getDimensionPixelSize(R.dimen.chip_gap)
            for (index in state.dock.indices) {
                val token = SignTokenView(tray.context)
                val params = LinearLayout.LayoutParams(size, size)
                if (index > 0) {
                    params.marginStart = gap
                }
                token.layoutParams = params
                token.setOnClickListener {
                    viewModel.onRealInput()
                    viewModel.selectDirection(token.direction)
                }
                tray.addView(token)
            }
        }
        for (index in state.dock.indices) {
            val child = tray.getChildAt(index)
            if (child is SignTokenView) {
                val slot = state.dock[index]
                child.bind(slot.direction, slot.remaining, slot.selected)
            }
        }
    }

    private fun renderPulses(bound: FragmentGameBinding, state: GameUiState) {
        val helper = animator ?: return
        if (state.crashPulse != lastCrashPulse) {
            lastCrashPulse = state.crashPulse
            helper.shake(bound.gameBoard, state.reducedMotion)
        }
        if (state.clearPulse != lastClearPulse) {
            lastClearPulse = state.clearPulse
            helper.flash(bound.gameBackdrop, state.reducedMotion)
        }
        if (lastActions >= 0 && state.actionsLeft != lastActions) {
            helper.popCounter(bound.gameActionsValue, state.reducedMotion)
        }
        lastActions = state.actionsLeft
    }

    override fun onDestroyView() {
        animator?.cancel()
        animator = null
        val bound = binding
        if (bound != null) {
            bound.gameBoard.onSocketTapped = null
            bound.gameBoard.clearSprites()
            ViewExt.cancelAnimations(bound.gameGo, bound.gameBoard, bound.gameActionsValue)
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TIER = "arg_tier"
        private const val ARG_ROUTE = "arg_route"
        private const val MAX_LAUNCHES = 3

        fun newInstance(tier: RouteTier, routeNumber: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putString(ARG_TIER, tier.id)
            args.putInt(ARG_ROUTE, routeNumber)
            fragment.arguments = args
            return fragment
        }
    }
}
