package com.example.veedio

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_storage.*
import java.io.*

class StorageActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "storageActivity"
    private val CHOOSING_IMAGE_REQUEST = 1234

    private var fileUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var imageReference: StorageReference? = null
    private var fileRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        tvFileName.text = ""
        imageReference = FirebaseStorage.getInstance().reference.child("image")

        btn_choose_file.setOnClickListener(this)
        btn_upload_byte.setOnClickListener(this)
        btn_upload_file.setOnClickListener(this)
        btn_upload_stream.setOnClickListener(this)
        btn_back.setOnClickListener(this)

        btn_download_byte.setOnClickListener(this)
        btn_download_file.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        val i = v!!.id

        when(i){

            R.id.btn_choose_file -> showChoosingFile()
            R.id.btn_upload_byte -> uploadBytes()
            R.id.btn_upload_file -> uploadFile()
            R.id.btn_upload_stream -> uploadStream()
            R.id.btn_back -> finish()

            R.id.btn_download_byte -> downloadInMemory(fileRef)
            R.id.btn_download_file -> downloadToLocalFile(fileRef)
        }
    }

    private fun uploadBytes() {
        if (fileUri != null) {
            val fileName = edtFileName.text.toString()

            if (!validateInputFileName(fileName)) {
                return
            }

            val baos = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data: ByteArray = baos.toByteArray()

            val fileRef = imageReference!!.child(fileName + "." + getFileExtension(fileUri!!))
            fileRef.putBytes(data)
                .addOnSuccessListener { taskSnapshot ->
                    Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
                    Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                    tvFileName.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                    Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    // progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    // percentage in progress dialog
                    val intProgress = progress.toInt()
                    tvFileName.text = "Uploaded " + intProgress + "%..."
                }
                .addOnPausedListener { System.out.println("Upload is paused!") }

        } else {
            Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadFile() {
        if (fileUri != null) {
            val fileName = edtFileName.text.toString()

            if (!validateInputFileName(fileName)) {
                return
            }

            val fileRef = imageReference!!.child(fileName + "." + getFileExtension(fileUri!!))
            fileRef.putFile(fileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
                    Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                    tvFileName.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                    Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    // progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    // percentage in progress dialog
                    val intProgress = progress.toInt()
                    tvFileName.text = "Uploaded " + intProgress + "%..."
                }
                .addOnPausedListener { System.out.println("Upload is paused!") }

        } else {
            Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadStream() {
        if (fileUri != null) {
            val fileName = edtFileName.text.toString()

            if (!validateInputFileName(fileName)) {
                return
            }

            try {
                val stream: InputStream? = contentResolver.openInputStream(fileUri!!)

                val fileRef = imageReference!!.child(fileName + "." + getFileExtension(fileUri!!))
                if (stream != null) {
                    fileRef.putStream(stream)
                        .addOnSuccessListener { taskSnapshot ->
                            Log.e(TAG, "Uri: " + taskSnapshot.uploadSessionUri)
                            Log.e(TAG, "Name: " + taskSnapshot.metadata!!.name)
                            tvFileName.text = taskSnapshot.metadata!!.path + " - " + taskSnapshot.metadata!!.sizeBytes / 1024 + " KBs"
                            Toast.makeText(this, "File Uploaded ", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                        }
                        .addOnProgressListener { taskSnapshot ->
                            // because this is a stream so:
                            // taskSnapshot.getTotalByteCount() = -1 (always)
                            tvFileName.text = "Uploaded " + taskSnapshot.bytesTransferred + " Bytes..."
                        }
                        .addOnPausedListener { System.out.println("Upload is paused!") }
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "No File!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(bitmap != null) bitmap!!.recycle()

        if(requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null){
            fileUri = data.data
            Log.d("fileUri", data.data.toString())
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                selected_image.setImageBitmap(bitmap)

            }catch (e: IOException){
                e.printStackTrace()
            }
        }

    }

    public fun downloadInMemory(fileRef: StorageReference?) {
        if(fileRef != null){
            tvFileName.text = "Downloading..."
            val ONE_MEGABYTE = (1024 * 1024).toLong()
            fileRef.getBytes(ONE_MEGABYTE)
                .addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    imgFile.setImageBitmap(bmp)
                    tvFileName.text = fileRef.name
                }
                .addOnFailureListener { exception ->
                    tvFileName.text = ""
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Upload file before downloading ", Toast.LENGTH_SHORT).show()
        }
    }
    public fun downloadToLocalFile(fileRef: StorageReference?) {
        if(fileRef != null){
            tvFileName.text = "Downloading..."
            try {
                val localFile: File = File.createTempFile("images", "jpg")
                fileRef.getFile(localFile)
                    .addOnSuccessListener {
                        val bmp = BitmapFactory.decodeFile(localFile.absolutePath)
                        imgFile.setImageBitmap(bmp)
                        tvFileName.text = fileRef.name
                    }
                    .addOnFailureListener{ exception ->
                        tvFileName.text = ""
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        val intProgress = progress.toInt()
                        tvFileName.text = "Downloading " + intProgress + "%..."
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "uploading file before downloading", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showChoosingFile() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()

        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    private fun validateInputFileName(fileName: String): Boolean {
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}