package com.futsoccerchamp.presentation.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

private const val MAX_SIZE = 256
private const val QUALITY = 80

fun Uri.toCompressedBase64(context: Context): String? = runCatching {
    val bitmap = context.contentResolver.openInputStream(this).use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: return null

    val scale = max(bitmap.width, bitmap.height).toFloat() / MAX_SIZE
    val resized = if (scale <= 1f) {
        bitmap
    } else {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width / scale).toInt().coerceAtLeast(1),
            (bitmap.height / scale).toInt().coerceAtLeast(1),
            true
        )
    }

    ByteArrayOutputStream().use { output ->
        resized.compress(Bitmap.CompressFormat.JPEG, QUALITY, output)
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }
}.getOrNull()

fun String.decodeBase64Image(): Bitmap? = runCatching {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()
