package com.example.uploadphotoapp

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    fun changeBrightness(imageView: ImageView, brightnessFactor: Float) {
        val matrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    brightnessFactor, 0f, 0f, 0f, 0f,
                    0f, brightnessFactor, 0f, 0f, 0f,
                    0f, 0f, brightnessFactor, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        imageView.colorFilter = ColorMatrixColorFilter(matrix)
    }

    fun changeContrast(imageView: ImageView, contrastValue: Int) {
        val contrast = contrastValue / 50f
        val colorMatrix = ColorMatrix().apply {
            set(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, 0f,
                    0f, contrast, 0f, 0f, 0f,
                    0f, 0f, contrast, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        val colorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
        imageView.colorFilter = colorMatrixFilter
    }


}