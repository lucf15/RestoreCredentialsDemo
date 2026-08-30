package io.github.lucf15.restorecredentials.di

import io.github.lucf15.restorecredentials.data.network.AuthApiClient
import io.github.lucf15.restorecredentials.data.network.RestoreCredentialApiClient
import io.github.lucf15.restorecredentials.data.network.createHttpClient
import io.github.lucf15.restorecredentials.data.session.DataStoreSessionStore
import io.github.lucf15.restorecredentials.data.session.sessionDataStore
import io.github.lucf15.restorecredentials.domain.repository.AuthRepository
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialApi
import io.github.lucf15.restorecredentials.domain.repository.RestoreCredentialGateway
import io.github.lucf15.restorecredentials.domain.repository.SessionStore
import io.github.lucf15.restorecredentials.domain.usecase.RegisterRestoreCredentialUseCase
import io.github.lucf15.restorecredentials.domain.usecase.SignInUseCase
import io.github.lucf15.restorecredentials.domain.usecase.SignOutUseCase
import io.github.lucf15.restorecredentials.domain.usecase.TryRestoreSignInUseCase
import io.github.lucf15.restorecredentials.platform.credentials.AndroidRestoreCredentialGateway
import io.github.lucf15.restorecredentials.ui.screens.home.HomeViewModel
import io.github.lucf15.restorecredentials.ui.screens.signin.SignInViewModel
import io.github.lucf15.restorecredentials.ui.screens.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    single { createHttpClient() }
    single { androidContext().sessionDataStore }

    singleOf(::AuthApiClient) { bind<AuthRepository>() }
    singleOf(::RestoreCredentialApiClient) { bind<RestoreCredentialApi>() }
    singleOf(::DataStoreSessionStore) { bind<SessionStore>() }
    singleOf(::AndroidRestoreCredentialGateway) { bind<RestoreCredentialGateway>() }

    factoryOf(::RegisterRestoreCredentialUseCase)
    factoryOf(::SignInUseCase)
    factoryOf(::TryRestoreSignInUseCase)
    factoryOf(::SignOutUseCase)

    viewModelOf(::SplashViewModel)
    viewModelOf(::SignInViewModel)
    viewModelOf(::HomeViewModel)
}
