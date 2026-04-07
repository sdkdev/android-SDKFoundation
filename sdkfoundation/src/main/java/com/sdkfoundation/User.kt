package com.sdkfoundation

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
) {
    fun getFullName(): String {
        return "$firstName $lastName"
    }
}
