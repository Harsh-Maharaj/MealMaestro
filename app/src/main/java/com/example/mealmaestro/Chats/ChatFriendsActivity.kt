package com.example.mealmaestro.Chats

import android.content.SharedPreferences
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
import com.example.mealmaestro.R
import com.example.mealmaestro.databinding.ActivityChatFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

// ChatFriendsActivity manages the chat interface between users
class ChatFriendsActivity : AppCompatActivity() {

    // Binding for the activity's layout
    private lateinit var binding: ActivityChatFriendsBinding

    // Views from the layout
    private lateinit var chatFriends: RecyclerView
    private lateinit var cameraText: ImageView
    private lateinit var imageText: ImageView

    // Adapter and data list for chat messages
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>

    // Firebase database helper and reference
    private lateinit var dataBase: DataBase
    private lateinit var dataBaseRef: DatabaseReference

    // Variables to store chat room identifiers
    var receiverRoom: String? = null
    var senderRoom: String? = null

    // onCreate method to initialize activity components
    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemeFromPreferences()  // Apply the selected theme before setting the layout
        super.onCreate(savedInstanceState)

        binding = ActivityChatFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Enable edge-to-edge layout behavior for immersive experience
        enableEdgeToEdge()

        // Apply window insets for proper layout adjustment with system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.chatFriends) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name = intent.getStringExtra("username")
        val receiverUid = intent.getStringExtra("uid")
        val icon = intent.getStringExtra("icon")
        val senderUid = FirebaseAuth.getInstance().currentUser!!.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        binding.cahtToolBarName.text = name
        Glide.with(this)
            .load(icon)
            .into(binding.chatToolBarImg)

        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList)
        dataBase = DataBase()

        chatFriends = binding.chatFriends
        chatFriends.layoutManager = LinearLayoutManager(this@ChatFriendsActivity)
        chatFriends.adapter = adapter

        binding.sendMessage.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isNotEmpty() && receiverUid != null) {
                val messageObject = Message(message, senderUid, receiverUid)

                dataBase.addFriendChatMessage(
                    senderRoom!!,
                    receiverRoom!!,
                    messageObject
                )

                binding.textMessage.text.clear()
            }
        }

        dataBase.getFriendMessage(senderRoom!!, messageList, adapter)

        binding.chatBack.setOnClickListener {
            finish()
        }
    }

    // Apply the selected theme from SharedPreferences
    private fun applyThemeFromPreferences() {
        val sharedPreferences: SharedPreferences =
            android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = sharedPreferences.getInt("SelectedTheme", 0)
        when (selectedTheme) {
            1 -> setTheme(R.style.DynamicTheme1)
            2 -> setTheme(R.style.DynamicTheme2)
            3 -> setTheme(R.style.DynamicTheme3)
            4 -> setTheme(R.style.DynamicTheme4)
        }
    }
}
