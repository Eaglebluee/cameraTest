package com.example.cameratest.analyze

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper.Companion.TAG
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.example.cameratest.databinding.FragmentAnalyzeBinding
import com.example.cameratest.detection.FaceFragment
import com.example.cameratest.result.FaceResultFragment
import com.example.common_base.BaseFragment
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FaceAnalyzeFragment @Inject constructor() : BaseFragment<FragmentAnalyzeBinding>(), FaceLandmarkerHelper.LandmarkerListener {

    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAnalyzeBinding.inflate(inflater, container, false)

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: FaceAnalyzeViewModel by viewModels()

    private lateinit var backgroundExecutor: ScheduledExecutorService

    override fun initFragment() {

        setOnClickListeners()
        runDetectionOnImage(mainViewModel.photoUri)

    }

    private fun setOnClickListeners() = with(binding) {

        btnRecapture.setOnClickListener {
            clearFaceView()
            mainViewModel.screenState = MainViewModel.ScreenState.Detect
            replaceFragment(R.id.fragmentContainer, FaceFragment(), parentFragmentManager)
        }

        btnResult.setOnClickListener {
            clearFaceView()
            mainViewModel.screenState = MainViewModel.ScreenState.Result
            replaceFragment(R.id.fragmentContainer, FaceResultFragment(), parentFragmentManager)
        }

    }

    private fun clearFaceView() {
        binding.overlay.clear()
        // Shut down our background executor.
        backgroundExecutor.execute { faceLandmarkerHelper.clearFaceLandmarker() }
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE,
            TimeUnit.NANOSECONDS
        )
    }

    private fun runDetectionOnImage(uri: Uri) {
        backgroundExecutor = Executors.newSingleThreadScheduledExecutor()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                requireActivity().contentResolver,
                uri
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                uri
            )
        }
            .copy(Bitmap.Config.ARGB_8888, true)
            ?.let { bitmap ->
                binding.imageResult.setImageBitmap(bitmap)

                // Run face landmarker on the input image
                backgroundExecutor.execute {

                    faceLandmarkerHelper =
                        FaceLandmarkerHelper(
                            context = requireContext(),
                            runningMode = RunningMode.IMAGE,
                            minFaceDetectionConfidence = viewModel.currentMinFaceDetectionConfidence,
                            minFaceTrackingConfidence = viewModel.currentMinFaceTrackingConfidence,
                            minFacePresenceConfidence = viewModel.currentMinFacePresenceConfidence,
                            maxNumFaces = viewModel.currentMaxFaces,
                            currentDelegate = viewModel.currentDelegate
                        )

                    faceLandmarkerHelper.detectImage(bitmap)?.let { result ->
                        activity?.runOnUiThread {
                            binding.overlay.setResults(
                                result.result,
                                bitmap.height,
                                bitmap.width,
                                RunningMode.IMAGE,
                                mainViewModel.screenState
                            )

                        }
                    } ?: run { Log.e(TAG, "Error running face landmarker.") }

                    faceLandmarkerHelper.clearFaceLandmarker()
                }
            }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {

    }

}