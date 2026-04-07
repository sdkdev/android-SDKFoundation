package com.sdkfoundation

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [HMGKeyValueStore] using an in-memory backing store.
 *
 * These tests exercise the same scenarios as the iOS UserDefaultsKeyValueStoreTests
 * without requiring an Android context.
 */
class HMGPersistenceTest {

    private lateinit var sut: InMemoryKeyValueStore

    @Before
    fun setUp() {
        sut = InMemoryKeyValueStore()
    }

    @Test
    fun `set validValue storesDataForKey`() {
        val value = TestProfile(id = "abc-123", name = "Sascha")

        sut.set(value, "profile", TestProfile.serializer())

        assertTrue(sut.containsValue("profile"))
    }

    @Test
    fun `get existingValue returnsDecodedValue`() {
        val expected = TestProfile(id = "abc-123", name = "Sascha")

        sut.set(expected, "profile", TestProfile.serializer())
        val loaded: TestProfile = sut.getValue("profile", TestProfile.serializer())

        assertEquals(expected, loaded)
    }

    @Test(expected = HMGKeyValueStoreError.NotFound::class)
    fun `get missingValue throwsNotFound`() {
        sut.getValue<TestProfile>("missing_profile", TestProfile.serializer())
    }

    @Test
    fun `containsValue existingKey returnsTrue`() {
        val value = TestProfile(id = "abc-123", name = "Sascha")

        sut.set(value, "profile", TestProfile.serializer())

        assertTrue(sut.containsValue("profile"))
    }

    @Test
    fun `containsValue missingKey returnsFalse`() {
        assertFalse(sut.containsValue("profile"))
    }

    @Test
    fun `removeValue existingKey removesStoredValue`() {
        val value = TestProfile(id = "abc-123", name = "Sascha")

        sut.set(value, "profile", TestProfile.serializer())
        assertTrue(sut.containsValue("profile"))

        sut.removeValue("profile")

        assertFalse(sut.containsValue("profile"))
    }

    @Test
    fun `removeValue missingKey isNoOperation`() {
        sut.removeValue("missing_profile")

        assertFalse(sut.containsValue("missing_profile"))
    }

    @Test(expected = HMGKeyValueStoreError.DecodingFailed::class)
    fun `get decodeFailure throwsDecodingFailed`() {
        sut.putRaw("profile", "not-valid-json")

        sut.getValue<TestProfile>("profile", TestProfile.serializer())
    }

    // -- Helpers --

    @Serializable
    private data class TestProfile(
        val id: String,
        val name: String,
    )

    /**
     * In-memory [HMGKeyValueStore] that stores JSON strings in a [HashMap],
     * mirroring how [SharedPreferencesKeyValueStore] stores JSON in SharedPreferences.
     */
    private class InMemoryKeyValueStore : HMGKeyValueStore {
        private val store = HashMap<String, String>()

        @Throws(HMGKeyValueStoreError::class)
        override fun <T> getValue(forKey: String, serializer: KSerializer<T>): T {
            val jsonString = store[forKey] ?: throw HMGKeyValueStoreError.NotFound
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
            store[forKey] = jsonString
        }

        override fun removeValue(forKey: String) {
            store.remove(forKey)
        }

        override fun containsValue(forKey: String): Boolean {
            return store.containsKey(forKey)
        }

        /** Insert raw string data to simulate corrupt/invalid stored values. */
        fun putRaw(key: String, value: String) {
            store[key] = value
        }
    }
}
