package com.afjltd.tracking.utils


import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions




typealias CameraTextAnalyzerListener = (text: Text) -> Unit


class TextAnalyser(
    private val textListener: CameraTextAnalyzerListener,
    private var context: Context,
    private val fromFile: Uri,
) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun analyseImage() {

        try {
            val image = InputImage.fromFilePath(context, fromFile)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    AFJUtils.writeLogs("text = ${visionText.text}")
                        textListener(visionText)
                }
                .addOnFailureListener { e ->

                    AFJUtils.writeLogs("text Error = ${e.localizedMessage}")
                }
        } catch (e: Exception) {
            AFJUtils.writeLogs("text Exception = ${e.localizedMessage}")

        }
    }


}










