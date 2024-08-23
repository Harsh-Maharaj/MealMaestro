package com.example.mealmaestro

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class UploadFiles (private val context: Context){
    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val dataBase = DataBase()

    fun uploadFile(uid: String, fileUri: Uri) {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver?.getType(fileUri)

        // Determine the file type based on MIME type
        val fileType = when {
            mimeType?.startsWith("image/") == true -> "images" to mimeType.substringAfterLast("/")
            mimeType?.startsWith("video/") == true -> "videos" to mimeType.substringAfterLast("/")
            mimeType?.startsWith("application/pdf") == true -> "documents" to "pdf"
            else -> "others" to (mimeType?.substringAfterLast("/") ?: "dat")
        }

        // Use fileType directly
        val folder = fileType.first
        val extension = fileType.second

        // Create a reference in the appropriate folder
        val fileRef = storageRef.child("$folder/$uid/${UUID.randomUUID()}.$extension")

        // Upload the file
        fileRef.putFile(fileUri).addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // Store the file URL or other metadata if needed
                Toast.makeText(context, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                dataBase.saveFileMetadata(uid, uri.toString(), mimeType, folder)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_SHORT).show()
        }
    }


    /*
        val imageUri: Uri = ... // Image URI obtained from the user's device
        uploadFile(uid, imageUri)

        val videoUri: Uri = ... // Video URI obtained from the user's device
        uploadFile(uid, videoUri)

        val pdfUri: Uri = ... // PDF URI obtained from the user's device
        uploadFile(uid, pdfUri)
    */
}