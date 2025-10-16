package com.sameerasw.gumroadstats.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val UPDATE_INTERVAL_KEY = longPreferencesKey("update_interval_minutes")
    }

    val accessToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY] ?: ""
    }

    val updateInterval: Flow<UpdateInterval> = context.dataStore.data.map { preferences ->
        val minutes = preferences[UPDATE_INTERVAL_KEY]
        UpdateInterval.fromMinutes(minutes)
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveUpdateInterval(interval: UpdateInterval) {
        context.dataStore.edit { preferences ->
            if (interval.minutes != null) {
                preferences[UPDATE_INTERVAL_KEY] = interval.minutes
            } else {
                preferences.remove(UPDATE_INTERVAL_KEY)
            }
        }
    }

    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
        }
    }
}

