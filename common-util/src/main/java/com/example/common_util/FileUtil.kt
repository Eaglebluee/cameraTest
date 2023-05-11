package com.example.common_util

import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtil {

    fun writeBitmapToFile(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat?,
        quality: Int,
        filePath: String
    ) {
        var fileOutputStream: FileOutputStream? = null

        try {
            fileOutputStream = FileOutputStream(filePath)
            bitmap.compress(compressFormat, quality, fileOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush()
                fileOutputStream.close()
            }
        }
    }

    fun makeFile(outputDir: String, uri: Uri): File {
        val directory = File(outputDir)
        if (!directory.exists()) directory.mkdirs()

        // Prepare the new file name and path
        val destinationFilePath = getDestinationFilePath(outputDir, uri)

        return File(destinationFilePath)
    }

    private fun getDestinationFilePath(outputDirPath: String?, imageUri: Uri?): String {
        val originalFileName = File(imageUri?.path).name
        return outputDirPath + File.separator + originalFileName
    }

}