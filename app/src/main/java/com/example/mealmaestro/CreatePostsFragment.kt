package com.example.mealmaestro

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class CreatePostsFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var editTextCaption: EditText
    private lateinit var buttonChooseImage: Button
    private lateinit var buttonPost: Button
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.image_view)
        editTextCaption = view.findViewById(R.id.edit_text_caption)
        buttonChooseImage = view.findViewById(R.id.button_choose_image)
        buttonPost = view.findViewById(R.id.button_post)

        buttonChooseImage.setOnClickListener { openFileChooser() }
        buttonPost.setOnClickListener { uploadPost() }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadPost() {
        if (imageUri != null && editTextCaption.text.isNotEmpty()) {
            val storageReference = FirebaseStorage.getInstance().reference.child("uploads")
            val fileReference = storageReference.child(UUID.randomUUID().toString())

            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val post = hashMapOf(
                            "user_id" to FirebaseAuth.getInstance().currentUser?.uid,
                            "image_url" to uri.toString(),
                            "caption" to editTextCaption.text.toString(),
                            "likes" to mapOf<String, Boolean>(),
                            "created_at" to FieldValue.serverTimestamp()  // Adding timestamp
                        )

                        FirebaseFirestore.getInstance().collection("posts")
                            .add(post)
                            .addOnSuccessListener {
                                // Clear the input fields
                                imageView.setImageResource(0)  // Clear ImageView
                                editTextCaption.text.clear()   // Clear caption EditText
                                imageUri = null

                                // Show a success message
                                Toast.makeText(context, "Post successfully created!", Toast.LENGTH_SHORT).show()

                                // Use the NavController to navigate back to HomeFragment
                                findNavController().navigate(R.id.action_createPostsFragment_to_homeFragment)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to create post. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Please select an image and add a caption.", Toast.LENGTH_SHORT).show()
        }
    }




    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
