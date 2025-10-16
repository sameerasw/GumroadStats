package com.sameerasw.gumroadstats.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sameerasw.gumroadstats.data.model.Payout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.payoutsDataStore: DataStore<Preferences> by preferencesDataStore(name = "payouts_cache")

class PayoutsCache(private val context: Context) {
    companion object {
        private val CACHED_PAYOUTS_KEY = stringPreferencesKey("cached_payouts")
        private val LAST_UPDATE_KEY = stringPreferencesKey("last_update_timestamp")
    }

    private val gson = Gson()

    val cachedPayouts: Flow<List<Payout>> = context.payoutsDataStore.data.map { preferences ->
        val json = preferences[CACHED_PAYOUTS_KEY] ?: return@map emptyList()
        try {
            val type = object : TypeToken<List<Payout>>() {}.type
            gson.fromJson<List<Payout>>(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    val lastUpdateTime: Flow<Long?> = context.payoutsDataStore.data.map { preferences ->
        preferences[LAST_UPDATE_KEY]?.toLongOrNull()
    }

    suspend fun savePayouts(payouts: List<Payout>) {
        context.payoutsDataStore.edit { preferences ->
            val json = gson.toJson(payouts)
            preferences[CACHED_PAYOUTS_KEY] = json
            preferences[LAST_UPDATE_KEY] = System.currentTimeMillis().toString()
        }
    }

    suspend fun clearCache() {
        context.payoutsDataStore.edit { preferences ->
            preferences.remove(CACHED_PAYOUTS_KEY)
            preferences.remove(LAST_UPDATE_KEY)
        }
    }
}

