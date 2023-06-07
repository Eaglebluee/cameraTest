package com.example.cameratest.analyze

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
import com.example.cameratest.MainViewModel
import com.example.cameratest.R
import com.example.cameratest.databinding.FragmentHandAnalyzeBinding
import com.example.cameratest.detection.HandFragment
import com.example.cameratest.landmarkerhelper.HandLandmarkerHelper
import com.example.cameratest.result.HandResultFragment
import com.example.common_base.BaseFragment
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class HandAnalyzeFragment @Inject constructor() : BaseFragment<FragmentHandAnalyzeBinding>(), HandLandmarkerHelper.LandmarkerListener {


    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHandAnalyzeBinding.inflate(inflater, container, false)

    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: HandAnalyzeViewModel by viewModels()
    private lateinit var backgroundExecutor: ScheduledExecutorService

    override fun initFragment() {

        clearData()
        observeViewModel()
        setOnClickListeners()
        getResultData()
        runDetectionOnImage(mainViewModel.photoUri)

    }

    private fun clearData() {
        mainViewModel.handCoordList.clear()
        mainViewModel.handResultDesc.clear()
    }

    private fun observeViewModel() = with(viewModel) {

        reportState.onUiState(
            success = {
                Log.d("123123123", "분석 결과 받기 성공!!")
                Log.d("123123123", "결과 : ${it}")


                mainViewModel.handResultDesc.add(Pair(it.feelTitle, it.feelContent))
                mainViewModel.handResultDesc.add(Pair(it.brainTitle, it.brainContent))
                mainViewModel.handResultDesc.add(Pair(it.lifeTitle, it.lifeContent))

                mainViewModel.handCoordList.add(it.feelCoordinate)
                mainViewModel.handCoordList.add(it.brainCoordinate)
                mainViewModel.handCoordList.add(it.lifeCoordinate)

                // 초기값 세팅
                binding.overlay.handAnalyzeResult = mainViewModel.handCoordList
                binding.overlay.invalidate()

            },
            error = {
                Log.d("123123123", "분석 결과 받기 실패... : $it")
            },
            finish = {
                binding.loadingBar.visibility = View.GONE
                binding.txtAnalyze.text = "분석 완료!!!"
                binding.btnResult.isEnabled = true
            }
        )
    }

    private fun setOnClickListeners() = with(binding) {

        btnRecapture.setOnClickListener {
            clearHandView()
            mainViewModel.screenState = MainViewModel.ScreenState.Detect
            replaceFragment(R.id.fragmentContainer, HandFragment(), parentFragmentManager)
        }

        btnResult.setOnClickListener {
            clearHandView()
            mainViewModel.screenState = MainViewModel.ScreenState.Result
            replaceFragment(R.id.fragmentContainer, HandResultFragment(), parentFragmentManager)
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
                                mainViewModel.screenState,
                                mainViewModel.isLeft
                            )

                        }
                    } ?: run { Log.e(HandLandmarkerHelper.TAG, "Error running hand landmarker.") }

                    handLandmarkerHelper.clearHandLandmarker()
                }
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