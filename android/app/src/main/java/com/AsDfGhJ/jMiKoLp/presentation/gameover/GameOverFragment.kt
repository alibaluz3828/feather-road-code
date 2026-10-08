package com.AsDfGhJ.jMiKoLp.presentation.gameover

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
import com.AsDfGhJ.jMiKoLp.databinding.FragmentGameOverBinding
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.presentation.dialog.RouteMapDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GameOverFragment : Fragment(R.layout.fragment_game_over) {

    private var binding: FragmentGameOverBinding? = null

    private val viewModel: GameOverViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = FragmentGameOverBinding.bind(view)
        binding = bound
        wireActions(bound)
        observeState()
        loadFromArguments()
        parentFragmentManager.setFragmentResultListener(
            RouteMapDialog.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val tierId = result.getString(RouteMapDialog.RESULT_TIER)
            startTier(RouteTier.fromId(tierId))
        }
    }

    private fun loadFromArguments() {
        val args = arguments ?: return
        viewModel.load(
            tier = RouteTier.fromId(args.getString(ARG_TIER)),
            routeNumber = args.getInt(ARG_ROUTE, 1),
            cleared = args.getBoolean(ARG_CLEARED, false),
            stars = args.getInt(ARG_STARS, 0),
            actionsUsed = args.getInt(ARG_ACTIONS, 0),
            launches = args.getInt(ARG_LAUNCHES, 0),
            clearedSegments = args.getInt(ARG_SEGMENTS, 0),
            failedAt = args.getInt(ARG_FAILED_AT, -1),
            codeOrdinals = args.getIntArray(ARG_CODE) ?: IntArray(0),
            outOfActions = args.getBoolean(ARG_OUT_OF_ACTIONS, false)
        )
    }

    private fun wireActions(bound: FragmentGameOverBinding) {
        ViewExt.bindPressFeedback(bound.resultPlayAgain)
        ViewExt.bindPressFeedback(bound.resultNextRoute)
        ViewExt.bindPressFeedback(bound.resultMenu)
        bound.resultPlayAgain.setOnClickListener {
            startTier(viewModel.state.value.tier)
        }
        bound.resultNextRoute.setOnClickListener {
            RouteMapDialog.newInstance(true).show(parentFragmentManager, RouteMapDialog.TAG)
        }
        bound.resultMenu.setOnClickListener {
            (activity as? MainActivity)?.navigator?.backToMenu()
        }
    }

    private fun startTier(tier: RouteTier) {
        if (!isAdded || view == null) {
            return
        }
        val host = activity as? MainActivity ?: return
        host.navigator.restartGame(tier, viewModel.state.value.nextRouteNumber)
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

    private fun render(state: GameOverUiState) {
        val bound = binding ?: return
        val verdict = when {
            state.cleared -> R.string.result_cleared
            state.outOfActions -> R.string.result_out_of_actions
            else -> R.string.result_shut_down
        }
        bound.resultVerdict.setText(verdict)
        bound.resultVerdict.setBackgroundResource(
            if (state.cleared) R.drawable.shape_plate_go else R.drawable.shape_plate_hazard
        )
        bound.resultVerdict.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (state.cleared) R.color.ink else R.color.text_primary
            )
        )
        bound.resultHazard.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                if (state.cleared) R.color.accent_go else R.color.accent_hazard
            )
        )
        renderStars(bound, state.stars)
        bound.resultRecap.bind(
            label = getString(R.string.result_route_travelled),
            code = state.code,
            states = state.chipStates
        )
        bound.resultStatActions.bind(
            value = if (state.actionsUsed > 0) state.actionsUsed.toString() else null,
            label = getString(R.string.result_stat_actions),
            accentColorRes = R.color.accent_cool
        )
        bound.resultStatLaunches.bind(
            value = if (state.launches > 0) state.launches.toString() else null,
            label = getString(R.string.result_stat_launches),
            accentColorRes = R.color.accent_hazard
        )
        bound.resultStatStars.bind(
            value = if (state.stars > 0) getString(R.string.fmt_stars_of, state.stars) else null,
            label = getString(R.string.result_stat_stars),
            accentColorRes = R.color.accent_primary
        )
        bound.resultHint.text = getString(R.string.fmt_next_up, state.nextTier.codeLength)
    }

    private fun renderStars(bound: FragmentGameOverBinding, stars: Int) {
        val slots = listOf(bound.resultStarOne, bound.resultStarTwo, bound.resultStarThree)
        val reduced = ServiceLocator.container(requireContext())
            .settingsRepository.settings().reducedMotion
        for (index in slots.indices) {
            val earned = index < stars
            applyStar(slots[index], earned)
            if (earned) {
                ViewExt.popIn(slots[index], STAR_STAGGER_MS * index, reduced)
            }
        }
    }

    private fun applyStar(view: ImageView, earned: Boolean) {
        view.setBackgroundResource(
            if (earned) R.drawable.shape_plate_gold else R.drawable.shape_plate_dim
        )
        view.alpha = if (earned) 1f else LOCKED_ALPHA
        view.contentDescription = getString(
            if (earned) R.string.cd_star_earned else R.string.cd_star_locked
        )
    }

    override fun onDestroyView() {
        val bound = binding
        if (bound != null) {
            ViewExt.cancelAnimations(
                bound.resultPlayAgain,
                bound.resultStarOne,
                bound.resultStarTwo,
                bound.resultStarThree
            )
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_TIER = "arg_tier"
        private const val ARG_ROUTE = "arg_route"
        private const val ARG_CLEARED = "arg_cleared"
        private const val ARG_STARS = "arg_stars"
        private const val ARG_ACTIONS = "arg_actions"
        private const val ARG_LAUNCHES = "arg_launches"
        private const val ARG_SEGMENTS = "arg_segments"
        private const val ARG_FAILED_AT = "arg_failed_at"
        private const val ARG_CODE = "arg_code"
        private const val ARG_OUT_OF_ACTIONS = "arg_out_of_actions"
        private const val STAR_STAGGER_MS = 180L
        private const val LOCKED_ALPHA = 0.22f

        fun newInstance(
            tier: RouteTier,
            routeNumber: Int,
            cleared: Boolean,
            stars: Int,
            actionsUsed: Int,
            launches: Int,
            clearedSegments: Int,
            failedAt: Int,
            codeOrdinals: IntArray,
            outOfActions: Boolean
        ): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putString(ARG_TIER, tier.id)
            args.putInt(ARG_ROUTE, routeNumber)
            args.putBoolean(ARG_CLEARED, cleared)
            args.putInt(ARG_STARS, stars)
            args.putInt(ARG_ACTIONS, actionsUsed)
            args.putInt(ARG_LAUNCHES, launches)
            args.putInt(ARG_SEGMENTS, clearedSegments)
            args.putInt(ARG_FAILED_AT, failedAt)
            args.putIntArray(ARG_CODE, codeOrdinals)
            args.putBoolean(ARG_OUT_OF_ACTIONS, outOfActions)
            fragment.arguments = args
            return fragment
        }
    }
}
