package com.AsDfGhJ.jMiKoLp.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.core.config.GameConfig
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.ext.ViewExt
import com.AsDfGhJ.jMiKoLp.core.ui.SignTokenView
import com.AsDfGhJ.jMiKoLp.data.sample.SampleData
import com.AsDfGhJ.jMiKoLp.databinding.DialogTutorialBinding
import com.AsDfGhJ.jMiKoLp.databinding.ItemTutorialStepBinding
import com.AsDfGhJ.jMiKoLp.domain.model.Cell
import com.AsDfGhJ.jMiKoLp.domain.model.Direction
import com.AsDfGhJ.jMiKoLp.domain.model.RoutePlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TutorialDialog : DialogFragment() {

    private var binding: DialogTutorialBinding? = null
    private var plan: RoutePlan? = null
    private var selected: Direction? = null
    private var placements: Map<Cell, Direction> = emptyMap()
    private var solved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_FeatherRoadCode_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bound = DialogTutorialBinding.inflate(inflater, container, false)
        binding = bound
        return bound.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        buildSteps(bound)
        val demo = ServiceLocator.container(requireContext()).routeRepository.tutorialRoute()
        plan = demo
        bound.tutorialBoard.submitPlan(demo)
        refreshBoard()
        bound.tutorialBoard.onSocketTapped = { cell ->
            onSocketTapped(cell)
        }
        buildTray(bound.tutorialTray)
        ViewExt.bindPressFeedback(bound.tutorialGotIt)
        bound.tutorialGotIt.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun buildSteps(bound: DialogTutorialBinding) {
        bound.tutorialSteps.removeAllViews()
        val titles = listOf(
            R.string.tutorial_step_one_title,
            R.string.tutorial_step_two_title,
            R.string.tutorial_step_three_title
        )
        val bodies = listOf(
            R.string.tutorial_step_one_body,
            R.string.tutorial_step_two_body,
            R.string.tutorial_step_three_body
        )
        for (index in titles.indices) {
            val step = ItemTutorialStepBinding.inflate(layoutInflater, bound.tutorialSteps, false)
            step.stepIndex.text = (index + 1).toString()
            step.stepTitle.setText(titles[index])
            step.stepBody.setText(bodies[index])
            step.stepTitle.contentDescription = SampleData.COACH_LINES[index]
            val params = step.root.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                params.topMargin = resources.getDimensionPixelSize(R.dimen.space_s)
                step.root.layoutParams = params
            }
            bound.tutorialSteps.addView(step.root)
        }
    }

    private fun buildTray(tray: LinearLayout) {
        tray.removeAllViews()
        val size = resources.getDimensionPixelSize(R.dimen.token_size)
        val gap = resources.getDimensionPixelSize(R.dimen.chip_gap)
        val options = listOf(Direction.RIGHT, Direction.DOWN)
        for (index in options.indices) {
            val token = SignTokenView(tray.context)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) {
                params.marginStart = gap
            }
            token.layoutParams = params
            token.bind(options[index], 1, false)
            token.setOnClickListener {
                selected = options[index]
                refreshTray(tray)
            }
            tray.addView(token)
        }
    }

    private fun refreshTray(tray: LinearLayout) {
        for (index in 0 until tray.childCount) {
            val child = tray.getChildAt(index)
            if (child is SignTokenView) {
                child.bind(child.direction, 1, child.direction == selected)
            }
        }
    }

    private fun onSocketTapped(cell: Cell) {
        val demo = plan ?: return
        val choice = selected ?: return
        if (solved) {
            return
        }
        placements = mapOf(cell to choice)
        refreshBoard()
        if (demo.solution[cell] == choice) {
            solved = true
            markStepComplete()
            runDemo(demo)
        }
    }

    private fun markStepComplete() {
        val bound = binding ?: return
        val last = bound.tutorialSteps.getChildAt(bound.tutorialSteps.childCount - 1) ?: return
        last.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.hazard_soft)
        )
    }

    private fun runDemo(demo: RoutePlan) {
        viewLifecycleOwner.lifecycleScope.launch {
            val trail = ArrayList<Cell>()
            var heading = demo.entryHeading
            for (cell in demo.path) {
                val bound = binding ?: return@launch
                val previous = trail.lastOrNull()
                if (previous != null) {
                    heading = headingBetween(previous, cell) ?: heading
                }
                trail.add(cell)
                bound.tutorialBoard.submitState(
                    placements = placements,
                    trail = trail.toList(),
                    passedCheckpoints = demo.checkpoints.filter { trail.contains(it) }.toSet(),
                    cartCell = cell,
                    cartHeading = heading,
                    selectedSocket = null,
                    highContrast = false
                )
                delay(GameConfig.CART_STEP_MS * 2)
            }
        }
    }

    private fun headingBetween(from: Cell, to: Cell): Direction? {
        for (direction in Direction.entries) {
            if (from.step(direction) == to) {
                return direction
            }
        }
        return null
    }

    private fun refreshBoard() {
        val bound = binding ?: return
        val demo = plan ?: return
        bound.tutorialBoard.submitState(
            placements = placements,
            trail = emptyList(),
            passedCheckpoints = emptySet(),
            cartCell = demo.entry,
            cartHeading = demo.entryHeading,
            selectedSocket = demo.sockets.firstOrNull(),
            highContrast = false
        )
    }

    override fun onDestroyView() {
        binding?.tutorialBoard?.onSocketTapped = null
        binding?.tutorialBoard?.clearSprites()
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "tutorial_dialog"
    }
}
