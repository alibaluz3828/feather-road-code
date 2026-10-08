package com.AsDfGhJ.jMiKoLp.presentation.game

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator

class CartRunAnimator(private val density: Float) {

    private val active = ArrayList<ValueAnimator>()

    fun shake(target: View, reduced: Boolean) {
        if (reduced) {
            return
        }
        val animator = ObjectAnimator.ofFloat(target, View.TRANSLATION_X, 0f, SHAKE_DP * density)
        animator.duration = SHAKE_MS
        animator.interpolator = CycleInterpolator(SHAKE_CYCLES)
        animator.start()
        active.add(animator)
    }

    fun flash(target: View, reduced: Boolean) {
        if (reduced) {
            return
        }
        val base = target.alpha
        val animator = ValueAnimator.ofFloat(base, FLASH_PEAK, base)
        animator.duration = FLASH_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                target.alpha = value
            }
        }
        animator.start()
        active.add(animator)
    }

    fun popCounter(target: View, reduced: Boolean) {
        if (reduced) {
            target.scaleX = 1f
            target.scaleY = 1f
            return
        }
        val animator = ValueAnimator.ofFloat(1f, COUNTER_PEAK, 1f)
        animator.duration = COUNTER_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                target.scaleX = value
                target.scaleY = value
            }
        }
        animator.start()
        active.add(animator)
    }

    fun pulse(target: View, reduced: Boolean) {
        if (reduced) {
            return
        }
        val animator = ValueAnimator.ofFloat(1f, PULSE_PEAK, 1f)
        animator.duration = PULSE_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                target.scaleX = value
                target.scaleY = value
            }
        }
        animator.start()
        active.add(animator)
    }

    fun cancel() {
        for (animator in active) {
            animator.removeAllUpdateListeners()
            animator.cancel()
        }
        active.clear()
    }

    private companion object {
        const val SHAKE_DP = 8f
        const val SHAKE_MS = 600L
        const val SHAKE_CYCLES = 5f
        const val FLASH_PEAK = 0.85f
        const val FLASH_MS = 420L
        const val COUNTER_PEAK = 1.25f
        const val COUNTER_MS = 180L
        const val PULSE_PEAK = 1.18f
        const val PULSE_MS = 220L
    }
}
