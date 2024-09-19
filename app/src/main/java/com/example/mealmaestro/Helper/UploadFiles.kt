package com.example.mealmaestro.Helper

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

// This class handles file uploads to Firebase Storage and saving the file metadata in Firebase Realtime Database
class UploadFiles(private val context: Context) {
    // Reference to Firebase Storage
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference

    // Create an instance of the DataBase class (assuming it's set up to handle metadata storage)
    private val dataBase = DataBase()

    // Function to upload a file to Firebase Storage
    fun uploadFile(uid: String, fileUri: Uri) {
        // Get the ContentResolver to determine the MIME type of the file
        val contentResolver = context.contentResolver
        val mimeType = contentResolver?.getType(fileUri) // Get the MIME type of the file (e.g., image/jpeg, video/mp4)

        // Determine the folder (images, videos, etc.) and file extension based on the MIME type
        val fileType = when {
            // If the file is an image, set the folder to "images" and extract the file extension (e.g., jpg, png)
            mimeType?.startsWith("image/") == true -> "images" to mimeType.substringAfterLast("/")
            // If the file is a video, set the folder to "videos" and extract the file extension (e.g., mp4)
            mimeType?.startsWith("video/") == true -> "videos" to mimeType.substringAfterLast("/")
            // If the file is a PDF document, set the folder to "documents" and the extension to "pdf"
            mimeType?.startsWith("application/pdf") == true -> "documents" to "pdf"
            // For other file types, set the folder to "others" and use the extracted or default extension (dat)
            else -> "others" to (mimeType?.substringAfterLast("/") ?: "dat")
        }

        val folder = fileType.first  // The folder where the file will be stored in Firebase Storage
        val extension = fileType.second  // The file extension (e.g., jpg, mp4, pdf)

        // Create a reference in Firebase Storage for the file using the user ID, folder, and a unique file name
        val fileRef = storageRef.child("$folder/$uid/${UUID.randomUUID()}.$extension")

        // Start the file upload process
        fileRef.putFile(fileUri).addOnSuccessListener {
            // If the file upload is successful, retrieve the download URL of the uploaded file
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // Call the saveFileMetadata method from the DataBase class to save the file metadata
                dataBase.saveFileMetadata(uid, uri.toString(), mimeType, folder)

                // Show a success message to the user
                Toast.makeText(context, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // If the file upload fails, show an error message to the user
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_SHORT).show()
        }
    }
}

