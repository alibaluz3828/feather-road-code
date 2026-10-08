package com.AsDfGhJ.jMiKoLp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.AsDfGhJ.jMiKoLp.core.di.ServiceLocator
import com.AsDfGhJ.jMiKoLp.core.navigation.Navigator
import com.AsDfGhJ.jMiKoLp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    lateinit var navigator: Navigator
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val bound = ActivityMainBinding.inflate(layoutInflater)
        binding = bound
        setContentView(bound.root)
        navigator = Navigator(supportFragmentManager, R.id.fragment_container)
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
