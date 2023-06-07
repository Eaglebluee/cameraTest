package com.example.cameratest.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FaceOverlayView(context: Context?, attrs: AttributeSet?) :
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

    var resultNum = 0

    private val guideLimit = -800
    private val frontLimit = 150
    private lateinit var guideText: String

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

        linePaint.color = Color.WHITE
        linePaint.strokeWidth = 4F
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.WHITE
        pointPaint.strokeWidth = 4F
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
                drawResultScreen(canvas)
            }
        }
    }

    private fun drawDetectScreen(canvas: Canvas) {
        results?.let { faceLandmarkerResult ->
            for(landmark in faceLandmarkerResult.faceLandmarks()) {

                val leftCheekX = landmark[234].x() * imageWidth * scaleFactor * 0.75f
                val leftCheekY = landmark[234].y() * imageHeight * scaleFactor * 0.75f
                val rightCheekX = landmark[454].x() * imageWidth * scaleFactor * 0.75f
                val rightCheekY = landmark[454].y() * imageHeight * scaleFactor * 0.75f
                val foreHead = landmark[10]
                val jaw = landmark[152]

                val top = foreHead.y() * imageHeight * scaleFactor * 0.5f
                val left = leftCheekX
                val right = rightCheekX
                val bottom = jaw.y() * imageHeight * scaleFactor * 0.75f
                val distance = top - bottom

                // Draw bounding box guideline
                val guideTop = height / 9f
                val guideBottom = height / 9f * 6f
                val guideLeft = width / 7f
                val guideRight = width / 7f * 6f

                val inGuide = top > guideTop && bottom < guideBottom && left > guideLeft && right < guideRight
                val inLimit = distance < guideLimit

                // 양옆 차이
                val sideDiff = abs((
                        landmark[454].x() * imageWidth * scaleFactor * 0.75f - landmark[1].x() * imageWidth * scaleFactor * 0.75f) -
                        (landmark[1].x() * imageWidth * scaleFactor * 0.75f - landmark[234].x() * imageWidth * scaleFactor * 0.75f)
                )

                // 위 아래 차이
                val upDownDiff = abs(
                    (landmark[1].y() * imageWidth * scaleFactor * 0.75f - landmark[10].y() * imageWidth * scaleFactor * 0.75f) -
                        (landmark[152].y() * imageWidth * scaleFactor * 0.75f - landmark[1].y() * imageWidth * scaleFactor * 0.75f)
                )

                val isFront = sideDiff < 150 && upDownDiff < 150

                _isInGuideLine.value =  inGuide && inLimit && isFront
                boxPaint.color = ContextCompat.getColor(context!!, if(isInGuideLine.value!!) R.color.isInGuideLine else R.color.isOutGuideLine)

                val drawableRect = RectF(left, top, right, bottom)
                canvas.drawRect(drawableRect, boxPaint)

                val drawableGuideRect = RectF(guideLeft, guideTop, guideRight, guideBottom)
                canvas.drawRect(drawableGuideRect, guidePaint)
                canvas.drawLine(leftCheekX, leftCheekY, rightCheekX, rightCheekY, guidePaint)


                guideText = when {
                    inGuide && !inLimit -> {
                        "조금 더 가까이 와주세요"
                    }
                    inGuide && !isFront -> {
                        "얼굴을 정면으로 맞춰주세요"
                    }
                    inGuide && inLimit && isFront -> {
                        "아래 버튼을 눌러 촬영해주세요"
                    }
                    else -> {
                        "얼굴을 가이드라인에 맞춰주세요"
                    }
                }

                canvas.drawText(
                    guideText,
                    guideLeft + 50f,
                    guideBottom + 100f,
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

                val leftEyebrowList = listOf(336, 296, 334, 293, 300)

                val rightEyebrowList = listOf(107, 66, 105, 63, 70)

                val mouthList = listOf(0, 267, 269, 270, 409, 291, 292, 415, 310, 311, 312, 13, 82, 81, 80, 191,
                    62, 95, 88, 178, 87, 14, 317, 402, 318, 324, 291, 375, 321, 405, 314, 17,
                    84, 181, 91, 146, 61, 185, 40, 39, 37, 0)

                val noseList = listOf(99, 240, 48, 49, 209, 198, 174, 245,  193, 168, 417, 465, 399, 420, 429, 279, 278 , 460, 328, 2, 99)

                val drawList = listOf(faceList, leftEyeList, rightEyeList, leftEyebrowList, rightEyebrowList, mouthList, noseList)

                drawList.forEach { list ->
                    for(i in list.indices) {
                        val pointX = landmark[list[i]].x() * imageWidth * scaleFactor
                        val pointY = landmark[list[i]].y() * imageHeight * 2.25f
                        canvas.drawPoint(pointX, pointY, pointPaint)
                    }
                }

            }

        }
    }

    private fun drawResultScreen(canvas: Canvas) {
        results?.let { faceLandmarkerResult ->
            for(landmark in faceLandmarkerResult.faceLandmarks()) {
                val faceList = listOf(10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400,
                    377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 10)

                val eyeList = listOf(263, 466, 388, 387, 386, 385, 384, 398, 362, 382, 381, 380, 374, 373, 390, 249, 33, 246, 161, 160, 159, 158, 157, 173, 133, 155, 154, 153, 145, 144, 163, 7)

                val eyebrowList = listOf(336, 296, 334, 293, 300, 107, 66, 105, 63, 70)

                val mouthList = listOf(0, 267, 269, 270, 409, 291, 292, 415, 310, 311, 312, 13, 82, 81, 80, 191,
                    62, 95, 88, 178, 87, 14, 317, 402, 318, 324, 291, 375, 321, 405, 314, 17,
                    84, 181, 91, 146, 61, 185, 40, 39, 37, 0)

                val noseList = listOf(99, 240, 48, 49, 209, 198, 174, 245,  193, 168, 417, 465, 399, 420, 429, 279, 278 , 460, 328, 2, 99)

                val drawList = listOf(faceList, eyeList, eyebrowList, noseList, mouthList)

                val xDiff = width / 8f

                for(i in 0 until drawList[resultNum].size) {
                    val pointX = landmark[drawList[resultNum][i]].x() * imageWidth * scaleFactor + xDiff
                    val pointY = landmark[drawList[resultNum][i]].y() * imageHeight * scaleFactor
                    canvas.drawPoint(pointX, pointY, pointPaint)
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
                // max(width * 1f / imageWidth, height * 1f / imageHeight)
                3.046875f
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
    }
}