package com.example.cameratest.result

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.cameratest.MainViewModel
import com.example.cameratest.adapter.ResultAdapter
import com.example.cameratest.databinding.FragmentResultBinding
import com.example.common_base.BaseFragment
import com.example.common_util.removeFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ResultFragment @Inject constructor() : BaseFragment<FragmentResultBinding>() {

    override fun createFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentResultBinding.inflate(inflater, container, false)

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val resultViewModel by viewModels<ResultViewModel>()
    private var resultAdapter : ResultAdapter? = null

    override fun initFragment() {

        observeViewModel()
        initAdapter()
        setClickListeners()
        showResultImage(mainViewModel.photoUri)
        getResultData()

    }

    private fun observeViewModel() = with(resultViewModel) {

        reportState.onUiState(
            success = {
                Log.d("123123123", "분석 결과 받기 성공!!")
                Log.d("123123123", "결과 : ${it.results.size}")
                resultAdapter?.submit(it.results)

            },
            error = {
                Log.d("123123123", "분석 결과 받기 실패... : $it")
            }
        )
    }

    private fun initAdapter() {
        resultAdapter = ResultAdapter(requireContext(), requestManager)
        binding.resultList.adapter = resultAdapter
    }

    private fun setClickListeners() = with(binding) {

        btnHome.setOnClickListener {
            requireActivity().removeFragment(this@ResultFragment)
            mainViewModel.changeToMain()
        }

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
        resultViewModel.getAnalyzeReport(mainViewModel.imgName)
    }



}