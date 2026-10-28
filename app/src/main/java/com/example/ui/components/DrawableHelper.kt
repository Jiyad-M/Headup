package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun rememberDrawableBitmap(drawable: Drawable?): ImageBitmap? {
    return remember(drawable) {
        drawable?.let { d ->
            try {
                if (d is BitmapDrawable && d.bitmap != null) {
                    d.bitmap.asImageBitmap()
                } else {
                    val width = if (d.intrinsicWidth > 0) d.intrinsicWidth else 96
                    val height = if (d.intrinsicHeight > 0) d.intrinsicHeight else 96
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                    bitmap.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
