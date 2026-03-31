package com.sdkfoundation

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun `serialization round trip`() {
        val user = User(firstName = "John", lastName = "Doe")
        val json = Json.encodeToString(user)
        val decoded = Json.decodeFromString<User>(json)
        assertEquals(user, decoded)
    }
}
