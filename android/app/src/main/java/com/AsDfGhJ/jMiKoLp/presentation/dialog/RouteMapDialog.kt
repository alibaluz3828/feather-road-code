package com.AsDfGhJ.jMiKoLp.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.ext.ViewExt
import com.AsDfGhJ.jMiKoLp.data.sample.SampleData
import com.AsDfGhJ.jMiKoLp.databinding.DialogRouteMapBinding
import com.AsDfGhJ.jMiKoLp.databinding.ItemRouteTierBinding
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier

class RouteMapDialog : DialogFragment() {

    private var binding: DialogRouteMapBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_FeatherRoadCode_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val bound = DialogRouteMapBinding.inflate(inflater, container, false)
        binding = bound
        return bound.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        val container = ServiceLocator.container(requireContext())
        val progress = container.progressRepository
        val snapshot = progress.snapshot()
        val selected = progress.selectedTier()
        bound.routeDialogTiers.removeAllViews()
        for (tier in RouteTier.entries) {
            val row = ItemRouteTierBinding.inflate(layoutInflater, bound.routeDialogTiers, false)
            bindRow(row, tier, tier == selected, snapshot.starsFor(tier))
            row.root.setOnClickListener {
                progress.selectTier(tier)
                setFragmentResult(RESULT_KEY, bundleOf(RESULT_TIER to tier.id))
                dismissAllowingStateLoss()
            }
            val params = row.root.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                params.topMargin = resources.getDimensionPixelSize(R.dimen.space_m)
                row.root.layoutParams = params
            }
            bound.routeDialogTiers.addView(row.root)
        }
        ViewExt.bindPressFeedback(bound.routeDialogClose)
        bound.routeDialogClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun bindRow(
        row: ItemRouteTierBinding,
        tier: RouteTier,
        active: Boolean,
        stars: Int
    ) {
        row.tierName.setText(nameFor(tier))
        row.tierCaption.text = getString(
            R.string.fmt_tier_caption,
            tier.codeLength,
            tier.actionBudget
        )
        val accent = ContextCompat.getColor(requireContext(), accentFor(tier))
        row.tierAccent.setBackgroundColor(accent)
        row.root.setBackgroundResource(
            if (active) R.drawable.shape_plate_dim else R.drawable.shape_block_surface
        )
        row.tierName.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (active) R.color.accent_primary else R.color.text_primary
            )
        )
        val slots = listOf(row.tierStarOne, row.tierStarTwo, row.tierStarThree)
        for (index in slots.indices) {
            slots[index].alpha = if (index < stars) 1f else LOCKED_ALPHA
        }
        val title = getString(nameFor(tier))
        row.root.contentDescription = SampleData.TIER_CAPTIONS[tier] ?: title
        ViewCompat.setStateDescription(
            row.root,
            getString(
                if (active) R.string.sd_tier_selected else R.string.sd_tier_available,
                title
            )
        )
    }

    private fun nameFor(tier: RouteTier): Int = when (tier) {
        RouteTier.SHORT_HAUL -> R.string.tier_short_haul
        RouteTier.STANDARD -> R.string.tier_standard
        RouteTier.LONG_HAUL -> R.string.tier_long_haul
        RouteTier.NIGHT_SHIFT -> R.string.tier_night_shift
    }

    private fun accentFor(tier: RouteTier): Int = when (tier) {
        RouteTier.SHORT_HAUL -> R.color.accent_go
        RouteTier.STANDARD -> R.color.accent_cool
        RouteTier.LONG_HAUL -> R.color.accent_primary
        RouteTier.NIGHT_SHIFT -> R.color.accent_hazard
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "route_map_dialog"
        const val RESULT_KEY = "route_map_result"
        const val RESULT_TIER = "route_map_tier"
        private const val ARG_FROM_RESULT = "arg_from_result"
        private const val LOCKED_ALPHA = 0.25f

        fun newInstance(fromResult: Boolean): RouteMapDialog {
            val dialog = RouteMapDialog()
            val args = Bundle()
            args.putBoolean(ARG_FROM_RESULT, fromResult)
            dialog.arguments = args
            return dialog
        }
    }
}
