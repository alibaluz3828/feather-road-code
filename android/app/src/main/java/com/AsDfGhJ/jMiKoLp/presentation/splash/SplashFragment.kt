package com.AsDfGhJ.jMiKoLp.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.AsDfGhJ.jMiKoLp.MainActivity
import com.AsDfGhJ.jMiKoLp.R
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.di.ViewModelFactory
import com.AsDfGhJ.jMiKoLp.databinding.FragmentSplashBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null

    private val viewModel: SplashViewModel by viewModels {
        ViewModelFactory(ServiceLocator.container(requireContext()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = FragmentSplashBinding.bind(view)
        binding = bound
        val reduced = isReducedMotion()
        val helper = SplashAnimator(resources.displayMetrics.density)
        animator = helper
        helper.playEntrance(
            plate = bound.splashPlate,
            title = bound.splashTitle,
            subtitle = bound.splashSubtitle,
            band = bound.splashBand,
            reduced = reduced
        )
        helper.playLoops(bound.splashPlate, bound.splashBand, reduced)
        helper.playGlow(bound.splashHazard, reduced)
        bound.splashProgress.isIndeterminate = true
        bound.splashLoading.text = getString(R.string.splash_loading)
        viewModel.start()
        observeStages()
        observeHandOff()
    }

    private fun observeStages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progressLabel.collectLatest { stage ->
                    val bound = binding ?: return@collectLatest
                    bound.splashLoading.alpha = STAGE_ALPHAS[stage % STAGE_ALPHAS.size]
                }
            }
        }
    }

    private fun observeHandOff() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handOffReady.collectLatest { ready ->
                    if (ready) {
                        handOff()
                    }
                }
            }
        }
    }

    private fun handOff() {
        if (!isAdded || view == null) {
            return
        }
        val host = activity as? MainActivity ?: return
        if (!host.navigator.showMenu()) {
            return
        }
        viewModel.consumeHandOff()
    }

    private fun isReducedMotion(): Boolean = try {
        ServiceLocator.container(requireContext()).settingsRepository.settings().reducedMotion
    } catch (e: Exception) {
        false
    }

    override fun onDestroyView() {
        animator?.cancelAll()
        animator = null
        binding?.splashProgress?.clearAnimation()
        binding = null
        super.onDestroyView()
    }

    private companion object {
        val STAGE_ALPHAS = floatArrayOf(0.55f, 0.75f, 0.9f, 1f)
    }
}
