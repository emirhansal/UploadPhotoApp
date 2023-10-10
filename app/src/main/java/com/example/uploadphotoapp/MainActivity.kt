package com.example.uploadphotoapp

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.uploadphotoapp.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setContentView(viewBinding.root)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
        initListener()
    }

    private fun initListener() {
        viewBinding.selectImageButton.setOnClickListener { selectPhoto() }
        viewBinding.crossButton.setOnClickListener { initUI(hasPhoto = false) }
        viewBinding.btnDownload.setOnClickListener { downloadPhoto() }
        viewBinding.sliderBrightness.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val brightnessFactor = progress / 100f
                    viewModel.changeBrightness(viewBinding.photoImageView, brightnessFactor)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            },
        )
        viewBinding.sliderContrast.setOnSeekBarChangeListener(
            object : 
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    viewModel.changeContrast(viewBinding.photoImageView, progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            },
        )
    }

    private fun downloadPhoto() {
        val bitmap = getImageOfView()
        if (bitmap != null) {
            saveToStorage(bitmap)
        }
    }

    private fun getImageOfView(): Bitmap? {
        var image: Bitmap? = null
        try {
            image = Bitmap.createBitmap(
                viewBinding.photoImageView.measuredWidth,
                viewBinding.photoImageView.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(image)
            viewBinding.photoImageView.draw(canvas)
        } catch (e: Exception) {
            Log.e("Failed", "Can not captured!")
        }
        return image
    }

    private fun saveToStorage(bitmap: Bitmap) {
        val imageName = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {
                    resolver.openOutputStream(it)
                }
            }
        }else{
            val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDirectory,imageName)
            fos = FileOutputStream(image)
        }
        fos?.use { file ->
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,file)
            Toast.makeText(this, "SUCCESS",Toast.LENGTH_SHORT).show()
            Log.e("Success", "Captures view successfully!")
        }
    }

    private fun initUI(hasPhoto: Boolean) {
        if (hasPhoto) {
            viewBinding.photoImageView.setImageURI(uri)
            viewBinding.photoImageView.visibility = View.VISIBLE
            viewBinding.sliderBrightness.visibility = View.VISIBLE
            viewBinding.sliderContrast.visibility = View.VISIBLE
            viewBinding.tvBrigtness.visibility = View.VISIBLE
            viewBinding.tvContrast.visibility = View.VISIBLE
            viewBinding.selectImageButton.text = "Change Photo"
            viewBinding.tvInfo.visibility = View.GONE
            viewBinding.crossButton.visibility = View.VISIBLE
            viewBinding.btnDownload.visibility = View.VISIBLE
        } else {
            uri = null
            viewBinding.photoImageView.setImageURI(null)
            viewBinding.tvInfo.visibility = View.VISIBLE
            viewBinding.selectImageButton.visibility = View.VISIBLE
            viewBinding.selectImageButton.text = "Select Photo"
            viewBinding.sliderBrightness.visibility = View.GONE
            viewBinding.sliderContrast.visibility = View.GONE
            viewBinding.tvBrigtness.visibility = View.GONE
            viewBinding.tvContrast.visibility = View.GONE
            viewBinding.photoImageView.visibility = View.GONE
            viewBinding.crossButton.visibility = View.GONE
            viewBinding.btnDownload.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                uri = data?.data!!
                initUI(hasPhoto = true)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun selectPhoto() {
        if (uri != null) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("You have already chosen a photo. Do you want to choose new one?")
                .setTitle("Warning")
                .setPositiveButton("Yes") { _, _ ->
                    ImagePicker.with(this).crop().compress(1024)
                        .maxResultSize(1080, 10800)
                        .start()
                }
                .setNegativeButton("No") { _, _ -> }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            ImagePicker.with(this).crop().compress(1024).maxResultSize(1080, 10800).start()
        }
    }
}
