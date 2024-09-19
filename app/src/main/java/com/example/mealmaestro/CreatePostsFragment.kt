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

    // UI components
    private lateinit var imageView: ImageView // ImageView for displaying selected image
    private lateinit var editTextCaption: EditText // EditText for user to input caption
    private lateinit var buttonChooseImage: Button // Button to allow user to select an image
    private lateinit var buttonPost: Button // Button to submit the post
    // URI to store the path of the selected image
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_posts, container, false)
    }

    // Initialize the UI components and set up click listeners after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find UI components by their IDs
        imageView = view.findViewById(R.id.image_view)
        editTextCaption = view.findViewById(R.id.edit_text_caption)
        buttonChooseImage = view.findViewById(R.id.button_choose_image)
        buttonPost = view.findViewById(R.id.button_post)

        // Set click listener for choosing an image
        buttonChooseImage.setOnClickListener { openFileChooser() }
        // Set click listener for posting the image and caption
        buttonPost.setOnClickListener { uploadPost() }
    }

    // Open the device's file chooser to allow the user to select an image
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK) // Open file picker
        intent.type = "image/*" // Only allow image files to be selected
        startActivityForResult(intent, PICK_IMAGE_REQUEST) // Start the activity to pick an image
    }

    // Handle the result of the file picker (when the user selects an image)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Check if the result is for image picking and was successful
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data // Get the URI of the selected image
            imageView.setImageURI(imageUri) // Display the selected image in the ImageView
        }
    }

    // Upload the post to Firebase Storage and Firestore
    private fun uploadPost() {
        // Ensure the user selected an image and entered a caption
        if (imageUri != null && editTextCaption.text.isNotEmpty()) {
            // Get a reference to Firebase Storage to store the image
            val storageReference = FirebaseStorage.getInstance().reference.child("uploads")
            val fileReference = storageReference.child(UUID.randomUUID().toString()) // Generate a unique filename

            // Upload the selected image to Firebase Storage
            fileReference.putFile(imageUri!!)
                .addOnSuccessListener {
                    // After a successful upload, get the download URL of the image
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        // Create a new post document with image URL, caption, and metadata
                        val post = hashMapOf(
                            "user_id" to FirebaseAuth.getInstance().currentUser?.uid, // Add user ID of the current user
                            "image_url" to uri.toString(), // Add image URL from Firebase Storage
                            "caption" to editTextCaption.text.toString(), // Add caption entered by the user
                            "likes" to mapOf<String, Boolean>(), // Initialize likes map (empty)
                            "created_at" to FieldValue.serverTimestamp()  // Adding timestamp
                        )

                        // Save the post to Firebase Firestore in the "posts" collection
                        FirebaseFirestore.getInstance().collection("posts")
                            .add(post)
                            .addOnSuccessListener {
                                // Clear the input fields
                                imageView.setImageResource(0)  // Clear ImageView
                                editTextCaption.text.clear()   // Clear caption EditText
                                imageUri = null // Reset the imageUri variable

                                // Show a success message
                                Toast.makeText(context, "Post successfully created!", Toast.LENGTH_SHORT).show()

                                // Use the NavController to navigate back to HomeFragment
                                findNavController().navigate(R.id.action_createPostsFragment_to_homeFragment)
                            }
                            .addOnFailureListener {
                                // Show an error message if the post could not be created
                                Toast.makeText(context, "Failed to create post. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    // Show an error message if the image could not be uploaded
                    Toast.makeText(context, "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Show a message if no image or caption is provided
            Toast.makeText(context, "Please select an image and add a caption.", Toast.LENGTH_SHORT).show()
        }
    }



    // Companion object to hold constant values
    companion object {
        private const val PICK_IMAGE_REQUEST = 1 // Request code for image picking
    }
}
