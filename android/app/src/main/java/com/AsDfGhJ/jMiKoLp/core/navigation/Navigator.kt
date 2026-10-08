package com.AsDfGhJ.jMiKoLp.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.domain.model.RouteTier
import com.AsDfGhJ.jMiKoLp.presentation.game.GameFragment
import com.AsDfGhJ.jMiKoLp.presentation.gameover.GameOverFragment
import com.AsDfGhJ.jMiKoLp.presentation.menu.MenuFragment
import com.AsDfGhJ.jMiKoLp.presentation.splash.SplashFragment

class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    fun showSplash(): Boolean = commit(SplashFragment(), null, Motion.FADE)

    fun showMenu(): Boolean = commit(MenuFragment(), null, Motion.FADE)

    fun showGame(tier: RouteTier, routeNumber: Int): Boolean =
        commit(GameFragment.newInstance(tier, routeNumber), TAG_GAME, Motion.SLIDE)

    fun showResult(
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
    ): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        val fragment = GameOverFragment.newInstance(
            tier = tier,
            routeNumber = routeNumber,
            cleared = cleared,
            stars = stars,
            actionsUsed = actionsUsed,
            launches = launches,
            clearedSegments = clearedSegments,
            failedAt = failedAt,
            codeOrdinals = codeOrdinals,
            outOfActions = outOfActions
        )
        return commit(fragment, TAG_RESULT, Motion.RISE)
    }

    fun restartGame(tier: RouteTier, routeNumber: Int): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(TAG_RESULT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return commit(GameFragment.newInstance(tier, routeNumber), TAG_GAME, Motion.SLIDE)
    }

    fun backToMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return true
    }

    fun back(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        if (fragmentManager.backStackEntryCount == 0) {
            return false
        }
        fragmentManager.popBackStack()
        return true
    }

    private fun commit(fragment: Fragment, backStackTag: String?, motion: Motion): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        val transaction = fragmentManager.beginTransaction()
        when (motion) {
            Motion.FADE -> transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )

            Motion.SLIDE -> transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )

            Motion.RISE -> transaction.setCustomAnimations(
                R.anim.slide_in_up,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_down
            )
        }
        transaction.replace(containerId, fragment)
        if (backStackTag != null) {
            transaction.addToBackStack(backStackTag)
        }
        transaction.commit()
        return true
    }

    private enum class Motion {
        FADE,
        SLIDE,
        RISE
    }

    companion object {
        const val TAG_GAME = "route_game"
        const val TAG_RESULT = "route_result"
    }
}
