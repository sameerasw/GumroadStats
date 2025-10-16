package com.sameerasw.gumroadstats.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.sameerasw.gumroadstats.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_cache")

class UserCache(private val context: Context) {
    companion object {
        private val CACHED_USER_KEY = stringPreferencesKey("cached_user")
        private val LAST_UPDATE_KEY = stringPreferencesKey("last_update_timestamp")
    }

    private val gson = Gson()

    val cachedUser: Flow<User?> = context.userDataStore.data.map { preferences ->
        val json = preferences[CACHED_USER_KEY] ?: return@map null
        try {
            gson.fromJson(json, User::class.java)
        } catch (_: Exception) {
            null
        }
    }

    val lastUpdateTime: Flow<Long?> = context.userDataStore.data.map { preferences ->
        preferences[LAST_UPDATE_KEY]?.toLongOrNull()
    }

    suspend fun saveUser(user: User) {
        context.userDataStore.edit { preferences ->
            val json = gson.toJson(user)
            preferences[CACHED_USER_KEY] = json
            preferences[LAST_UPDATE_KEY] = System.currentTimeMillis().toString()
        }
    }

    suspend fun clearCache() {
        context.userDataStore.edit { preferences ->
            preferences.remove(CACHED_USER_KEY)
            preferences.remove(LAST_UPDATE_KEY)
        }
    }
}
