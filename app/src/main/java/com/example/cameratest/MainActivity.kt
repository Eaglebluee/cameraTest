package com.example.cameratest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameratest.databinding.ActivityMainBinding
import com.example.cameratest.facedetect.FaceFragment
import com.example.cameratest.permission.PermissionsFragment
import com.example.common_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val PERMISSIONS_REQUEST_CODE = 123

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )

    private val viewModel by viewModels<MainViewModel>()
    private var faceFragment: FaceFragment? = null
    private var permissionFragment: PermissionsFragment? = null

    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initActivity() {

        observeViewModel()
        setClickListeners()

    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
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
            faceFragment = FaceFragment()
            addFragment(R.id.fragmentContainer, faceFragment)
        }

        btnHand.setOnClickListener {

        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in requiredPermissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 거부되었을 경우 처리할 로직
                    finish()
                    return
                }
            }

            // 모든 권한이 허용되었을 경우 처리할 로직
        }
    }


}