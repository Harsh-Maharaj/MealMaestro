package com.example.mealmaestro.Helper

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.mealmaestro.Chats.Message
import com.example.mealmaestro.Chats.MessageAdapter
import com.example.mealmaestro.PostAdapter
import com.example.mealmaestro.users.FriendsAdapter
import com.example.mealmaestro.users.Users
import com.example.mealmaestro.users.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class DataBase(private val context: Context?) {
    constructor() : this(null)

    interface DataFetchCallback {
        fun onDataFetched()
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    // -------------------- User Methods --------------------------

    fun addUserToDataBase(email: String, uid: String) {
        val user = Users(username = null, name = null, email, uid, icon = null)
        firestore.collection("user").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(context, "User added to Firestore", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to add user: ${it.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun addUserIcon(uid: String, iconUri: Uri) {
        val contentResolver = context?.contentResolver
        val mimeType = contentResolver?.getType(iconUri)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "jpg"
        }

        val iconRef = storageRef.child("userImages/$uid/${UUID.randomUUID()}.$extension")
        iconRef.putFile(iconUri).addOnSuccessListener {
            iconRef.downloadUrl.addOnSuccessListener { uri ->
                firestore.collection("user").document(uid).update("icon", uri.toString())
                    .addOnSuccessListener {
                        Toast.makeText(context, "User icon updated", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to update user icon", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    fun getUsersFromDataBase(
        userList: ArrayList<Users>,
        adapter: UsersAdapter,
        callback: DataFetchCallback
    ) {
        firestore.collection("user").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                Toast.makeText(context, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            userList.clear()
            for (document in snapshot.documents) {
                val user = document.toObject(Users::class.java)
                if (user?.uid != auth.currentUser?.uid) {
                    userList.add(user!!)
                }
            }
            adapter.notifyDataSetChanged()
            callback.onDataFetched()
        }
    }

    // -------------------- Friends Methods --------------------------

    fun getFriendsList(friendList: ArrayList<Users>, adapter: FriendsAdapter) {
        val currentUserId = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("user").document(currentUserId)

        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(Users::class.java)
            val friendsIds = user?.friends ?: arrayListOf()

            friendList.clear()
            friendsIds.forEach { friendId ->
                firestore.collection("user").document(friendId).get()
                    .addOnSuccessListener { friendSnapshot ->
                        val friend = friendSnapshot.toObject(Users::class.java)
                        if (friend != null) {
                            friendList.add(friend)
                        }
                        adapter.notifyDataSetChanged()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to retrieve friends list", Toast.LENGTH_SHORT).show()
        }
    }

    fun addFriendToDataBase(currentUserId: String, newFriendId: String?) {
        if (newFriendId == null) return

        val currentUserRef = firestore.collection("user").document(currentUserId)

        currentUserRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(Users::class.java)
            val friendsList = user?.friends ?: arrayListOf()

            if (!friendsList.contains(newFriendId)) {
                friendsList.add(newFriendId)
                currentUserRef.update("friends", friendsList)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Error adding friend: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "Friend already in the list!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                context,
                "Error fetching friends list: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun removeFriendFromDataBase(currentUserId: String, friendToRemove: String) {
        val currentUserRef = firestore.collection("user").document(currentUserId)

        currentUserRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.toObject(Users::class.java)
            val friendsList = user?.friends ?: arrayListOf()

            if (friendsList.contains(friendToRemove)) {
                friendsList.remove(friendToRemove)
                currentUserRef.update("friends", friendsList)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend removed successfully!", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Error removing friend: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(context, "Friend not in your list!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(
                context,
                "Error fetching friends list: ${it.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // -------------------- Chat Methods --------------------------

    fun addFriendChatMessage(senderRoom: String, receiverRoom: String, messageObject: Message) {
        firestore.collection("friendChat").document(senderRoom).collection("messages")
            .add(messageObject)
            .addOnSuccessListener {
                firestore.collection("friendChat").document(receiverRoom).collection("messages")
                    .add(messageObject)
            }
    }

    fun getFriendMessage(
        senderRoom: String,
        messageList: ArrayList<Message>,
        adapter: MessageAdapter
    ) {
        firestore.collection("friendChat").document(senderRoom).collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                messageList.clear()
                for (document in snapshot.documents) {
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    // -------------------- Posts Methods --------------------------

    fun addPostToDataBase(uid: String, imageUri: Uri, caption: String) {
        val postId = UUID.randomUUID().toString()
        val postImageRef = storageRef.child("postImages/$postId.jpg")

        postImageRef.putFile(imageUri).addOnSuccessListener {
            postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val post = Post(
                    postId = postId,
                    user_id = uid,
                    image_url = downloadUri.toString(),
                    caption = caption,
                    likes = mapOf(), // Initialize with an empty map
                    isPublic = true // Set post visibility to public
                )
                firestore.collection("posts").document(postId).set(post)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Post added successfully!", Toast.LENGTH_SHORT)
                            .show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Failed to add post: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    fun likePost(postId: String, userId: String, callback: (Boolean) -> Unit) {
        val postRef = firestore.collection("posts").document(postId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likes = snapshot.get("likes") as Map<String, Boolean>? ?: mapOf()

            val newLikeStatus = likes[userId] != true
            transaction.update(postRef, "likes.$userId", newLikeStatus)
            newLikeStatus
        }.addOnSuccessListener { result ->
            callback(result)
        }.addOnFailureListener {
            callback(false)
        }
    }

    fun getPostsFromDataBase(postList: ArrayList<Post>, adapter: PostAdapter) {
        firestore.collection("posts").whereEqualTo("isPublic", true)
            .get()
            .addOnSuccessListener { snapshot ->
                postList.clear()
                for (document in snapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        postList.add(post)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch posts", Toast.LENGTH_SHORT).show()
            }
    }

    fun savePostToFavorites(postId: String, userId: String) {
        val favoritesRef =
            firestore.collection("favorites").document(userId).collection("posts").document(postId)
        favoritesRef.set(mapOf("saved" to true))
            .addOnSuccessListener {
                Toast.makeText(context, "Post saved to favorites!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save post", Toast.LENGTH_SHORT).show()
            }
    }

    fun updatePost(postId: String, newCaption: String?, newImageUri: Uri?) {
        val postRef = firestore.collection("posts").document(postId)

        if (newCaption != null) {
            postRef.update("caption", newCaption)
        }

        if (newImageUri != null) {
            val postImageRef = storageRef.child("postImages/$postId.jpg")
            postImageRef.putFile(newImageUri).addOnSuccessListener {
                postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    postRef.update("image_url", downloadUri.toString())
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveFileMetadata(uid: String, fileUrl: String, mimeType: String?, folder: String) {
        val metadata = mapOf(
            "url" to fileUrl,  // The actual URL of the uploaded file
            "mimeType" to mimeType,
            "folder" to folder,
            "timestamp" to System.currentTimeMillis()  // Store the current timestamp
        )

        val fileMetadataRef = FirebaseFirestore.getInstance()
            .collection("user_files")
            .document(uid)
            .collection("files")
            .document()  // Auto-generate a document ID

        fileMetadataRef.set(metadata)
            .addOnSuccessListener {
                Toast.makeText(context, "File metadata saved successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed to save file metadata: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}



