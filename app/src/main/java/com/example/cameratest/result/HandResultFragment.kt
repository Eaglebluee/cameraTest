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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.cameratest.MainViewModel
import com.example.cameratest.adapter.HandResultAdapter
import com.example.cameratest.databinding.FragmentHandResultBinding
import com.example.cameratest.landmarkerhelper.HandLandmarkerHelper
import com.example.cameratest.overlay.HandOverlayView
import com.example.common_base.BaseFragment
import com.example.common_util.removeFragment
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HandResultFragment @Inject constructor() : BaseFragment<FragmentHandResultBinding>(), HandLandmarkerHelper.LandmarkerListener {

    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHandResultBinding.inflate(inflater, container, false)

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val viewModel by viewModels<HandResultViewModel>()
    private var handResultAdapter : HandResultAdapter? = null
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var backgroundExecutor: ScheduledExecutorService
    private val handCoordList = mutableListOf<List<List<Int>>>()

    override fun initFragment() {
        observeViewModel()
        initAdapter()
        setClickListeners()
        showResultImage(mainViewModel.photoUri)
        getResultData()
        runDetectionOnImage(mainViewModel.photoUri)
        setPageListener()
    }

    private fun observeViewModel() = with(viewModel) {

        reportState.onUiState(
            success = {
                Log.d("123123123", "분석 결과 받기 성공!!")
                Log.d("123123123", "결과 : ${it}")

                val handResultList = mutableListOf<Pair<String, String>>()
                handResultList.add(Pair(it.feelTitle, it.feelContent))
                handResultList.add(Pair(it.brainTitle, it.brainContent))
                handResultList.add(Pair(it.lifeTitle, it.lifeContent))
                handResultAdapter?.submit(handResultList)

                handCoordList.add(it.feelCoordinate)
                handCoordList.add(it.brainCoordinate)
                handCoordList.add(it.lifeCoordinate)

                // 초기값 세팅
                binding.overlay.handResult = it.feelCoordinate
                binding.overlay.invalidate()

            },
            error = {
                Log.d("123123123", "분석 결과 받기 실패... : $it")
            }
        )
    }

    private fun initAdapter() {
        handResultAdapter = HandResultAdapter(requireContext(), requestManager)
        binding.resultList.adapter = handResultAdapter
    }

    private fun setClickListeners() = with(binding) {

        btnHome.setOnClickListener {
            clearHandView()
            mainViewModel.screenState = MainViewModel.ScreenState.Detect
            requireActivity().removeFragment(this@HandResultFragment)
            mainViewModel.changeToMain()
        }

        btnPrev.setOnClickListener {
            resultList.currentItem = resultList.currentItem - 1
        }

        btnNext.setOnClickListener {
            resultList.currentItem = resultList.currentItem + 1
        }

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
                binding.imageResult.setImageBitmap(bitmap)
            }
    }

    private fun getResultData() {
        viewModel.getAnalyzeReport(mainViewModel.imgName)
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

                // Run hand landmarker on the input image
                backgroundExecutor.execute {

                    handLandmarkerHelper = HandLandmarkerHelper(
                        context = requireContext(),
                        runningMode = RunningMode.IMAGE,
                        minHandDetectionConfidence = viewModel.currentMinHandDetectionConfidence,
                        minHandTrackingConfidence = viewModel.currentMinHandTrackingConfidence,
                        minHandPresenceConfidence = viewModel.currentMinHandPresenceConfidence,
                        maxNumHands = viewModel.currentMaxHands,
                        currentDelegate = viewModel.currentDelegate,
                        handLandmarkerHelperListener = this
                    )

                    handLandmarkerHelper.detectImage(bitmap)?.let { result ->
                        activity?.runOnUiThread {
                            binding.overlay.setResults(
                                result.results[0],
                                bitmap.height,
                                bitmap.width,
                                RunningMode.IMAGE,
                                mainViewModel.screenState
                            )

                        }
                    } ?: run { Log.e(HandLandmarkerHelper.TAG, "Error running hand landmarker.") }

                    handLandmarkerHelper.clearHandLandmarker()
                }
            }
    }

    private fun setPageListener() {
        binding.resultList.registerOnPageChangeCallback(pageChangeCallback)
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            if(handCoordList.isNotEmpty()) {
                binding.overlay.handResult = handCoordList[position]
                binding.overlay.invalidate()
                binding.overlay.handResultType = when(position) {
                    0 -> HandOverlayView.HandResult.Emotion
                    1 -> HandOverlayView.HandResult.Brain
                    else -> HandOverlayView.HandResult.Life
                }
            }

            if(position == 0) binding.btnPrev.visibility = View.GONE
            else binding.btnPrev.visibility = View.VISIBLE

            if(position == 2) binding.btnNext.visibility = View.GONE
            else binding.btnNext.visibility = View.VISIBLE

        }
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
    }

}