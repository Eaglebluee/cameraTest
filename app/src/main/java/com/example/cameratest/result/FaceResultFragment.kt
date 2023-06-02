package com.example.cameratest.result

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.cameratest.MainViewModel
import com.example.cameratest.adapter.FaceResultAdapter
import com.example.cameratest.databinding.FragmentFaceResultBinding
import com.example.cameratest.landmarkerhelper.FaceLandmarkerHelper
import com.example.common_base.BaseFragment
import com.example.common_util.removeFragment
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class FaceResultFragment @Inject constructor() : BaseFragment<FragmentFaceResultBinding>(), FaceLandmarkerHelper.LandmarkerListener {

    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentFaceResultBinding.inflate(inflater, container, false)

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val faceResultViewModel by viewModels<FaceResultViewModel>()
    private var faceResultAdapter : FaceResultAdapter? = null
    private lateinit var faceLandmarkerHelper: FaceLandmarkerHelper
    private lateinit var backgroundExecutor: ScheduledExecutorService

    override fun initFragment() {

        observeViewModel()
        initAdapter()
        setClickListeners()
        showResultImage(mainViewModel.photoUri)
        getResultData()
        runDetectionOnImage(mainViewModel.photoUri)
        setPageListener()

    }

    private fun observeViewModel() = with(faceResultViewModel) {

        reportState.onUiState(
            success = {
                Log.d("123123123", "분석 결과 받기 성공!!")
                Log.d("123123123", "결과 : ${it.results.size}")
                faceResultAdapter?.submit(it.results)

            },
            error = {
                Log.d("123123123", "분석 결과 받기 실패... : $it")
            }
        )
    }

    private fun initAdapter() {
        faceResultAdapter = FaceResultAdapter(requireContext(), requestManager)
        binding.resultList.adapter = faceResultAdapter
    }

    private fun setClickListeners() = with(binding) {

        btnHome.setOnClickListener {
            clearFaceView()
            mainViewModel.screenState = MainViewModel.ScreenState.Detect
            requireActivity().removeFragment(this@FaceResultFragment)
            mainViewModel.changeToMain()
        }

        btnPrev.setOnClickListener {
            resultList.currentItem = resultList.currentItem - 1
        }

        btnNext.setOnClickListener {
            resultList.currentItem = resultList.currentItem + 1
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

    private fun showResultImage(uri: Uri) {
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
                binding.imageResultFace.setImageBitmap(bitmap)
            }
    }

    private fun getResultData() {
        faceResultViewModel.getAnalyzeReport(mainViewModel.imgName)
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
                binding.imageResultFace.setImageBitmap(bitmap)

                // Run face landmarker on the input image
                backgroundExecutor.execute {

                    faceLandmarkerHelper =
                        FaceLandmarkerHelper(
                            context = requireContext(),
                            runningMode = RunningMode.IMAGE,
                            minFaceDetectionConfidence = faceResultViewModel.currentMinFaceDetectionConfidence,
                            minFaceTrackingConfidence = faceResultViewModel.currentMinFaceTrackingConfidence,
                            minFacePresenceConfidence = faceResultViewModel.currentMinFacePresenceConfidence,
                            maxNumFaces = faceResultViewModel.currentMaxFaces,
                            currentDelegate = faceResultViewModel.currentDelegate
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
                    } ?: run { Log.e(FaceLandmarkerHelper.TAG, "Error running face landmarker.") }

                    faceLandmarkerHelper.clearFaceLandmarker()
                }
            }
    }

    private fun setPageListener() {
        binding.resultList.registerOnPageChangeCallback(pageChangeCallback)
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            binding.overlay.resultNum = when(position) {
                0 -> 0
                1,2,3 -> 1
                4 -> 2
                5 -> 3
                else -> 4
            }
            binding.overlay.invalidate()

            if(position == 0) binding.btnPrev.visibility = View.GONE
            else binding.btnPrev.visibility = View.VISIBLE

            if(position == 7) binding.btnNext.visibility = View.GONE
            else binding.btnNext.visibility = View.VISIBLE
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