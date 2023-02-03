package com.afjltd.tracking.view.fragment.route

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.afjltd.tracking.model.requests.LocationApiRequest
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.model.responses.VehicleMenu
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.adapter.ClickListenerInterface
import com.afjltd.tracking.view.adapter.RouteListAdapter
import com.afjltd.tracking.view.fragment.auth.CustomAuthenticationView
import com.afjltd.tracking.view.fragment.route.viewmodel.RouteViewModel
import com.afjltd.tracking.databinding.FragmentWeeklyInspectionListBinding
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


class RouteFragment : Fragment() {

    private var _binding: FragmentWeeklyInspectionListBinding? = null
    private val binding get() = _binding!!

    lateinit var routeViewModel: RouteViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity


    var identifierForm = "no_argument"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    companion object {
        const val FORM_IDENTIFIER_ARGUMENT = "form_identifier"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        routeViewModel = ViewModelProvider(this)[RouteViewModel::class.java]
        _binding = FragmentWeeklyInspectionListBinding.inflate(inflater, container, false)


        val menuObject = requireArguments().getSerializable(FORM_IDENTIFIER_ARGUMENT) as VehicleMenu
        identifierForm = menuObject.identifier.toString()

        val root: View = binding.root



        routeViewModel.showDialog.observe(mBaseActivity) {
            mBaseActivity.showProgressDialog(it)
        }

        binding.baseLayout.visibility = View.GONE
        binding.btnAddInspection.visibility = View.GONE
        binding.txtInspectionTitle.visibility = View.GONE



        mBaseActivity.supportActionBar?.title = menuObject.name

        if (menuObject.qrStatus == true) {
            val authView = CustomAuthenticationView(requireContext())
            binding.root.addView(authView)
            authView.addAuthListener(object : CustomAuthenticationView.AuthListeners {
                override fun onAuthCompletionListener(boolean: Boolean) {
                    if (_binding == null)
                        return
                    if (boolean) {
                        binding.root.removeAllViews()
                        binding.root.addView(binding.baseLayout)
                        binding.baseLayout.visibility = View.VISIBLE
                        routeViewModel.getRouteList(mBaseActivity)
                    } else {
                        binding.root.removeAllViews()
                        binding.root.addView(authView)

                    }
                }

                override fun onAuthForceClose(boolean: Boolean) {
                    mBaseActivity.pressBackButton()
                }
            })

        } else {
            binding.root.removeAllViews()
            binding.root.addView(binding.baseLayout)
            binding.baseLayout.visibility = View.VISIBLE
            routeViewModel.getRouteList(mBaseActivity)
        }




        routeViewModel.getRouteList.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    showRouteList(it)
                } catch (e: Exception) {
                    AFJUtils.writeLogs("route sheet exception")
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                routeViewModel._routeList.value = null
            }
        }


        // Add observer for score
        routeViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {

                mBaseActivity.toast(it, true)
                mBaseActivity.showProgressDialog(false)
                binding.recWeeklyInspectionList.visibility = View.GONE
                binding.txtNoData.visibility = View.VISIBLE
                binding.txtNoData.text = it.toString()
                routeViewModel.errorsMsg.value = null

                AFJUtils.writeLogs("route error  $it")
            }
        })





        return root
    }


    private fun showRouteList(listRoutes: List<Sheets>) {

        binding.baseLayout.visibility = View.VISIBLE
        binding.txtNoData.visibility = View.GONE


        val adapter = RouteListAdapter(mBaseActivity)
        adapter.setPageSize(listRoutes.size)
        adapter.setDefaultRecyclerView(mBaseActivity, binding.recWeeklyInspectionList)
        adapter.submitItems(listRoutes)
        adapter.setListnerClick(object : ClickListenerInterface {
            @SuppressLint("SimpleDateFormat")
            override fun <T> handleContinueButtonClick(data: T) {
                val item = data as Sheets
                if (item.pick == true) {
                    item.action = "picked"
                } else {
                    item.action = "dropped"
                }

                val formatter: Format = SimpleDateFormat("H:mm")
                item.time = formatter.format(Date())


                val locPref = AFJUtils.getObjectPref(
                    mBaseActivity,
                    AFJUtils.KEY_LOCATION_REQUEST_OBJECT,
                    LocationApiRequest::class.java
                )


                val request = LoginRequest(
                    routeSheet = item,
                    deviceDetail = AFJUtils.getDeviceDetail(),
                    latitude = locPref.latitude ?:"0",
                    longitude = locPref.longitude ?:"0"
                )

                routeViewModel.updateRouteListStatus(mBaseActivity, request) {
                    val data = it as String

                    if (data.equals("Success")) {
                        routeViewModel.getRouteList(mBaseActivity)
                    } else {
                        mBaseActivity.writeExceptionLogs(data)
                    }
                }
            }
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null


    }


}

data class Person(var name: String, var tutorial: String)
