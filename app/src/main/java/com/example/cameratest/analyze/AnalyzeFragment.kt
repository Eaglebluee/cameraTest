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
import com.example.cameratest.FaceLandmarkerHelper
import com.example.cameratest.FaceLandmarkerHelper.Companion.TAG
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.example.cameratest.databinding.FragmentAnalyzeBinding
import com.example.cameratest.result.ResultFragment
import com.example.common_base.BaseFragment
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Inject

@AndroidEntryPoint
class AnalyzeFragment @Inject constructor() : BaseFragment<FragmentAnalyzeBinding>(), FaceLandmarkerHelper.LandmarkerListener {

    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAnalyzeBinding.inflate(inflater, container, false)

    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var backgroundExecutor: ScheduledExecutorService

    override fun initFragment() {

        setOnClickListeners()
        runDetectionOnImage(mainViewModel.photoUri)

    }

    private fun setOnClickListeners() = with(binding) {

        btnRecapture.setOnClickListener {

        }

        btnResult.setOnClickListener {
            mainViewModel.screenState = MainViewModel.ScreenState.Result
            replaceFragment(R.id.fragmentContainer, ResultFragment(), parentFragmentManager)
        }

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
                            minFaceDetectionConfidence = mainViewModel.currentMinFaceDetectionConfidence,
                            minFaceTrackingConfidence = mainViewModel.currentMinFaceTrackingConfidence,
                            minFacePresenceConfidence = mainViewModel.currentMinFacePresenceConfidence,
                            maxNumFaces = mainViewModel.currentMaxFaces,
                            currentDelegate = mainViewModel.currentDelegate
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