package com.example.veedio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKeys
import com.example.veedio.R
import com.example.veedio.data.Photo
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_photo.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PhotoActivity : AppCompatActivity() {

  private var selectedPhoto: Photo? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_photo)

    val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
    val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    selectedPhoto = intent.getSerializableExtra(PHOTO_KEY) as Photo
    Picasso.with(this).load(selectedPhoto?.url).into(
      object: Target{
        override fun onBitmapFailed(errorDrawable: Drawable?) {
          Log.d("Error", "Error")
        }

        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
          photoImageView.setImageBitmap(bitmap)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
          TODO("Not yet implemented")
        }
      }
    )



    photoDescription?.text = selectedPhoto?.explanation
    storageBtn.setOnClickListener{
      val intent = Intent(this, StorageActivity::class.java).apply {
//        putExtra("IMAGE", )
      }
      startActivity(Intent(this, StorageActivity::class.java))
    }

    showImage.setOnClickListener {
      val encryptedFile = EncryptedFile.Builder(
        File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myImage.jpeg"),
        applicationContext,
        masterKeyAlias,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
      ).build()
      try {
        val iStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap = BitmapFactory.decodeStream(iStream)
        temp_image_show.setImageBitmap(bitmap)
      } catch (e: IOException) {
        Log.e("IOException", e.localizedMessage)
      }
    }
    downloadBtn.setOnClickListener {
      Toast.makeText(this, "Checking", Toast.LENGTH_SHORT).show()
      val filename = "file"
      Picasso.with(this).load(selectedPhoto?.url).into(
       object: Target{
         override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
         Thread(Runnable {

           val file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myImage.jpeg")
           val encryptedFile = EncryptedFile.Builder(
             File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myImage.jpeg"),
             applicationContext,
             masterKeyAlias,
             EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
           ).build()
           try {
             val oStream = encryptedFile.openFileOutput()
             bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, oStream)
             oStream.flush()
             oStream.close()
           } catch (e: IOException) {
             Log.e("IOException", e.localizedMessage)
           }


         }).start()
         }

         override fun onBitmapFailed(errorDrawable: Drawable?) {
           TODO("Not yet implemented")
         }

         override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
           TODO("Not yet implemented")
         }
       }
      )
    }


  }

  companion object {
    private val PHOTO_KEY = "PHOTO"
  }

  fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
  }

  fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
            setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
  }



}
