package com.AsDfGhJ.jMiKoLp.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.databinding.ViewSignTokenBinding
import com.AsDfGhJ.jMiKoLp.domain.model.Direction

class SignTokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSignTokenBinding.inflate(LayoutInflater.from(context), this)

    var direction: Direction = Direction.UP
        private set

    fun bind(direction: Direction, remaining: Int, selected: Boolean) {
        this.direction = direction
        val label = context.getString(CodeChipStripView.labelFor(direction))
        binding.tokenGlyph.setImageResource(CodeChipStripView.glyphFor(direction))
        val exhausted = remaining <= 0
        val background = when {
            exhausted -> R.drawable.shape_token_empty
            selected -> R.drawable.shape_token_selected
            else -> R.drawable.shape_token_idle
        }
        setBackgroundResource(background)
        val tint = when {
            exhausted -> R.color.text_muted
            selected -> R.color.ink
            else -> R.color.accent_primary
        }
        binding.tokenGlyph.setColorFilter(ContextCompat.getColor(context, tint))
        alpha = if (exhausted) EXHAUSTED_ALPHA else 1f
        isEnabled = !exhausted
        isClickable = !exhausted
        binding.tokenBadge.text = context.getString(R.string.fmt_token_badge, remaining)
        binding.tokenBadge.visibility = if (remaining > 1) View.VISIBLE else View.GONE
        contentDescription = label
        val description = when {
            exhausted -> context.getString(R.string.sd_token_empty, label)
            selected -> context.getString(R.string.sd_token_selected, label)
            else -> context.getString(R.string.sd_token_available, label, remaining)
        }
        ViewCompat.setStateDescription(this, description)
    }

    private companion object {
        const val EXHAUSTED_ALPHA = 0.35f
    }
}
