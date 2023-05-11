package com.example.cameratest

import android.view.View
import com.example.cameratest.databinding.ActivityMainBinding
import com.example.cameratest.fragment.FaceFragment
import com.example.cameratest.fragment.PermissionsFragment
import com.example.common_base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var faceFragment: FaceFragment? = null
    private var permissionFragment : PermissionsFragment? = null

    override fun createBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun initActivity() {

        setClickListeners()

    }

    private fun setClickListeners() = with(binding) {

        btnFace.setOnClickListener {
            btnFace.visibility = View.GONE
            btnHand.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
            if(PermissionsFragment.hasPermissions(this@MainActivity)) {
                faceFragment = FaceFragment()
                addFragment(R.id.fragmentContainer, faceFragment)
            }else {
                permissionFragment = PermissionsFragment()
                addFragment(R.id.fragmentContainer, permissionFragment)
            }
        }

        btnHand.setOnClickListener {

        }

    }


}