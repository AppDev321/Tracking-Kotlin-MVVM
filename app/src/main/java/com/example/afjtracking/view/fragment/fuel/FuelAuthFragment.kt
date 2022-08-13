package com.example.afjtracking.view.fragment.fuel

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.databinding.FragmentFuelLoginBinding
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.fuel.viewmodel.FuelViewModel
import java.util.*


class FuelAuthFragment : Fragment() {

    private var _binding: FragmentFuelLoginBinding? = null
    private val binding get() = _binding!!


    private val TAG = FuelAuthFragment::class.java.simpleName


    private var _fuelViewModel: FuelViewModel? = null
    private val fuelViewModel get() = _fuelViewModel!!

    private lateinit var mBaseActivity: NavigationDrawerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fuelViewModel = ViewModelProvider(this).get(FuelViewModel::class.java)

        _binding = FragmentFuelLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.fuelViewModel = fuelViewModel


        fuelViewModel.user.observe(mBaseActivity) { loginUser ->
            if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strEmailAddress)) {
                binding.txtEmailAddress.error = "Enter an E-Mail Address"
                binding.txtEmailAddress.requestFocus()
            } else if (!loginUser!!.isEmailValid) {
                binding.txtEmailAddress.error = "Enter a Valid E-mail Address"
                binding.txtEmailAddress.requestFocus()
            } else if (TextUtils.isEmpty(Objects.requireNonNull(loginUser).strPassword)) {
                binding.txtPassword.error = "Enter a Password"
                binding.txtPassword.requestFocus()
            } else if (!loginUser.isPasswordLengthGreaterThan5) {
                binding.txtPassword.error = "Enter at least 6 Digit password"
                binding.txtPassword.requestFocus()
            } else {
                mBaseActivity.showProgressDialog(true)
            //    fuelViewModel.loginApiRequest(loginUser, this@LoginActivity)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}