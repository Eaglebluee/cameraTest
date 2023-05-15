package com.example.cameratest

import android.view.View
import androidx.activity.viewModels
import com.example.cameratest.databinding.ActivityMainBinding
import com.example.cameratest.facedetect.FaceFragment
import com.example.cameratest.permission.PermissionsFragment
import com.example.common_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel by viewModels<MainViewModel>()
    private var faceFragment: FaceFragment? = null
    private var permissionFragment: PermissionsFragment? = null

    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initActivity() {

        observeViewModel()
        setClickListeners()

    }

    private fun observeViewModel() {

        viewModel.toMainScreen.onResult {
            binding.btnFace.visibility = View.VISIBLE
            binding.btnHand.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
        }

    }

    private fun setClickListeners() = with(binding) {

        btnFace.setOnClickListener {
            btnFace.visibility = View.GONE
            btnHand.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            if (PermissionsFragment.hasPermissions(this@MainActivity)) {
                faceFragment = FaceFragment()
                addFragment(R.id.fragmentContainer, faceFragment)
            } else {
                permissionFragment = PermissionsFragment()
                addFragment(R.id.fragmentContainer, permissionFragment)
            }
        }

        btnHand.setOnClickListener {

        }

    }

}