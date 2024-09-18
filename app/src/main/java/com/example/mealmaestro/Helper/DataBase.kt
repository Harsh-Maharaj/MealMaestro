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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.io.IOException

class DataBase(private val context: Context?) {
    // Secondary constructor to allow instantiation without passing context
    constructor() : this(null)

    // Interface definition for a callback to be triggered when data fetching is complete
    interface DataFetchCallback {
        fun onDataFetched() // This method will be implemented by the caller to handle data fetching events
    }

    // Firebase Authentication instance, used to manage user authentication (e.g., login, sign up)
    private val auth = FirebaseAuth.getInstance()

    // Firebase Storage reference, used for uploading and accessing files (e.g., user profile images)
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    // Firebase Realtime Database reference, pointing to the root of the specified database URL
    // This allows for reading and writing data to the Firebase Realtime Database
    val dataBaseRef =
        FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()


    // -------------------- User Methods --------------------------

    // Adds an FCM (Firebase Cloud Messaging) token for the current user to the database
    fun addFCMToken(token: String, currentUserId: String) {
        // Access the "user" node in the database and set the FCM token for the current user
        dataBaseRef.child("user")
            .child(currentUserId) // Specify the current user's ID to store the token under their node
            .child("fcmToken") // Store the FCM token under the "fcmToken" child
            .setValue(token) // Save the token value in the database
            .addOnSuccessListener {
                // If the token is successfully saved, log the success
                Log.i("FCM Token", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                // If there's an error saving the token, log the error message
                Log.e("FCM Token Error", "Fail to save token", e)
            }
    }

    // Adds a new user to the Firebase Realtime Database
    fun addUserToDataBase(email: String, uid: String, username: String) {
        // Create a new user in the "user" node using the user's unique ID (uid)
        dataBaseRef.child("user").child(uid)
            // Store the user's email, uid, and username, and set other fields (e.g., name, icon) to null
            .setValue(Users(name = null, email, uid, username = username, icon = null))
    }

    // Uploads a user icon to Firebase Storage and stores the download URL in the database
    fun addUserIcon(uid: String, iconUri: Uri) {
        // Get the content resolver to determine the MIME type of the image (e.g., JPEG, PNG)
        val contentResolver = context?.contentResolver
        val mimeType = contentResolver?.getType(iconUri)

        // Determine the file extension based on the MIME type (defaults to "jpg" if unknown)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "jpg"
        }

        // Create a reference to where the user's icon will be stored in Firebase Storage
        val iconRef = storageRef.child("userImages/$uid/${UUID.randomUUID()}.$extension")

        // Upload the file to Firebase Storage
        iconRef.putFile(iconUri).addOnSuccessListener {
            // After a successful upload, retrieve the download URL of the uploaded file
            iconRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the download URL of the icon to the user's node in the database
                dataBaseRef.child("user").child(uid).child("icon").setValue(uri.toString())
            }.addOnFailureListener {
                // If there's an error retrieving the download URL, show a toast message to the user
                Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // If there's an error uploading the file, show a toast message to the user
            Toast.makeText(context, "Failed to upload icon", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetches users from the database and updates the user list and adapter
    fun getUsersFromDataBase(
        userList: ArrayList<Users>, // List to store the fetched users
        adapter: UsersAdapter, // Adapter to update the RecyclerView with the fetched users
        callback: DataFetchCallback // Callback to notify when data fetching is complete
    ) {
        // Add a value event listener to the "user" node in the database
        dataBaseRef.child("user").addValueEventListener(object : ValueEventListener {
            // Called when the data changes or is initially loaded
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // Clear the existing user list
                for (user in snapshot.children) {
                    try {
                        // Parse each user's data into a map
                        val userData = user.value as? Map<String, Any>
                        if (userData != null) {
                            // Create a new Users object from the parsed data
                            val currentUser = Users(
                                uid = userData["uid"] as? String ?: "", // Get the user's UID
                                username = userData["username"] as? String ?: "" // Get the user's username
                            )
                            // Only add users that are not the currently authenticated user
                            if (currentUser.uid != auth.currentUser!!.uid) {
                                userList.add(currentUser) // Add the user to the userList
                            }
                        }
                    } catch (e: Exception) {
                        // Log any errors that occur during parsing of user data
                        Log.e("DatabaseError", "Error parsing user data", e)
                    }
                }
                // Notify the adapter that the data has changed, so the UI can be updated
                adapter.notifyDataSetChanged()
                // Call the callback method to signal that data fetching is complete
                callback.onDataFetched()
            }

            // Called if there's an error fetching data from the database
            override fun onCancelled(error: DatabaseError) {
                // Log the error if data fetching is cancelled or fails
                Log.e("DatabaseError", "Error fetching data", error.toException())
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
                        snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {}) ?: arrayListOf()

                    for (friendId in friendsIds) {
                        dataBaseRef.child("user").child(friendId).get()
                            .addOnSuccessListener { userSnapshot ->
                                val userData = userSnapshot.getValue() as Map<String, Any>?
                                if (userData != null) {
                                    val uid = userData["uid"] as? String ?: ""
                                    val username = userData["username"] as? String ?: ""
                                    val user = Users(uid = uid, username = username)
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

    // ================ NOTIFICATIONS ==============================================================
    fun addFriendChatMessage(senderRoom: String, receiverRoom: String, messageObject: Message) {
        // Store the message in the sender's chat room
        dataBaseRef.child("friendChat").child(senderRoom).child("messages").push()
            .setValue(messageObject).addOnSuccessListener {
                // After success, store the message in the receiver's chat room
                dataBaseRef.child("friendChat").child(receiverRoom).child("messages").push()
                    .setValue(messageObject).addOnSuccessListener {
                        // After adding the message to both sender's and receiver's chat rooms, trigger the notification
                        triggerNotificationToFriend(messageObject.receiverUid!!, messageObject.message)
                    }
            }
    }

    // Function to trigger FCM notification to a friend using Realtime Database
    private fun triggerNotificationToFriend(friendUid: String, message: String) {
        // Get the friends FCM token and details from the Realtime Database
        val userRef = dataBaseRef.child("user").child(friendUid)

        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val friendFcmToken = dataSnapshot.child("fcmToken").getValue(String::class.java)
                val friendName = dataSnapshot.child("username").getValue(String::class.java)
                val friendIcon = dataSnapshot.child("icon").getValue(String::class.java)

                Log.d("FCM Token", "Token: $friendFcmToken") // debug
                Log.d("Friend username", "username: $friendName")  // debug
                Log.d("Friend Icon", "Icon: $friendIcon")  // debug

                // Send the notification via FCM
                if (friendFcmToken != null && friendName != null && friendIcon != null) {
                    Log.d("sendFCMNotification", "Notification Fun Send")  // debug
                    sendFCMNotification(friendFcmToken, friendName, friendUid, friendIcon, message)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("DatabaseError", "Error fetching user details: ", exception)
        }
    }

    // Function to send the actual FCM notification in Kotlin
    private fun sendFCMNotification(friendFcmToken: String, friendName: String, friendUid: String, friendIcon: String, message: String) {
        val jsonObject = JSONObject().apply {
            val notificationObject = JSONObject().apply {
                put("title", "New message from $friendName")
                put("body", message)
            }
            val dataObject = JSONObject().apply {
                put("friendName", friendName)
                put("friendUid", friendUid)
                put("friendIcon", friendIcon)
            }
            put("notification", notificationObject)
            put("data", dataObject)
            put("to", friendFcmToken)
        }

        val client = OkHttpClient()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .post(requestBody)
            .addHeader("Authorization", "key=AIzaSyDB24uVr8v76ti0Cd5x-nWPUfrP4OlnHPo")  // Firebase server key from google credential
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCMNotification", "Failed to send FCM notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        Log.e("FCMNotification", "FCM Notification Response Failed: ${response.body?.string()}")
                    } else {
                        Log.i("FCMNotification", "FCM Notification Response: ${response.body?.string()}")
                    }
                }
            }
        })
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

        // First, fetch the username from the Realtime Database for the user
        dataBaseRef.child("user").child(uid).child("username").get().addOnSuccessListener { snapshot ->
            val username = snapshot.getValue(String::class.java) ?: "Unknown"

            postImageRef.putFile(imageUri).addOnSuccessListener {
                postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val post = Post(
                        postId = postId,
                        user_id = uid,
                        username = username,  // Add the username here
                        image_url = downloadUri.toString(),
                        caption = caption,
                        likes = mutableMapOf(), // Initialize with a mutable map
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
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to get username", Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(context, "Failed to unlike post: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // User has not liked the post yet, so we add the like
                    postRef.child("likes").child(userId).setValue(true)
                        .addOnSuccessListener {
                            callback(true) // Liked
                            Toast.makeText(context, "Post liked!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to like post: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to check like status.", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
