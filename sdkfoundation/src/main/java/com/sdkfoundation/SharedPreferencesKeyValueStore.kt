package com.sdkfoundation

import android.content.SharedPreferences
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A [HMGKeyValueStore] implementation backed by Android [SharedPreferences].
 *
 * Values are JSON-encoded and stored as strings.
 */
class SharedPreferencesKeyValueStore(
    val sharedPreferences: SharedPreferences,
) : HMGKeyValueStore {

    @Throws(HMGKeyValueStoreError::class)
    override fun <T> getValue(forKey: String, serializer: KSerializer<T>): T {
        val jsonString = sharedPreferences.getString(forKey, null)
            ?: throw HMGKeyValueStoreError.NotFound
        return try {
            Json.decodeFromString(serializer, jsonString)
        } catch (_: Exception) {
            throw HMGKeyValueStoreError.DecodingFailed
        }
    }

    @Throws(HMGKeyValueStoreError::class)
    override fun <T> set(value: T, forKey: String, serializer: KSerializer<T>) {
        val jsonString = try {
            Json.encodeToString(serializer, value)
        } catch (_: Exception) {
            throw HMGKeyValueStoreError.EncodingFailed
        }
        sharedPreferences.edit().putString(forKey, jsonString).apply()
    }

    override fun removeValue(forKey: String) {
        sharedPreferences.edit().remove(forKey).apply()
    }

    override fun containsValue(forKey: String): Boolean {
        return sharedPreferences.contains(forKey)
    }
}
