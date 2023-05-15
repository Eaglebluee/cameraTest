package com.example.cameratest.facedetect

/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.cameratest.FaceLandmarkerHelper
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.example.cameratest.analyze.AnalyzeFragment
import com.example.cameratest.databinding.FragmentFaceBinding
import com.example.common_base.BaseFragment
import com.example.common_util.FileUtil
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FaceFragment @Inject constructor() : BaseFragment<FragmentFaceBinding>(),
    FaceLandmarkerHelper.LandmarkerListener {

    companion object {
        const val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        private const val TAG = "Face Landmarker"
    }

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private val mainViewModel: MainViewModel by activityViewModels()
    private val faceViewModel by viewModels<FaceViewModel>()
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_FRONT
    private lateinit var analyzeFileName: String

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ExecutorService


    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFaceBinding.inflate(inflater, container, false)

    override fun initFragment() {

        // Initialize our background executor
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Create the FaceDetectionHelper that will handle the inference
        backgroundExecutor.execute {
            faceLandmarkerHelper = FaceLandmarkerHelper(
                context = requireContext(),
                runningMode = RunningMode.LIVE_STREAM,
                minFaceDetectionConfidence = mainViewModel.currentMinFaceDetectionConfidence,
                minFaceTrackingConfidence = mainViewModel.currentMinFaceTrackingConfidence,
                minFacePresenceConfidence = mainViewModel.currentMinFacePresenceConfidence,
                maxNumFaces = mainViewModel.currentMaxFaces,
                currentDelegate = mainViewModel.currentDelegate,
                faceLandmarkerHelperListener = this
            )

            // Wait for the views to be properly laid out
            binding.viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
        }

        initScreenCapture()

        observeViewModel()

    }

    override fun onResume() {
        super.onResume()

        backgroundExecutor.execute {
            if (faceLandmarkerHelper.isClosed()) {
                faceLandmarkerHelper.setupFaceLandmarker()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if(this::faceLandmarkerHelper.isInitialized) {
            mainViewModel.setMaxFaces(faceLandmarkerHelper.maxNumFaces)
            mainViewModel.setMinFaceDetectionConfidence(faceLandmarkerHelper.minFaceDetectionConfidence)
            mainViewModel.setMinFaceTrackingConfidence(faceLandmarkerHelper.minFaceTrackingConfidence)
            mainViewModel.setMinFacePresenceConfidence(faceLandmarkerHelper.minFacePresenceConfidence)
            mainViewModel.setDelegate(faceLandmarkerHelper.currentDelegate)

            // Close the FaceLandmarkerHelper and release resources
            backgroundExecutor.execute { faceLandmarkerHelper.clearFaceLandmarker() }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Shut down our background executor.
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE,
            TimeUnit.NANOSECONDS
        )
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
                        detectFace(image)
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

    private fun detectFace(imageProxy: ImageProxy) {
        faceLandmarkerHelper.detectLiveStream(
            imageProxy = imageProxy,
            isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
        )
    }

    private fun initScreenCapture() {

        binding.overlay.isInGuideLine.observe(viewLifecycleOwner) { isInGuideLine ->
            binding.capture.visibility = if (isInGuideLine) View.VISIBLE else View.GONE
        }

        binding.capture.setOnClickListener {
            faceLandmarkerHelper.captureFace {
                captureScreen(it)
                uploadImage(it)
            }
        }

    }

    private fun captureScreen(bitmap: Bitmap) {
        verifyStoragePermissions()
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


    private fun verifyStoragePermissions() {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                requireActivity(),
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
            )
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

        faceViewModel.uploadImage(makeRequestBody(file))
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
        val imageFileName = "yeo_photo/".plus(timeStamp).plus("/1/").plus(fileName).plus(".jpg")
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

    private fun observeViewModel() {

        faceViewModel.uploadImageState.onUiState(
            success = {
                Log.d("123123123", "사진 업로드 성공!!!: ${it.files[0].imginfo}")
                val timeStamp = SimpleDateFormat("yyyy/MM/dd").format(Date())
                val imgName = "/".plus(timeStamp).plus("/1/")
                    .plus(it.files[0].imginfo.replace("|480|640", ""))
                Log.d("123123123", "바뀐 폴더 이름!!!: ${imgName}")

                mainViewModel.imgName = imgName

                mainViewModel.screenState = MainViewModel.ScreenState.Analyze
                replaceFragment(R.id.fragmentContainer, AnalyzeFragment(), parentFragmentManager)

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


    // Update UI after face have been detected. Extracts original
    // image height/width to scale and place the landmarks properly through
    // OverlayView
    override fun onResults(
        resultBundle: FaceLandmarkerHelper.ResultBundle
    ) {
        if(mainViewModel.screenState is MainViewModel.ScreenState.Detect) {
            activity?.runOnUiThread {

                // Pass necessary information to OverlayView for drawing on the canvas
                binding.overlay.setResults(
                    resultBundle.result,
                    resultBundle.inputImageHeight,
                    resultBundle.inputImageWidth,
                    RunningMode.LIVE_STREAM,
                    mainViewModel.screenState
                )

                // Force a redraw
                binding.overlay.invalidate()

            }
        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }


}