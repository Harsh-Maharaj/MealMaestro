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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class DataBase(private val context: Context?) {
    constructor() : this(null)

    interface DataFetchCallback {
        fun onDataFetched()
    }

    private val auth = FirebaseAuth.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val dataBaseRef =
        FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()

    // -------------------- User Methods --------------------------

    fun addUserToDataBase(email: String, uid: String) {
        dataBaseRef.child("user").child(uid)
            .setValue(Users(username = null, name = null, email, uid, icon = null))
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
                dataBaseRef.child("user").child(uid).child("icon").setValue(uri.toString())
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload icon", Toast.LENGTH_SHORT).show()
        }
    }

    fun getUsersFromDataBase(
        userList: ArrayList<Users>,
        adapter: UsersAdapter,
        callback: DataFetchCallback
    ) {
        dataBaseRef.child("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (user in snapshot.children) {
                    val currentUser = user.getValue(Users::class.java)
                    if (currentUser!!.uid != auth.currentUser!!.uid) {
                        userList.add(currentUser)
                    }
                }
                adapter.notifyDataSetChanged()
                callback.onDataFetched()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // -------------------- Friends Methods --------------------------

    fun getFriendsList(friendList: ArrayList<Users>, adapter: FriendsAdapter) {
        val currentUserId = auth.currentUser?.uid ?: return
        dataBaseRef.child("user").child(currentUserId).child("friends")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    friendList.clear()
                    var friendsRetrieved = 0
                    val friendsIds =
                        snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: arrayListOf()

                    for (friendId in friendsIds) {
                        dataBaseRef.child("user").child(friendId).get()
                            .addOnSuccessListener { userSnapshot ->
                                val user = userSnapshot.getValue(Users::class.java)
                                if (user != null) {
                                    friendList.add(user)
                                }
                                friendsRetrieved++
                                if (friendsRetrieved == friendsIds.size) {
                                    adapter.notifyDataSetChanged()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Failed to retrieve friend: $friendId",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to retrieve friends list.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun addFriendToDataBase(currentUserId: String, newFriendId: String?) {
        if (newFriendId == null) return
        val currentUserRef = dataBaseRef.child("user").child(currentUserId).child("friends")

        currentUserRef.get().addOnSuccessListener { snapshot ->
            val friendsList = snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: arrayListOf()
            if (!friendsList.contains(newFriendId)) {
                friendsList.add(newFriendId)
                currentUserRef.setValue(friendsList)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend added successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error adding friend: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Friend already in the list!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            println("Error fetching friends list: ${e.message}")
        }
    }

    fun removeFriendFromDataBase(currentUserId: String, friendToRemove: String) {
        val currentUserRef = dataBaseRef.child("user").child(currentUserId).child("friends")

        currentUserRef.get().addOnSuccessListener { snapshot ->
            val friendsList = snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: arrayListOf()
            if (friendsList.contains(friendToRemove)) {
                friendsList.remove(friendToRemove)
                currentUserRef.setValue(friendsList)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend removed successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(context, "Error removing friend: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Friend not in your list!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            println("Error fetching friends list: ${e.message}")
        }
    }

    // -------------------- Chat Methods --------------------------

    fun addFriendChatMessage(senderRoom: String, receiverRoom: String, messageObject: Message) {
        dataBaseRef.child("friendChat").child(senderRoom).child("messages").push()
            .setValue(messageObject).addOnSuccessListener {
                dataBaseRef.child("friendChat").child(receiverRoom).child("messages").push()
                    .setValue(messageObject)
            }
    }

    fun getFriendMessage(
        senderRoom: String,
        messageList: ArrayList<Message>,
        adapter: MessageAdapter
    ) {
        dataBaseRef.child("friendChat").child(senderRoom).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnap in snapshot.children) {
                        val message = postSnap.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}

            })
    }

    // -------------------- File Metadata --------------------------

    fun saveFileMetadata(uid: String, fileUrl: String, mimeType: String?, folder: String) {
        val metadata = mapOf(
            "url" to fileUrl,
            "mimeType" to mimeType,
            "folder" to folder,
            "timestamp" to System.currentTimeMillis()
        )
        dataBaseRef.child("user").child(uid).child("files").push().setValue(metadata)
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
                    likes = mapOf(),
                    isPublic = true // Set post visibility to public
                )
                dataBaseRef.child("posts").child(postId).setValue(post)
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
        val postRef = dataBaseRef.child("posts").child(postId)
        postRef.child("likes").child(userId).setValue(true)
            .addOnSuccessListener {
                Toast.makeText(context, "Post liked!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to like post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun savePostToFavorites(postId: String, userId: String) {
        val favoritesRef = dataBaseRef.child("favorites").child(userId).child(postId)
        favoritesRef.setValue(true)
            .addOnSuccessListener {
                Toast.makeText(context, "Post saved to favorites!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getPostsFromDataBase(postList: ArrayList<Post>, adapter: PostAdapter) {
        dataBaseRef.child("posts").orderByChild("isPublic").equalTo(true)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    postList.clear()
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(Post::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to fetch posts.", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
