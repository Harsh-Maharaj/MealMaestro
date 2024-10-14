package com.example.mealmaestro

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)  // Disable manifest lookup for Robolectric
@RunWith(AndroidJUnit4::class)
class AiResponseAdapterTest {

    private lateinit var adapter: AiResponseAdapter
    private lateinit var context: Context

    @Mock
    private lateinit var mockDb: FirebaseFirestore
    @Mock
    private lateinit var mockCollectionRef: CollectionReference
    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot
    @Mock
    private lateinit var mockDocumentSnapshot: QueryDocumentSnapshot

    private val mockResponses = mutableListOf(
        AiResponse("user1", "What is AI?", "AI is artificial intelligence."),
        AiResponse("user1", "How does AI work?", "It uses algorithms to learn.")
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        context = ApplicationProvider.getApplicationContext()
        adapter = AiResponseAdapter(mockResponses, context)

        // Mock Firestore initialization
        `when`(FirebaseFirestore.getInstance()).thenReturn(mockDb)
        `when`(mockDb.collection("ai_responses")).thenReturn(mockCollectionRef)
    }

    @Test
    fun testDeleteResponse_Success() {
        // Arrange: Setup a successful query and deletion
        `when`(mockCollectionRef.whereEqualTo("userId", "user1")).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.whereEqualTo("question", "What is AI?")).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.limit(1)).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        // Return a non-empty query snapshot with the mocked document
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.first()).thenReturn(mockDocumentSnapshot)
        `when`(mockDocumentSnapshot.id).thenReturn("documentId")

        // Mock deletion task success
        `when`(mockCollectionRef.document("documentId").delete()).thenReturn(Tasks.forResult(null))

        // Act: Trigger the deletion process
        adapter.showDeleteConfirmationDialog(mockResponses[0], 0)

        // Verify: Ensure the delete operation was called and the item was removed from the list
        verify(mockCollectionRef.document("documentId")).delete()
        assert(mockResponses.size == 1)  // One item should have been removed
    }

    @Test
    fun testDeleteResponse_NoMatchingDocument() {
        // Arrange: Simulate no matching documents found
        `when`(mockCollectionRef.whereEqualTo("userId", "user1")).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.whereEqualTo("question", "What is AI?")).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.limit(1)).thenReturn(mockCollectionRef)
        `when`(mockCollectionRef.get()).thenReturn(Tasks.forResult(mockQuerySnapshot))

        // Return an empty query snapshot
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        // Act: Attempt to delete a response
        adapter.showDeleteConfirmationDialog(mockResponses[0], 0)

        // Verify: Ensure no delete operation was performed and the list remains the same
        verify(mockCollectionRef.document(anyString()), never()).delete()
        assert(mockResponses.size == 2)  // List size should remain unchanged
    }
}
