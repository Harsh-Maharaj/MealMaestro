package com.example.mealmaestro.Helper

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.mealmaestro.Chats.Message
import com.example.mealmaestro.Chats.MessageAdapter
import com.example.mealmaestro.PostAdapter
import com.example.mealmaestro.users.FriendsAdapter
import com.example.mealmaestro.users.Users
import com.example.mealmaestro.users.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
    val dataBaseRef =
        FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()

    // -------------------- User Methods --------------------------

    fun addFCMToken(token: String, currentUserId: String) {
        dataBaseRef.child("user")
            .child(currentUserId)
            .child("fcmToken")
            .setValue(token)
            .addOnSuccessListener {
                Log.i("FCM Token", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FCM Token Error", "Fail to save token", e)
            }
    }

    fun addUserToDataBase(email: String, uid: String, username: String) {
        dataBaseRef.child("user").child(uid)
            .setValue(Users(email, uid, username = username, icon = null))
    }

    fun saveUsernameToDatabaseFromGoogle(user: FirebaseUser) {
        val userId = user.uid

        // Check if the user already exists in the database
        dataBaseRef.child("user").child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (!dataSnapshot.exists()) {
                // default username
                val emailPrefix = user.email?.substringBefore("@") ?: "user"
                val username = emailPrefix + System.currentTimeMillis().toString()
                    .takeLast(4) // Create a unique username

                val userId = user.uid

                // Create a user map
                val userMap = mapOf(
                    "username" to username,
                    "email" to user.email,
                    "uid" to userId
                )
                dataBaseRef.child("user").child(userId).setValue(userMap)
            }
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
                        try {
                            val userData = user.value as? Map<String, Any>
                            if (userData != null) {
                                val currentUser = Users(
                                    uid = userData["uid"] as? String ?: "",
                                    username = userData["username"] as? String ?: "",
                                )
                                if (currentUser.uid != auth.currentUser!!.uid) {
                                    userList.add(currentUser)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DatabaseError", "Error parsing user data", e)
                            // Handle the error as appropriate for your app
                        }
                    }
                    adapter.notifyDataSetChanged()
                    callback.onDataFetched()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseError", "Error fetching data", error.toException())
                    // Handle the error as appropriate for your app
                }
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
                            snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {})
                                ?: arrayListOf()

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
                        Toast.makeText(
                            context,
                            "Failed to retrieve friends list.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                })
        }

        fun addFriendToDataBase(currentUserId: String, newFriendId: String?) {
            if (newFriendId == null) return
            val currentUserRef = dataBaseRef.child("user").child(currentUserId).child("friends")

            currentUserRef.get().addOnSuccessListener { snapshot ->
                val friendsList =
                    snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {})
                        ?: arrayListOf()
                if (!friendsList.contains(newFriendId)) {
                    friendsList.add(newFriendId)
                    currentUserRef.setValue(friendsList)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Friend added successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error adding friend: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "Friend already in the list!", Toast.LENGTH_SHORT)
                        .show()
                }
            }.addOnFailureListener { e ->
                println("Error fetching friends list: ${e.message}")
            }
        }

        fun removeFriendFromDataBase(currentUserId: String, friendToRemove: String) {
            val currentUserRef = dataBaseRef.child("user").child(currentUserId).child("friends")

            currentUserRef.get().addOnSuccessListener { snapshot ->
                val friendsList =
                    snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {})
                        ?: arrayListOf()
                if (friendsList.contains(friendToRemove)) {
                    friendsList.remove(friendToRemove)
                    currentUserRef.setValue(friendsList)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Friend removed successfully!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "Error removing friend: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "Friend not in your list!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                println("Error fetching friends list: ${e.message}")
            }
        }

        // -------------------- Chat Methods --------------------------

        // ================ NOTIFICATIONS ==============================================================
        fun addFriendChatMessage(
            senderRoom: String,
            receiverRoom: String,
            messageObject: Message,
            context: Context
        ) {
            val senderUid = messageObject.sender
            val senderRef = dataBaseRef.child("user").child(senderUid)

            senderRef.get().addOnSuccessListener { senderSnapshot ->
                if (senderSnapshot.exists()) {
                    val senderName = senderSnapshot.child("username").getValue(String::class.java)

                    // Store the message in the sender's chat room
                    dataBaseRef.child("friendChat").child(senderRoom).child("messages").push()
                        .setValue(messageObject).addOnSuccessListener {
                            // After success, store the message in the receiver's chat room
                            dataBaseRef.child("friendChat").child(receiverRoom).child("messages")
                                .push()
                                .setValue(messageObject).addOnSuccessListener {
                                    // Now call the FCMNotificationService to send the notification
                                    val friendUid = messageObject.receiverUid!!
                                    val friendRef = dataBaseRef.child("user").child(friendUid)

                                    // Fetch friend's FCM token from the database
                                    friendRef.get().addOnSuccessListener { friendSnapshot ->
                                        val friendFcmToken = friendSnapshot.child("fcmToken")
                                            .getValue(String::class.java)

                                        if (friendFcmToken != null) {
                                            // Use FCMNotificationService to send the notification
                                            val notificationService = FCMNotificationService()
                                            notificationService.sendFCMNotification(
                                                friendFcmToken,
                                                senderName!!,
                                                friendUid,
                                                messageObject.message,
                                                context
                                            )
                                        }
                                    }
                                }
                        }
                }
            }
        }

        //=============================== END NOTIFICATION =================================================
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
                        likes = mutableMapOf(), // Initialize with a mutable map
                        isPublic = true // Set post visibility to public
                    )
                    dataBaseRef.child("posts").child(postId).setValue(post)
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
            val postRef = dataBaseRef.child("posts").child(postId)

            postRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val post = snapshot.getValue(Post::class.java) ?: return

                    if (post.likes.containsKey(userId)) {
                        // User has already liked the post, so we remove the like
                        postRef.child("likes").child(userId).removeValue()
                            .addOnSuccessListener {
                                callback(false) // Not liked
                                Toast.makeText(context, "Post unliked!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to unlike post: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        // User has not liked the post yet, so we add the like
                        postRef.child("likes").child(userId).setValue(true)
                            .addOnSuccessListener {
                                callback(true) // Liked
                                Toast.makeText(context, "Post liked!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Failed to like post: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to check like status.", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }

        fun savePostToFavorites(postId: String, userId: String) {
            val favoritesRef = dataBaseRef.child("favorites").child(userId).child(postId)
            favoritesRef.setValue(true)
                .addOnSuccessListener {
                    Toast.makeText(context, "Post saved to favorites!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to save post: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
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

        // -------------------- Update Post Method --------------------------

        fun updatePost(postId: String, newCaption: String?, newImageUri: Uri?) {
            val postRef = dataBaseRef.child("posts").child(postId)

            if (newCaption != null) {
                postRef.child("caption").setValue(newCaption)
            }

            if (newImageUri != null) {
                val postImageRef = storageRef.child("postImages/$postId.jpg")
                postImageRef.putFile(newImageUri).addOnSuccessListener {
                    postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        postRef.child("image_url").setValue(downloadUri.toString())
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT)
                            .show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }