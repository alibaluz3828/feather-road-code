package com.AsDfGhJ.jMiKoLp.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        background = ContextCompat.getDrawable(context, R.drawable.shape_block_surface)
        val horizontal = resources.getDimensionPixelSize(R.dimen.space_s)
        val vertical = resources.getDimensionPixelSize(R.dimen.space_xs)
        setPadding(horizontal, vertical, horizontal, vertical)
    }

    fun bind(value: String?, label: String, accentColorRes: Int) {
        val accent = ContextCompat.getColor(context, accentColorRes)
        val muted = ContextCompat.getColor(context, R.color.text_muted)
        val hasValue = !value.isNullOrEmpty()
        binding.statValue.text = if (hasValue) value else context.getString(R.string.value_placeholder)
        binding.statValue.setTextColor(if (hasValue) accent else muted)
        binding.statLabel.text = label
        binding.statNotch.backgroundTintList =
            ColorStateList.valueOf(if (hasValue) accent else muted)
        contentDescription = label
        ViewCompat.setStateDescription(
            this,
            if (hasValue) value else context.getString(R.string.value_placeholder)
        )
    }
}
