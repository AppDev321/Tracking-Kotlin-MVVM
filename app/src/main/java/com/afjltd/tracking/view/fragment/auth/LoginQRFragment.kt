package com.afjltd.tracking.view.fragment.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.databinding.FragmentFormsBinding


class LoginQRFragment : Fragment() {

    private var _binding: FragmentFormsBinding? = null
    private val binding get() = _binding!!
    private lateinit var mBaseActivity: NavigationDrawerActivity



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFormsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mBaseActivity.supportActionBar?.title = "Login"
        binding.baseLayout.visibility = View.GONE
            val authView = CustomAuthenticationView(requireContext())
            binding.root.addView(authView)
            authView.addAuthListener(object : CustomAuthenticationView.AuthListeners {
                override fun onAuthCompletionListener(boolean: Boolean) {
                    if (_binding == null)
                        return
                    if (boolean) {
                        binding.root.removeAllViews()
                        binding.root.addView(binding.baseLayout)
                        mBaseActivity.pressBackButton()
                    } else {
                        binding.root.removeAllViews()
                        binding.root.addView(authView)
                    }
                }
                override fun onAuthForceClose(boolean: Boolean) {
                    mBaseActivity.pressBackButton()
                }
            })
        return root
    }



}

