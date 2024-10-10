package com.example.mealmaestro.Chats

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivityChatFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

// ChatFriendsActivity manages the chat interface between users
class ChatFriendsActivity : AppCompatActivity() {

    // Binding for the activity's layout
    private lateinit var binding: ActivityChatFriendsBinding

    // Views from the layout
    private lateinit var chatFriends: RecyclerView // RecyclerView for displaying chat messages
    private lateinit var cameraText: ImageView // ImageView for camera option (currently unused)
    private lateinit var imageText: ImageView // ImageView for image option (currently unused)

    // Adapter and data list for chat messages
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    // Firebase database helper and reference
    private lateinit var dataBase: DataBase
    private lateinit var dataBaseRef: DatabaseReference

    // Variables to store chat room identifiers
    var receiverRoom: String? = null // Chat room for the receiver
    var senderRoom: String? = null // Chat room for the sender

    // onCreate method to initialize activity components
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatFriendsBinding.inflate(layoutInflater) // Inflate the layout
        setContentView(binding.root)

        // Enable edge-to-edge layout behavior for immersive experience
        enableEdgeToEdge()

        // Apply window insets for proper layout adjustment with system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.chatFriends) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get data passed from the previous activity (e.g., user details for the chat)
        val name = intent.getStringExtra("username") // Receiver's username
        val receiverUid = intent.getStringExtra("uid") // Receiver's UID (User ID)
        val icon = intent.getStringExtra("icon") // Receiver's profile icon URL
        val senderUid = FirebaseAuth.getInstance().currentUser!!.uid // Current user's UID

        // Set up chat room identifiers by combining sender and receiver UIDs
        senderRoom = receiverUid + senderUid // Unique room for the sender
        receiverRoom = senderUid + receiverUid // Unique room for the receiver

        // Set the receiver's name and icon in the chat toolbar
        binding.cahtToolBarName.text = name
        Glide.with(this)
            .load(icon) // Load the receiver's profile picture
            .into(binding.chatToolBarImg) // Set the profile picture in the toolbar

        // Initialize message list and adapter for displaying messages
        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList)
        dataBase = DataBase() // Initialize the custom database helper

        // Set up the RecyclerView with the message adapter and layout manager
        chatFriends = binding.chatFriends
        chatFriends.layoutManager = LinearLayoutManager(this@ChatFriendsActivity)
        chatFriends.adapter = adapter

        // Set up the send button to send messages
        binding.sendMessage.setOnClickListener {
            val message = binding.textMessage.text.toString() // Get the typed message from the input field
            val receiverUid = intent.getStringExtra("uid") // Ensure this is not null

            if (message.isNotEmpty() && receiverUid != null) { // Check for non-empty message and non-null receiverUid
                // Create a Message object with the typed message and user details
                val messageObject = Message(message, senderUid, receiverUid)

                // Send the message to the database for both sender and receiver rooms
                dataBase.addFriendChatMessage(
                    senderRoom!!,
                    receiverRoom!!,
                    messageObject, this
                )

                // Clear the input field after sending the message
                binding.textMessage.text.clear()
            }
        }


        // Display messages in the chat by retrieving them from the database
        dataBase.getFriendMessage(senderRoom!!, messageList, adapter)

        // Set up the back button to finish the activity and return to the previous screen
        binding.chatBack.setOnClickListener {
            finish()
        }
    }
}
