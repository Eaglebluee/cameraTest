package com.example.cameratest.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class HandOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var guidePaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private val _isInGuideLine = MutableLiveData(false)
    val isInGuideLine: LiveData<Boolean> = _isInGuideLine

    private var screenState: MainViewModel.ScreenState = MainViewModel.ScreenState.Detect

    var handResult = listOf(listOf<Int>())
    var handResultType: HandResult = HandResult.Emotion

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.hand_result_emotion)
        linePaint.strokeWidth = 4F
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        guidePaint.color = ContextCompat.getColor(context!!, R.color.icActive)
        guidePaint.strokeWidth = 8F
        guidePaint.style = Paint.Style.STROKE

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

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

        results?.let { handLandmarkerResult ->

            // Draw bounding box guideline
            val guideTop = height / 20f * 3f
            val guideBottom = height / 20f * 17f
            val guideLeft = width / 40f
            val guideRight = width / 40f * 39f

            for (landmarks in handLandmarkerResult.landmarks()) {

                val inList = BooleanArray(landmarks.size) { false }
                landmarks.forEachIndexed { index, it ->
                    val x = it.x() * imageWidth * scaleFactor * 0.75
                    val y = it.y() * imageHeight * scaleFactor * 0.9
                    if (x > guideLeft && x < guideRight && y > guideTop && y < guideBottom) {
                        inList[index] = true
                    }
                }

                _isInGuideLine.value = inList.all { it }

            }

            guidePaint.color = ContextCompat.getColor(
                context!!,
                if (isInGuideLine.value!!) R.color.isInGuideLine else R.color.isOutGuideLine
            )
            val drawableGuideRect = RectF(guideLeft, guideTop, guideRight, guideBottom)
            canvas.drawRect(drawableGuideRect, guidePaint)

            canvas.drawText(
                "손을 펴서 가이드라인에 맞춰주세요",
                200f,
                height - 150f,
                textPaint
            )
        }
    }

    private fun drawAnalyzeScreen(canvas: Canvas) {

    }

    private fun drawResultScreen(canvas: Canvas) {
        results?.let { handLandmarkerResult ->

            if(handResult.size > 1) {
                val lines = mutableListOf<Float>()

                linePaint.color = when(handResultType) {
                    HandResult.Emotion -> ContextCompat.getColor(context!!, R.color.hand_result_emotion)
                    HandResult.Brain -> ContextCompat.getColor(context!!, R.color.hand_result_brain)
                    else -> ContextCompat.getColor(context!!, R.color.hand_result_life)
                }

                val scale = 0.9f * width.toFloat() / 1050f

                for (i in 0 until handResult.size - 1) {


                    val startX =
                        handResult[i][0] * imageWidth / 256f * scale
                    val startY =
                        handResult[i][1] * imageHeight / 256f * scale
                    val endX =
                        handResult[i+1][0] * imageWidth / 256f * scale
                    val endY =
                        handResult[i+1][1] * imageHeight / 256f * scale
                    lines.add(startX)
                    lines.add(startY)
                    lines.add(endX)
                    lines.add(endY)
                }

                canvas.drawLines(lines.toFloatArray(), linePaint)

            }

        }
    }

    fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        screenState: MainViewModel.ScreenState
    ) {
        results = handLandmarkerResults

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

    sealed interface HandResult {
        object Emotion : HandResult
        object Brain : HandResult
        object Life : HandResult
    }

}