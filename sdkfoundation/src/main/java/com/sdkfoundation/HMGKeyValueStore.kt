package com.sdkfoundation

import kotlinx.serialization.KSerializer

/**
 * Typed failures returned by [HMGKeyValueStore] operations.
 */
sealed class HMGKeyValueStoreError(message: String) : Exception(message) {
    /** No value exists for the requested key. */
    data object NotFound : HMGKeyValueStoreError("notFound")
    /** Stored bytes could not be decoded to the requested type. */
    data object DecodingFailed : HMGKeyValueStoreError("decodingFailed")
    /** The provided value could not be encoded for storage. */
    data object EncodingFailed : HMGKeyValueStoreError("encodingFailed")
}

/**
 * A lightweight, type-safe interface for key-value persistence.
 *
 * Implement `HMGKeyValueStore` to provide a storage backend (for example,
 * `SharedPreferences`, a file-backed store, or an in-memory map) that can
 * read and write serializable values by string keys.
 *
 * Typical usage:
 * - Use [set] to persist any serializable value.
 * - Use [getValue] to retrieve and decode a previously stored value.
 * - Use [containsValue] to check for the existence of a value without decoding it.
 * - Use [removeValue] to delete a value.
 *
 * Thread-safety and lifetime semantics are determined by the implementing class.
 *
 * Errors:
 * - [getValue] and [set] may throw [HMGKeyValueStoreError] to surface
 *   encoding/decoding failures, I/O failures, or other storage errors.
 *
 * Keys:
 * - Keys are case-sensitive strings and their namespace is defined by the implementor.
 *   Avoid collisions by using well-scoped, unique key names.
 */
interface HMGKeyValueStore {

    /**
     * Returns a decoded value for the provided key.
     *
     * @param key The lookup key.
     * @param serializer The serializer for type [T].
     * @return The decoded value as [T].
     * @throws HMGKeyValueStoreError.NotFound when no value exists.
     * @throws HMGKeyValueStoreError.DecodingFailed when decoding fails.
     */
    @Throws(HMGKeyValueStoreError::class)
    fun <T> getValue(forKey: String, serializer: KSerializer<T>): T

    /**
     * Persists a value under the provided key.
     *
     * @param value The value to store.
     * @param key The lookup key.
     * @param serializer The serializer for type [T].
     * @throws HMGKeyValueStoreError.EncodingFailed when encoding fails.
     */
    @Throws(HMGKeyValueStoreError::class)
    fun <T> set(value: T, forKey: String, serializer: KSerializer<T>)

    /**
     * Removes a stored value for the provided key.
     *
     * If no value exists, implementations may treat this as a no-op.
     *
     * @param key The key to remove.
     */
    fun removeValue(forKey: String)

    /**
     * Returns whether a value currently exists for the provided key.
     *
     * @param key The lookup key.
     * @return `true` if a value exists, otherwise `false`.
     */
    fun containsValue(forKey: String): Boolean
}
