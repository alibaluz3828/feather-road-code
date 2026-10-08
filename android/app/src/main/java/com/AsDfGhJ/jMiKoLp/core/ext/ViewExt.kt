package com.AsDfGhJ.jMiKoLp.core.ext

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import com.AsDfGhJ.jMiKoLp.core.config.GameConfig

object ViewExt {

    @SuppressLint("ClickableViewAccessibility")
    fun bindPressFeedback(view: View) {
        view.setOnTouchListener { target, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> target.animate()
                    .scaleX(PRESS_SCALE)
                    .scaleY(PRESS_SCALE)
                    .setDuration(GameConfig.PRESS_DOWN_MS)
                    .start()

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> target.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setInterpolator(OvershootInterpolator(2f))
                    .setDuration(GameConfig.PRESS_UP_MS)
                    .start()
            }
            false
        }
    }

    fun popIn(view: View, delayMs: Long, reducedMotion: Boolean) {
        if (reducedMotion) {
            view.alpha = 1f
            view.scaleX = 1f
            view.scaleY = 1f
            view.translationY = 0f
            return
        }
        view.alpha = 0f
        view.scaleX = 0.9f
        view.scaleY = 0.9f
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(delayMs)
            .setInterpolator(OvershootInterpolator(1.8f))
            .setDuration(320L)
            .start()
    }

    fun riseIn(view: View, delayMs: Long, distancePx: Float, reducedMotion: Boolean) {
        if (reducedMotion) {
            view.alpha = 1f
            view.translationY = 0f
            return
        }
        view.alpha = 0f
        view.translationY = distancePx
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delayMs)
            .setDuration(420L)
            .start()
    }

    fun cancelAnimations(vararg views: View?) {
        for (view in views) {
            view?.animate()?.cancel()
        }
    }

    fun dp(view: View, value: Float): Float = value * view.resources.displayMetrics.density

    private const val PRESS_SCALE = 0.96f
}
