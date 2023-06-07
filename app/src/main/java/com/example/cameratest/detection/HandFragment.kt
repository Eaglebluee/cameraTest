package com.example.cameratest.detection

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.example.cameratest.analyze.HandAnalyzeFragment
import com.example.cameratest.databinding.FragmentHandBinding
import com.example.cameratest.landmarkerhelper.HandLandmarkerHelper
import com.example.cameratest.overlay.HandOverlayView
import com.example.common_base.BaseFragment
import com.example.common_util.FileUtil
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class HandFragment : BaseFragment<FragmentHandBinding>(), HandLandmarkerHelper.LandmarkerListener {

    private val TAG = "Hand Landmarker"
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel by viewModels<HandViewModel>()
    private var preview: Preview? = null
    private var imageAnalyzer : ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK
    private lateinit var analyzeFileName: String

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService

    override fun createFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHandBinding.inflate(inflater, container, false)

    override fun initFragment() {

        clearData()

        observeViewModel()

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Create the FaceDetectionHelper that will handle the inference
        backgroundExecutor.execute {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                maxNumHands = viewModel.currentMaxHands,
                currentDelegate = viewModel.currentDelegate,
                handLandmarkerHelperListener = this
            )

            // Wait for the views to be properly laid out
            binding.viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
        }

        initScreenCapture()

        setRadioBtn()

        removeGuide()

    }

    private fun initScreenCapture() {

        binding.overlay.isInGuideLine.observe(viewLifecycleOwner) { isInGuideLine ->
            binding.capture.visibility = if (isInGuideLine) View.VISIBLE else View.GONE
        }

        binding.capture.setOnClickListener {
            handLandmarkerHelper.captureHand {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    captureScreenHigherVersion(it)
                }else {
                    captureScreen(it)
                }
                uploadImage(it)
            }
        }

    }

    private fun captureScreenHigherVersion(bitmap: Bitmap) {
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }

        val resolver: ContentResolver = requireContext().contentResolver
        var uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        uri = resolver.insert(uri, values) ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val outputStream = resolver.openOutputStream(uri)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream?.flush()
                outputStream?.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    resolver.update(uri, values, null, null)
                }

                withContext(Dispatchers.Main) {
                    // 이미지 저장이 완료되었음을 알리는 메시지
                    Toast.makeText(context, "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    private fun captureScreen(bitmap: Bitmap) {
        // 파일 저장
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val file = File(Environment.getExternalStorageDirectory(), filename)

        try {
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()

            scanMedia(file)
            Toast.makeText(requireContext(), "저장 완료! ${file.absolutePath}", Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "저장 실패! ${e.printStackTrace()}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun scanMedia(file: File) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        requireContext().sendBroadcast(mediaScanIntent)
    }

    private fun uploadImage(bitmap: Bitmap) {
        val transferUri = bitmapToUri(bitmap)
        mainViewModel.photoUri = transferUri
        val file = FileUtil.makeFile(getCacheRootPath(), transferUri)

        FileUtil.writeBitmapToFile(bitmap, Bitmap.CompressFormat.JPEG, 80, file.path)

         viewModel.uploadImage(makeRequestBody(file))
    }

    private fun makeRequestBody(file: File): MultipartBody.Part =
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "imagefiles",
                URLEncoder.encode(file.name, "utf-8"),
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()
            .part(0)

    private fun bitmapToUri(bitmap: Bitmap): Uri {
        val timeStamp = SimpleDateFormat("yyyy/MM/dd").format(Date())
        val fileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val folderPath = getCurrentTime()
        val imageFileName = "yeo_photo/".plus(timeStamp).plus("/${folderPath}/").plus(fileName).plus(".jpg")
        analyzeFileName = "/".plus(timeStamp).plus("/1/1_").plus(timeStamp).plus(".jpg")
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            imageFileName,
            null
        )
        return Uri.parse(path)
    }

    private fun getCurrentTime(): String {
        val currentTime = Calendar.getInstance().time
        val format = SimpleDateFormat("a", Locale.getDefault())
        val formatTime = SimpleDateFormat("hh", Locale.getDefault())
        val amPm = format.format(currentTime)

        return when {
            amPm == "오전" && formatTime.format(Date()).toInt() < 10 -> "0"
            amPm == "오후" && formatTime.format(Date()).toInt() > 6 -> "2"
            else -> "1"
        }
    }

    private fun getCacheRootPath(): String {
        val cacheRootDir: File =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
                File(requireContext().externalCacheDir, "tempImg")
            else File(requireContext().cacheDir, "tempImg")

        if (!cacheRootDir.exists()) {
            cacheRootDir.mkdirs()
        } // 경로가 존재하지 않으면 경로를 만듭니다.

        return cacheRootDir.path
    }

    private fun clearData() {
        mainViewModel.isLeft = true
    }

    private fun observeViewModel() {

        viewModel.uploadImageState.onUiState(
            success = {
                Log.d("123123123", "사진 업로드 성공!!!: ${it.files[0].imginfo}")
                val timeStamp = SimpleDateFormat("yyyy/MM/dd").format(Date())
                val folderPath = getCurrentTime()
                val imgName = "/".plus(timeStamp).plus("/${folderPath}/")
                    .plus(it.files[0].imginfo.replace("|480|640", ""))
                Log.d("123123123", "바뀐 폴더 이름!!!: ${imgName}")

                mainViewModel.imgName = imgName

                clearHandView()

                mainViewModel.screenState = MainViewModel.ScreenState.Analyze
                replaceFragment(R.id.fragmentContainer, HandAnalyzeFragment(), parentFragmentManager)

            },
            error = {
                Log.d("123123123", "사진 업로드 실패... : $it")
                it.printStackTrace()
            },
            finish = {
                Log.d("123123123", "사진 업로드 완료!!!")
            }
        )

    }

    private fun clearHandView() {
        binding.overlay.clear()
        // Shut down our background executor.
        backgroundExecutor.execute { handLandmarkerHelper.clearHandLandmarker() }
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE,
            TimeUnit.NANOSECONDS
        )
        preview = null
        imageAnalyzer = null
        camera = null
        cameraProvider = null
    }

    private fun setRadioBtn() = with(binding) {

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.leftBtn -> {
                    binding.guideHand.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.hello_left))
                    binding.overlay.isLeft = true
                    mainViewModel.isLeft = true
                }
                R.id.rightBtn -> {
                    binding.guideHand.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.hello_right))
                    binding.overlay.isLeft = false
                    mainViewModel.isLeft = false
                }
            }
        }

    }

    private fun removeGuide() {

        lifecycleScope.launch {
            delay(3000)
            binding.guideHand.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        backgroundExecutor.execute {
            if(handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
        }
    }

    // Initialize CameraX, and prepare to bind the camera use cases
    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    // Declare and bind preview, capture and analysis use cases
    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectHand(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectHand(imageProxy: ImageProxy) {
        handLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }



    override fun onPause() {
        super.onPause()

        if(this::handLandmarkerHelper.isInitialized && !backgroundExecutor.isShutdown) {
            viewModel.setMaxHands(handLandmarkerHelper.maxNumHands)
            viewModel.setMinHandDetectionConfidence(handLandmarkerHelper.minHandDetectionConfidence)
            viewModel.setMinHandTrackingConfidence(handLandmarkerHelper.minHandTrackingConfidence)
            viewModel.setMinHandPresenceConfidence(handLandmarkerHelper.minHandPresenceConfidence)
            viewModel.setDelegate(handLandmarkerHelper.currentDelegate)

            // Close the HandLandmarkerHelper and release resources
            backgroundExecutor.execute { handLandmarkerHelper.clearHandLandmarker() }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {

        // Pass necessary information to OverlayView for drawing on the canvas
        binding.overlay.setResults(
            resultBundle.results.first(),
            resultBundle.inputImageHeight,
            resultBundle.inputImageWidth,
            RunningMode.LIVE_STREAM,
            mainViewModel.screenState,
            mainViewModel.isLeft
        )

        // Force a redraw
        binding.overlay.invalidate()


    }


}