package com.example.mealmaestro.Chats

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.databinding.ActivityChatFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChatFriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatFriendsBinding
    private lateinit var chatFriends: RecyclerView
    private lateinit var imageText: ImageView
    private lateinit var adapter: MessageAdapter
    private lateinit var messageList: ArrayList<Message>
    private lateinit var dataBase: DataBase
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private var receiverRoom: String? = null
    private var senderRoom: String? = null

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

        val name = intent.getStringExtra("username")
        receiverUid = intent.getStringExtra("uid")!!
        val icon = intent.getStringExtra("icon")
        senderUid = FirebaseAuth.getInstance().currentUser!!.uid

        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

        binding.cahtToolBarName.text = name
        Glide.with(this).load(icon).into(binding.chatToolBarImg)

        messageList = ArrayList()
        adapter = MessageAdapter(this, messageList, senderRoom!!, receiverRoom!!)
        dataBase = DataBase()

        chatFriends = binding.chatFriends
        chatFriends.layoutManager = LinearLayoutManager(this@ChatFriendsActivity)
        chatFriends.adapter = adapter

        binding.sendMessage.setOnClickListener {
            val message = binding.textMessage.text.toString()
            if (message.isNotEmpty()) {
                val messageObject = Message(message, senderUid, receiverUid)
                dataBase.addFriendChatMessage(senderRoom!!, receiverRoom!!, messageObject, this)
                binding.textMessage.text.clear()
            }
        }

        binding.cameraText.setOnClickListener {
            openCamera()
        }

        dataBase.getFriendMessage(senderRoom!!, messageList, adapter)

        binding.chatBack.setOnClickListener { finish() }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraResultLauncher.launch(takePictureIntent)
        }
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                val imageUri = saveBitmapToCache(imageBitmap)
                if (imageUri != null) {
                    dataBase.uploadImageToFirebase(imageUri, senderRoom!!, receiverRoom!!, senderUid, receiverUid, this)
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val file = File(cacheDir, "${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            Uri.fromFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2
    }
}
