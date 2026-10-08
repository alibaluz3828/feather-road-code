package com.AsDfGhJ.jMiKoLp.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.databinding.ViewCodeChipStripBinding
import com.AsDfGhJ.jMiKoLp.domain.model.ChipState
import com.AsDfGhJ.jMiKoLp.domain.model.Direction

class CodeChipStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCodeChipStripBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        background = ContextCompat.getDrawable(context, R.drawable.shape_block_surface)
        val horizontal = resources.getDimensionPixelSize(R.dimen.space_m)
        val vertical = resources.getDimensionPixelSize(R.dimen.space_s)
        setPadding(horizontal, vertical, horizontal, vertical)
    }

    fun bind(label: String, code: List<Direction>, states: List<ChipState>) {
        binding.stripLabel.text = label
        val container = binding.stripChips
        container.removeAllViews()
        val size = resources.getDimensionPixelSize(R.dimen.chip_size)
        val glyph = resources.getDimensionPixelSize(R.dimen.chip_glyph)
        val gap = resources.getDimensionPixelSize(R.dimen.chip_gap)
        val inset = (size - glyph) / 2
        for (index in code.indices) {
            val state = states.getOrElse(index) { ChipState.PENDING }
            val chip = ImageView(context)
            val params = LayoutParams(size, size)
            if (index > 0) {
                params.marginStart = gap
            }
            chip.layoutParams = params
            chip.setPadding(inset, inset, inset, inset)
            chip.scaleType = ImageView.ScaleType.FIT_CENTER
            chip.setBackgroundResource(backgroundFor(state))
            chip.setImageResource(glyphFor(code[index]))
            chip.setColorFilter(ContextCompat.getColor(context, tintFor(state)))
            chip.contentDescription = context.getString(labelFor(code[index]))
            ViewCompat.setStateDescription(
                chip,
                context.getString(descriptionFor(state), index + 1, code.size)
            )
            container.addView(chip)
        }
    }

    private fun backgroundFor(state: ChipState): Int = when (state) {
        ChipState.FIXED -> R.drawable.shape_chip_fixed
        ChipState.PENDING -> R.drawable.shape_chip_pending
        ChipState.CLEARED -> R.drawable.shape_chip_cleared
        ChipState.FAILED -> R.drawable.shape_chip_failed
    }

    private fun tintFor(state: ChipState): Int = when (state) {
        ChipState.FIXED -> R.color.accent_primary
        ChipState.PENDING -> R.color.text_secondary
        ChipState.CLEARED -> R.color.ink
        ChipState.FAILED -> R.color.text_primary
    }

    private fun descriptionFor(state: ChipState): Int = when (state) {
        ChipState.FIXED -> R.string.sd_chip_fixed
        ChipState.PENDING -> R.string.sd_chip_pending
        ChipState.CLEARED -> R.string.sd_chip_cleared
        ChipState.FAILED -> R.string.sd_chip_failed
    }

    companion object {
        fun glyphFor(direction: Direction): Int = when (direction) {
            Direction.UP -> R.drawable.ic_arrow_up
            Direction.RIGHT -> R.drawable.ic_arrow_right
            Direction.DOWN -> R.drawable.ic_arrow_down
            Direction.LEFT -> R.drawable.ic_arrow_left
        }

        fun labelFor(direction: Direction): Int = when (direction) {
            Direction.UP -> R.string.dir_up
            Direction.RIGHT -> R.string.dir_right
            Direction.DOWN -> R.string.dir_down
            Direction.LEFT -> R.string.dir_left
        }
    }
}
