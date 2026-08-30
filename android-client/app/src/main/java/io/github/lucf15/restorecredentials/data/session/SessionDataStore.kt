package io.github.lucf15.restorecredentials.data.session

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.sessionDataStore by preferencesDataStore(name = "session")
