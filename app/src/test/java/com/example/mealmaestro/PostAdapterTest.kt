package com.example.mealmaestro

import com.example.mealmaestro.Helper.Post
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class PostAdapterTest {

    private lateinit var postAdapter: PostAdapter
    private lateinit var postList: MutableList<Post>

    @Before
    fun setup() {
        // Arrange: Initialize the PostAdapter with mock data
        val context = mock(android.content.Context::class.java)  // Mock the context
        postList = mutableListOf(
            Post(
                postId = "1",
                user_id = "user1",
                caption = "Ingredients:\nChicken\nGarlic\nOnion"
            ),
            Post(
                postId = "2",
                user_id = "user2",
                caption = "Ingredients:\nTomato\nBasil\nGarlic"
            )
        )
        postAdapter = PostAdapter(context, postList) { }
    }

    @Test
    fun testGenerateShoppingList_Pass() {
        val generatedIngredients = postAdapter.extractIngredients(postList[0].caption)
        val expectedIngredients = listOf("Chicken", "Garlic", "Onion")

        assertEquals(expectedIngredients, generatedIngredients)  // Should pass now
    }

}
