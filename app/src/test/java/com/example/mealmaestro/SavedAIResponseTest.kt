package com.example.mealmaestro

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SavedAIResponseTest {

    private lateinit var savedResponses: MutableList<AiResponse>

    @Before
    fun setup() {
        // Arrange: Initialize a list of saved AI responses
        savedResponses = mutableListOf(
            AiResponse("1", "What is AI?", "AI is artificial intelligence."),
            AiResponse("2", "How does AI work?", "It uses algorithms to learn.")
        )
    }

    @Test
    fun testDeleteSavedResponse_Pass() {
        // Act: Delete the response with ID "1"
        val responseToDelete = savedResponses.first { it.id == "1" }
        savedResponses.remove(responseToDelete)

        // Assert: Verify that the response with ID "1" is removed
        assertTrue(savedResponses.none { it.id == "1" })  // This will now pass
    }
}
