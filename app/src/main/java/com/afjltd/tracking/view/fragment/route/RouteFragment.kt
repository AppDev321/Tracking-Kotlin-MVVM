package com.afjltd.tracking.view.fragment.route

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.afjltd.tracking.databinding.FragmentWeeklyInspectionListBinding
import com.afjltd.tracking.model.requests.DistanceRequest
import com.afjltd.tracking.model.requests.LocationData
import com.afjltd.tracking.model.requests.LoginRequest
import com.afjltd.tracking.model.responses.Calculation
import com.afjltd.tracking.model.responses.DistanceResponse
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.model.responses.VehicleMenu
import com.afjltd.tracking.service.location.LocationRepository
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.CustomDialog
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.adapter.ClickListenerInterface
import com.afjltd.tracking.view.adapter.RouteListAdapter
import com.afjltd.tracking.view.fragment.auth.CustomAuthenticationView
import com.afjltd.tracking.view.fragment.route.viewmodel.RouteViewModel
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


class RouteFragment : Fragment() {

    private var _binding: FragmentWeeklyInspectionListBinding? = null
    private val binding get() = _binding!!

    lateinit var routeViewModel: RouteViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity

    private var latitude = "0.0"
    private var longitude = "0.0"
    private var minLocationDistance = 100
    var identifierForm = "no_argument"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    companion object {
        const val FORM_IDENTIFIER_ARGUMENT = "form_identifier"
    }

    private fun getUpdateLocationData(context: Context) {
        val repository = LocationRepository(context)
        lifecycleScope.launch {
            repository.getLocations()
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collectLatest {
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                }
        }
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

        getUpdateLocationData(mBaseActivity)

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


        lifecycleScope.launch{
            routeViewModel.getRouteList.collectLatest {
                showRouteList(it)

            }
            routeViewModel.errorsMsg.collectLatest {
                    mBaseActivity.toast(it, true)
                    mBaseActivity.showProgressDialog(false)
                    binding.recWeeklyInspectionList.visibility = View.GONE
                    binding.txtNoData.visibility = View.VISIBLE
                    binding.txtNoData.text = it
                    AFJUtils.writeLogs("route error  $it")

            }
        }




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


                val request = LoginRequest(
                    routeSheet = item,
                    deviceDetail = AFJUtils.getDeviceDetail(),
                    latitude = latitude,
                    longitude = longitude
                )


                val childrenLoc = LocationData()
                childrenLoc.latitude = item.latitude ?: 0.0
                childrenLoc.longitude = item.longitude ?: 0.0

                val vehicleLocation = LocationData()
                vehicleLocation.latitude = latitude.toDouble()
                vehicleLocation.longitude = longitude.toDouble()

                //   val distance = childrenLoc.distanceTo(vehicleLocation)
                // AFJUtils.writeLogs("The distance between the two points is ${distance / 1000} km")

                val distanceRequest = DistanceRequest(
                    location = arrayListOf(
                        vehicleLocation, childrenLoc
                    )
                )
                routeViewModel.getDistance(mBaseActivity, distanceRequest) {
                    if (it is String) {
                        mBaseActivity.writeExceptionLogs(it.toString())
                    } else {
                        val distance = it as Calculation
                        val distanceCalculate = distance.distanceValue!!.toInt()
                        if (distanceCalculate > minLocationDistance || distanceCalculate < 0) {
                            CustomDialog().showInputDialog(
                                mBaseActivity, "Note Required",
                                "Please mention your reason because child not pick from his location",
                                positiveButton = "Save",
                                negativeButton = "Cancel"
                            ) { msg ->
                                if (msg.isNotEmpty() && msg.length > 10) {
                                    //add note here
                                    request.routeSheet?.driverNote = msg
                                    //***************************
                                    routeViewModel.updateRouteListStatus(mBaseActivity, request) {
                                        val data = it as String

                                        if (data.contains("Success")) {
                                            routeViewModel.getRouteList(mBaseActivity)
                                        } else {
                                            mBaseActivity.writeExceptionLogs(data)
                                        }
                                    }
                                } else {
                                    mBaseActivity.showSnackMessage(
                                        "Please enter valid reason",
                                        binding.root
                                    )
                                }
                            }
                        } else {
                            routeViewModel.updateRouteListStatus(mBaseActivity, request) {
                                val data = it as String

                                if (data.contains("Success")) {
                                    routeViewModel.getRouteList(mBaseActivity)
                                } else {
                                    mBaseActivity.writeExceptionLogs(data)
                                }
                            }
                        }

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

