package io.github.lucf15.restorecredentials.data.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.lucf15.restorecredentials.domain.model.AuthSession
import io.github.lucf15.restorecredentials.domain.repository.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val ACCESS_TOKEN = stringPreferencesKey("access_token")
private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
private val USERNAME = stringPreferencesKey("username")

class DataStoreSessionStore(private val dataStore: DataStore<Preferences>) : SessionStore {
    override val session: Flow<AuthSession?> =
        dataStore.data.map { prefs ->
            val accessToken = prefs[ACCESS_TOKEN] ?: return@map null
            val refreshToken = prefs[REFRESH_TOKEN] ?: return@map null
            val username = prefs[USERNAME] ?: return@map null
            AuthSession(accessToken, refreshToken, username)
        }

    override suspend fun save(session: AuthSession) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = session.accessToken
            prefs[REFRESH_TOKEN] = session.refreshToken
            prefs[USERNAME] = session.username
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
