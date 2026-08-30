package io.github.lucf15.restorecredentials.di

import android.content.Context
import androidx.startup.Initializer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

class KoinInitializer : Initializer<Koin> {
    override fun create(context: Context): Koin {
        GlobalContext.getOrNull()?.let { return it }
        return startKoin {
            androidLogger()
            androidContext(context)
            modules(appModule)
        }.koin
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
