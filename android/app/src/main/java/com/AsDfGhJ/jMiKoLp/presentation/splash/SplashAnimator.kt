package com.AsDfGhJ.jMiKoLp.presentation.splash

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.AsDfGhJ.jMiKoLp.core.config.GameConfig

class SplashAnimator(private val density: Float) {

    private val loops = ArrayList<ValueAnimator>()
    private val entries = ArrayList<View>()

    fun playEntrance(plate: View, title: View, subtitle: View, band: View, reduced: Boolean) {
        entries.clear()
        entries.add(plate)
        entries.add(title)
        entries.add(subtitle)
        entries.add(band)
        if (reduced) {
            for (view in entries) {
                view.alpha = 1f
                view.scaleX = 1f
                view.scaleY = 1f
                view.translationY = 0f
            }
            return
        }
        plate.alpha = 0f
        plate.scaleX = PLATE_START_SCALE
        plate.scaleY = PLATE_START_SCALE
        plate.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(OvershootInterpolator(2.4f))
            .setDuration(PLATE_MS)
            .start()

        title.alpha = 0f
        title.translationY = TITLE_RISE_DP * density
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(TITLE_DELAY_MS)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(TITLE_MS)
            .start()

        subtitle.alpha = 0f
        subtitle.animate()
            .alpha(1f)
            .setStartDelay(SUBTITLE_DELAY_MS)
            .setDuration(SUBTITLE_MS)
            .start()

        band.alpha = 0f
        band.animate()
            .alpha(1f)
            .setStartDelay(TITLE_DELAY_MS)
            .setDuration(SUBTITLE_MS)
            .start()
    }

    fun playLoops(plate: View, band: View, reduced: Boolean) {
        cancelLoops()
        if (reduced) {
            return
        }
        val sweep = ValueAnimator.ofFloat(-SWEEP_DP * density, SWEEP_DP * density)
        sweep.duration = GameConfig.LOOP_SLOW_MS
        sweep.repeatCount = ValueAnimator.INFINITE
        sweep.repeatMode = ValueAnimator.REVERSE
        sweep.interpolator = LinearInterpolator()
        sweep.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                band.translationX = value
            }
        }
        sweep.start()
        loops.add(sweep)

        val breathe = ValueAnimator.ofFloat(1f, PLATE_BREATHE_SCALE)
        breathe.duration = GameConfig.LOOP_FAST_MS
        breathe.repeatCount = ValueAnimator.INFINITE
        breathe.repeatMode = ValueAnimator.REVERSE
        breathe.interpolator = DecelerateInterpolator()
        breathe.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                plate.scaleX = value
                plate.scaleY = value
            }
        }
        breathe.startDelay = PLATE_MS
        breathe.start()
        loops.add(breathe)
    }

    fun playGlow(target: View, reduced: Boolean) {
        if (reduced) {
            return
        }
        val glow = ValueAnimator.ofFloat(GLOW_MIN, 1f)
        glow.duration = GLOW_MS
        glow.repeatCount = ValueAnimator.INFINITE
        glow.repeatMode = ValueAnimator.REVERSE
        glow.interpolator = LinearInterpolator()
        glow.addUpdateListener { animation ->
            val value = animation.animatedValue
            if (value is Float) {
                target.alpha = value
            }
        }
        glow.start()
        loops.add(glow)
    }

    fun cancelLoops() {
        for (animator in loops) {
            animator.removeAllUpdateListeners()
            animator.cancel()
        }
        loops.clear()
    }

    fun cancelAll() {
        cancelLoops()
        for (view in entries) {
            view.animate().cancel()
        }
        entries.clear()
    }

    private companion object {
        const val PLATE_START_SCALE = 0.82f
        const val PLATE_BREATHE_SCALE = 1.04f
        const val PLATE_MS = 520L
        const val TITLE_MS = 460L
        const val TITLE_DELAY_MS = 180L
        const val TITLE_RISE_DP = 28f
        const val SUBTITLE_MS = 300L
        const val SUBTITLE_DELAY_MS = 420L
        const val SWEEP_DP = 56f
        const val GLOW_MIN = 0.45f
        const val GLOW_MS = 1600L
    }
}
