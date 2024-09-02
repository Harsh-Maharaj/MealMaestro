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

    private val auth = FirebaseAuth.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    fun addUserToDataBase(email: String, uid: String) {
        val user = Users(username = null, name = null, email, uid, icon = null)
        firestore.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(context, "User added successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
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
                firestore.collection("users").document(uid).update("icon", uri.toString())
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload icon", Toast.LENGTH_SHORT).show()
        }
    }

    fun getUsersFromDataBase(userList: ArrayList<Users>, adapter: UsersAdapter) {
        firestore.collection("users").addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(context, "Error fetching users: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            if (snapshots != null) {
                userList.clear()
                for (document in snapshots.documents) {
                    val currentUser = document.toObject(Users::class.java)
                    if (currentUser != null && currentUser.uid != auth.currentUser?.uid) {
                        userList.add(currentUser)
                    }
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun getFriendsList(friendList: ArrayList<Users>, adapter: FriendsAdapter) {
        val currentUser = auth.currentUser?.uid ?: return

        firestore.collection("users").document(currentUser)
            .get().addOnSuccessListener { snapshot ->
                val friendsIds = snapshot.toObject(Users::class.java)?.friends ?: listOf()

                friendList.clear()
                for (friendId in friendsIds) {
                    firestore.collection("users").document(friendId).get()
                        .addOnSuccessListener { userSnapshot ->
                            val user = userSnapshot.toObject(Users::class.java)
                            if (user != null) {
                                friendList.add(user)
                            }
                            adapter.notifyDataSetChanged()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to fetch friends list", Toast.LENGTH_SHORT).show()
            }
    }

    fun addFriendToDataBase(currentUserId: String, newFriendId: String?) {
        if (newFriendId == null) return

        firestore.collection("users").document(currentUserId)
            .get().addOnSuccessListener { snapshot ->
                val friendsList = snapshot.toObject(Users::class.java)?.friends ?: mutableListOf()

                if (!friendsList.contains(newFriendId)) {
                    friendsList.add(newFriendId)
                    firestore.collection("users").document(currentUserId)
                        .update("friends", friendsList)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error adding friend: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Friend already in the list!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching friends list", Toast.LENGTH_SHORT).show()
            }
    }

    fun removeFriendFromDataBase(currentUserId: String, friendToRemove: String) {
        firestore.collection("users").document(currentUserId)
            .get().addOnSuccessListener { snapshot ->
                val friendsList = snapshot.toObject(Users::class.java)?.friends ?: mutableListOf()

                if (friendsList.contains(friendToRemove)) {
                    friendsList.remove(friendToRemove)
                    firestore.collection("users").document(currentUserId)
                        .update("friends", friendsList)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Friend removed successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error removing friend: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Friend not in your list!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching friends list", Toast.LENGTH_SHORT).show()
            }
    }

    fun addFriendChatMessage(senderId: String, receiverId: String, messageObject: Message) {
        val chatRoomId = if (senderId < receiverId) {
            "$senderId$receiverId"
        } else {
            "$receiverId$senderId"
        }

        firestore.collection("friendChat").document(chatRoomId)
            .collection("messages").add(messageObject)
            .addOnSuccessListener {
                Toast.makeText(context, "Message sent!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to send message.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getFriendMessage(
        senderId: String,
        receiverId: String,
        messageList: ArrayList<Message>,
        adapter: MessageAdapter
    ) {
        val chatRoomId = if (senderId < receiverId) {
            "$senderId$receiverId"
        } else {
            "$receiverId$senderId"
        }

        firestore.collection("friendChat").document(chatRoomId)
            .collection("messages").addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Error fetching messages: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    messageList.clear()
                    for (document in snapshots.documents) {
                        val message = document.toObject(Message::class.java)
                        if (message != null) {
                            messageList.add(message)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    fun saveFileMetadata(uid: String, fileUrl: String, mimeType: String?, folder: String) {
        val metadata = mapOf(
            "url" to fileUrl,
            "mimeType" to mimeType,
            "folder" to folder,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("users").document(uid)
            .collection("files").add(metadata)
    }

    // ---------------------- New Methods for Posts ----------------------

    fun addPostToFirestore(uid: String, imageUri: Uri, caption: String) {
        val postId = UUID.randomUUID().toString()
        val postImageRef = storageRef.child("postImages/$postId.jpg")

        postImageRef.putFile(imageUri).addOnSuccessListener {
            postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val post = Post(
                    id = postId,
                    user_id = uid,
                    image_url = downloadUri.toString(),
                    caption = caption,
                    likes = mapOf()
                )

                firestore.collection("posts")
                    .document(postId)
                    .set(post)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Post added successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to add post: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    fun likePost(postId: String, userId: String) {
        val postRef = firestore.collection("posts").document(postId)
        postRef.update("likes.$userId", true)
            .addOnSuccessListener {
                Toast.makeText(context, "Post liked!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to like post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun savePostToFavorites(postId: String, userId: String) {
        val favoritesRef = firestore.collection("favorites").document(userId).collection("posts")
        favoritesRef.document(postId).set(mapOf("saved" to true))
            .addOnSuccessListener {
                Toast.makeText(context, "Post saved to favorites!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getPostsFromFirestore(postList: ArrayList<Post>, adapter: PostAdapter) {
        firestore.collection("posts")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(context, "Error fetching posts: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots.documents) {
                        val post = document.toObject(Post::class.java)
                        post?.let { postList.add(it) }  // Ensure the post is non-null before adding
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}
