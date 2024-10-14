package com.example.mealmaestro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostManagerTest {

    private lateinit var postManager: PostManager
    private lateinit var userPost: Post
    private lateinit var otherUserPost: Post

    @Before
    fun setup() {
        postManager = PostManager()

        // Initialize posts with sample data
        userPost = Post(postId = "1", userId = "user123", caption = "My first post")
        otherUserPost = Post(postId = "2", userId = "otherUser456", caption = "Another user's post")

        // Add posts to the post manager
        postManager.addPost(userPost)
        postManager.addPost(otherUserPost)
    }

    @Test
    fun testDeleteOwnPost_Success() {
        // Act: Try to delete the user's own post
        val result = postManager.deletePost("1", "user123")

        // Assert: Deletion should be successful
        assertTrue("User should be able to delete their own post", result)
    }

    @Test
    fun testDeleteOtherUserPost_Failure() {
        // Act: Try to delete a post from another user
        val result = postManager.deletePost("2", "user123")

        // Assert: Deletion should fail
        assertFalse("User should not be able to delete another user's post", result)
    }
}

// Sample Post and PostManager classes for the test to pass
data class Post(
    val postId: String,
    val userId: String,
    val caption: String
)

class PostManager {
    private val posts = mutableListOf<Post>()

    fun addPost(post: Post) {
        posts.add(post)
    }

    fun deletePost(postId: String, userId: String): Boolean {
        val post = posts.find { it.postId == postId }
        return if (post != null && post.userId == userId) {
            posts.remove(post)
            true
        } else {
            false
        }
    }
}
