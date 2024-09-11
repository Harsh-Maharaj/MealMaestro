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

class ChatFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFriendsBinding
    private lateinit var chatFriends: RecyclerView
    private lateinit var cameraText: ImageView
    private lateinit var imageText: ImageView

    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dataBase: DataBase
    private lateinit var dataBaseRef: DatabaseReference

    var receiverRoom: String? = null
    var senderRoom: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.chatFriends) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val name = intent.getStringExtra("name")
        val receiverUid = intent.getStringExtra("uid")
        val icon = intent.getStringExtra("icon")
        val senderUid = FirebaseAuth.getInstance().currentUser!!.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        binding.cahtToolBarName.text = name // assign name to the top bar
        Glide.with(this)
            .load(icon)  // assign the icon of the user
            .into(binding.chatToolBarImg)


        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList)
        dataBase = DataBase()

        chatFriends = binding.chatFriends
        chatFriends.layoutManager = LinearLayoutManager(this@ChatFriendsActivity)
        chatFriends.adapter = adapter

        // send message
        binding.sendMessage.setOnClickListener {
            val message = binding.textMessage.text.toString() // message written in the edit text
            val messageObject = Message(message, senderUid) // create a message

            dataBase.addFriendChatMessage(senderRoom!!, receiverRoom!!,messageObject) // send message to database
            binding.textMessage.text.clear() // clear message text box
        }

        // display message in recycler
        dataBase.getFriendMessage(senderRoom!!,messageList, adapter)

        binding.chatBack.setOnClickListener {
            finish()
        }

    }
}