package com.example.mealmaestro

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.mealmaestro.Chats.Message
import com.example.mealmaestro.Chats.MessageAdapter
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
    constructor():this(null)

    private val auth = FirebaseAuth.getInstance()
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val dataBaseRef =
        FirebaseDatabase.getInstance("https://mealmaestro-46c0d-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference()

    fun addUserToDataBase(email: String, uid: String) {
        dataBaseRef.child("user").child(uid)
            .setValue(Users(username = null, name = null, email, uid, icon = null))
    }

    fun addUserIcon(uid: String, iconUri: Uri){
        // Extract the file extension from the URI
        val contentResolver = context?.contentResolver
        val mimeType = contentResolver?.getType(iconUri)
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "jpg" // Default to jpg if unknown
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

    fun getUsersFromDataBase(userList:ArrayList<Users>, adapter: UsersAdapter){
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
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun addFriendToDataBase(currentUserId: String, newFriendId: String?) {

        if (newFriendId == null) return
        // Reference to the current user's friends array
        val currentUserRef = dataBaseRef.child("user").child(currentUserId).child("friends")

        currentUserRef.get().addOnSuccessListener { snapshot ->
            val friendsList =
                snapshot.getValue(object : GenericTypeIndicator<ArrayList<String>>() {})
                    ?: arrayListOf()
            if (!friendsList.contains(newFriendId)) {
                friendsList.add(newFriendId)
                currentUserRef.setValue(friendsList)
                    .addOnSuccessListener {
                        // Friend added successfully
                        Toast.makeText(context,"Friend added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                        Toast.makeText(context,"Error adding friend: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Friend already exists in the list
                Toast.makeText(context,"Friend already in the list!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            // Handle failure to get the friends list
            println("Error fetching friends list: ${e.message}")
        }
    }

    fun getFriendsList(snapshot: DataSnapshot): ArrayList<String>? {
        val friendsList = snapshot.child("friends").getValue(object : GenericTypeIndicator<ArrayList<String>>() {})
        return friendsList
    }

    fun addFriendChatMessage(senderRoom:String, receiverRoom:String, messageObject: Message){
        dataBaseRef.child("friendChat").child(senderRoom).child("messages").push()
            .setValue(messageObject).addOnSuccessListener {
                dataBaseRef.child("friendChat").child(receiverRoom).child("messages").push()
                    .setValue(messageObject)
            }
    }

    fun getFriendMessage(senderRoom: String, messageList:ArrayList<Message>, adapter: MessageAdapter){
        dataBaseRef.child("friendChat").child(senderRoom).child("messages")
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnap in snapshot.children){
                        val message = postSnap.getValue(Message::class.java)
                        messageList.add(message!!)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    // to save URL of files uploaded
    fun saveFileMetadata(uid: String, fileUrl: String, mimeType: String?, folder: String) {
        val metadata = mapOf(
            "url" to fileUrl,
            "mimeType" to mimeType,
            "folder" to folder,
            "timestamp" to System.currentTimeMillis()
        )
        dataBaseRef.child("user").child(uid).child("files").push().setValue(metadata)
    }
}
