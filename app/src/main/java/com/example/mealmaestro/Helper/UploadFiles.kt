package com.example.mealmaestro.Helper

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class UploadFiles(private val context: Context) {
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val dataBase = DataBase()  // Assuming DataBase class is set up correctly

    // Function to handle file upload
    fun uploadFile(uid: String, fileUri: Uri) {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver?.getType(fileUri)

        // Determine the folder and file extension based on MIME type
        val fileType = when {
            mimeType?.startsWith("image/") == true -> "images" to mimeType.substringAfterLast("/")
            mimeType?.startsWith("video/") == true -> "videos" to mimeType.substringAfterLast("/")
            mimeType?.startsWith("application/pdf") == true -> "documents" to "pdf"
            else -> "others" to (mimeType?.substringAfterLast("/") ?: "dat")
        }

        val folder = fileType.first  // The folder where the file will be stored
        val extension = fileType.second  // File extension (jpg, mp4, pdf, etc.)

        // Create a reference for the file in Firebase Storage
        val fileRef = storageRef.child("$folder/$uid/${UUID.randomUUID()}.$extension")

        // Start the file upload
        fileRef.putFile(fileUri).addOnSuccessListener {
            // On success, get the download URL
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // Store the file metadata using the saveFileMetadata method from DataBase class
                dataBase.saveFileMetadata(uid, uri.toString(), mimeType, folder)
                Toast.makeText(context, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            // Handle any failures in the upload process
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_SHORT).show()
        }
    }
}
