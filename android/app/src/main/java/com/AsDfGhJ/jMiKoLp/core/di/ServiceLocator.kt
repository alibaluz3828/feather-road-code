package com.AsDfGhJ.jMiKoLp.core.di

import android.content.Context

object ServiceLocator {

    private var container: AppContainer? = null

    fun init(context: Context) {
        if (container == null) {
            container = AppContainer(context.applicationContext)
        }
    }

    fun container(context: Context): AppContainer {
        val existing = container
        if (existing != null) {
            return existing
        }
        val created = AppContainer(context.applicationContext)
        container = created
        return created
    }
}
