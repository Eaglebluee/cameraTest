package com.example.cameratest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var guidePaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var bounds = Rect()

    private val _isInGuideLine = MutableLiveData(false)
    val isInGuideLine : LiveData<Boolean> =  _isInGuideLine

    private var screenState: MainViewModel.ScreenState = MainViewModel.ScreenState.Detect

    init {
        initPaints()
    }

    fun clear() {
        results = null
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.mp_primary)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE

        guidePaint.color = ContextCompat.getColor(context!!, R.color.icActive)
        guidePaint.strokeWidth = 8F
        guidePaint.style = Paint.Style.STROKE

        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        when(screenState) {
            MainViewModel.ScreenState.Detect -> {
                drawDetectScreen(canvas)
            }
            MainViewModel.ScreenState.Analyze -> {
                drawAnalyzeScreen(canvas)
            }
            MainViewModel.ScreenState.Result -> {

            }
        }


    }

    private fun drawDetectScreen(canvas: Canvas) {
        results?.let { faceLandmarkerResult ->
            for(landmark in faceLandmarkerResult.faceLandmarks()) {
//                for(normalizedLandmark in landmark) {
//                    canvas.drawPoint(normalizedLandmark.x() * imageWidth * scaleFactor * 0.725f, normalizedLandmark.y() * imageHeight * 2.25f, pointPaint)
//                }

                val leftCheekX = landmark[234].x() * imageWidth * scaleFactor * 0.725f
                val leftCheekY = landmark[234].y() * imageHeight * 2.25f
                val rightCheekX = landmark[454].x() * imageWidth * scaleFactor * 0.725f
                val rightCheekY = landmark[454].y() * imageHeight * 2.25f
                val foreHead = landmark[10]
                val jaw = landmark[152]

                val top = foreHead.y() * imageHeight * 2.25f - 20f
                val left = leftCheekX - 20f
                val right = rightCheekX + 20f
                val bottom = jaw.y() * imageHeight * 2.25f + 20f

                // Draw bounding box guideline
                val guideTop = 200f
                val guideBottom = 1050f
                val guideLeft = 150f
                val guideRight = 900f

                _isInGuideLine.value = top > guideTop && bottom < guideBottom && left > guideLeft && right < guideRight
                boxPaint.color = ContextCompat.getColor(context!!, if(isInGuideLine.value!!) R.color.isInGuideLine else R.color.isOutGuideLine)

                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)

                val drawableGuideRect = RectF(guideLeft, guideTop, guideRight, guideBottom)
                canvas.drawRect(drawableGuideRect, guidePaint)

                canvas.drawPoint(leftCheekX, leftCheekY, pointPaint)
                canvas.drawPoint(rightCheekX, rightCheekY, pointPaint)
                canvas.drawLine(leftCheekX, leftCheekY, rightCheekX, rightCheekY, guidePaint)


                canvas.drawText(
                    "얼굴을 가이드라인에 맞춰주세요",
                    guideLeft,
                    guideBottom + 250f,
                    textPaint
                )

            }
        }
    }


    private fun drawAnalyzeScreen(canvas: Canvas) {
        results?.let { faceLandmarkerResult ->
            for(landmark in faceLandmarkerResult.faceLandmarks()) {
                val faceList = listOf(10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400,
                    377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 10)

                val leftEyeList = listOf(263, 466, 388, 387, 386, 385, 384, 398, 362, 382, 381, 380, 374, 373, 390, 249, 263)

                val rightEyeList = listOf(33, 246, 161, 160, 159, 158, 157, 173, 133, 155, 154, 153, 145, 144, 163, 7, 33)

                val leftEyebrowList = listOf(336, 296, 334, 293, 300, 336)

                val rightEyebrowList = listOf(107, 66, 105, 63, 70, 107)

                val mouthList = listOf(0, 267, 269, 270, 409, 291, 292, 415, 310, 311, 312, 13, 82, 81, 80, 191,
                    62, 95, 88, 178, 87, 14, 317, 402, 318, 324, 291, 375, 321, 405, 314, 17,
                    84, 181, 91, 146, 61, 185, 40, 39, 37, 0)

                val noseList = listOf(99, 240, 48, 49, 209, 198, 174, 245,  193, 168, 417, 465, 399, 420, 429, 279, 278 , 460, 328, 2, 99)

                val drawList = listOf(faceList, leftEyeList, rightEyeList, leftEyebrowList, rightEyebrowList, mouthList, noseList)

                drawList.forEach { list ->
                    for(i in 0 until list.size - 1) {
                        val startX = landmark[list[i]].x() * imageWidth * scaleFactor
                        val startY = landmark[list[i]].y() * imageHeight * 2.25f
                        val endX = landmark[list[i+1]].x() * imageWidth * scaleFactor
                        val endY = landmark[list[i+1]].y() * imageHeight * 2.25f
                        canvas.drawLine(startX, startY, endX, endY, guidePaint)
                    }
                }

            }

        }
    }


    fun setResults(
        faceLandmarkerResults: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        screenState: MainViewModel.ScreenState
    ) {
        results = faceLandmarkerResults

        this.screenState = screenState

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}