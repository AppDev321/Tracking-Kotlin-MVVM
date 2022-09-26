package com.example.afjtracking.view.fragment.fuel

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.R
import com.example.afjtracking.databinding.FragmentFuelFromBinding
import com.example.afjtracking.model.requests.SaveFormRequest
import com.example.afjtracking.model.responses.FuelForm
import com.example.afjtracking.model.responses.InspectionForm
import com.example.afjtracking.model.responses.Vehicle
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.Constants
import com.example.afjtracking.utils.CustomWidget
import com.example.afjtracking.utils.StoreCustomFormData
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.auth.CustomAuthenticationView
import com.example.afjtracking.view.fragment.fuel.viewmodel.FuelViewModel

class FuelFormFragment : Fragment() {

    private var _binding: FragmentFuelFromBinding? = null
    private val binding get() = _binding!!

    var storedData: ArrayList<StoreCustomFormData> = arrayListOf()

    lateinit var fuelViewModel: FuelViewModel
    private lateinit var mBaseActivity: NavigationDrawerActivity
    val uniqueUploadId = Constants.FILE_UPLOAD_UNIQUE_ID
    var requestType = ""


    var isOdoMeterErrorFound = false
    var lastOdoReading = 0
    var odoReadingError = ""
    lateinit var txtErrorMsg: TextView

    var vehicle: Vehicle = Vehicle()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {


        fuelViewModel = ViewModelProvider(this).get(FuelViewModel::class.java)
        _binding = FragmentFuelFromBinding.inflate(inflater, container, false)


        val root: View = binding.root
        txtErrorMsg = binding.txtErrorMsg

        val authView = CustomAuthenticationView(requireContext())
        binding.root.addView(authView)

        authView.addAuthListner(object : CustomAuthenticationView.AuthListeners {
            override fun onAuthCompletionListener(boolean: Boolean) {
                if (_binding == null)
                    return
                if (boolean) {

                    binding.root.removeAllViews()
                    binding.root.addView(binding.baseLayout)
                    binding.root.addView(binding.txtErrorMsg)
                    fuelViewModel.getFuelFormRequest(mBaseActivity)
                } else {
                    binding.root.removeAllViews()
                    binding.root.addView(authView)
                }

            }

            override fun onAuthForceClose(boolean: Boolean) {
                mBaseActivity.onBackPressed()
            }
        })





        fuelViewModel.showDialog.observe(viewLifecycleOwner) {
            mBaseActivity.showProgressDialog(it)
        }





        fuelViewModel.getFuelForm.observe(viewLifecycleOwner) {
            if (it != null) {
                try {
                    showInspectionCreationForm(it)
                } catch (e: Exception) {
                    mBaseActivity.writeExceptionLogs(e.toString())
                }
                fuelViewModel._fuelForm.value = null
            }
        }

        fuelViewModel.getVehicle.observe(viewLifecycleOwner) {
            if (it != null) {
                vehicle = it
                fuelViewModel._vehicle.value = null
            }
        }


        fuelViewModel.errorsMsg.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                mBaseActivity.toast(it, false)
                binding.txtErrorMsg.visibility = View.VISIBLE
                binding.txtErrorMsg.text = it.toString()
                binding.baseLayout.visibility = View.GONE
                fuelViewModel.errorsMsg.value = null
            }
        })

        fuelViewModel.apiUploadStatus.observe(viewLifecycleOwner) {
            if (it) {
                mBaseActivity.onBackPressed()
                mBaseActivity.showSnackMessage(
                    "Request saved", requireView()
                )
            }
        }




        return root
    }


    fun showInspectionCreationForm(fuelList: List<InspectionForm>) {

        binding.baseLayout.visibility = View.VISIBLE
        binding.txtErrorMsg.visibility = View.GONE

        binding.txtInspectionTitle.text = "Fuel Form"

        val odoReading = vehicle.odometerReading
        lastOdoReading = if (odoReading!!.isEmpty()) 0 else odoReading.toInt()
        odoReadingError = getString(R.string.odo_meter_error,lastOdoReading)

        for (i in fuelList.indices) {
            val formData = fuelList[i]
            try {
                //createViewChecks(formData.type!!.uppercase(), formData, i)
                val customFormData = CustomWidget().createDynamicFormViews(
                    mBaseActivity,
                    formData,
                    i,
                    lastOdoReading,
                    uniqueUploadId,
                    requestType,
                    binding.layoutVdiForm

                )
                if (customFormData != null) {
                    storedData.add(customFormData)

                }
            } catch (e: Exception) {
                mBaseActivity.writeExceptionLogs(e.toString())
            }
        }



        binding.btnPreviousCehck.setOnClickListener {
            mBaseActivity.onBackPressed()
        }
        binding.btnSubmit.setOnClickListener {
            for (i in storedData.indices) {
                if (storedData[i].isOdoMeterErrorFound == true) {
                    isOdoMeterErrorFound = true
                }
            }


            if (!isOdoMeterErrorFound) {
                var isAllRequired = true
                for (i in storedData.indices) {
                    fuelList[i].value = storedData[i].formData!!.value

                    if (storedData[i].formData?.required == true) {
                        if (fuelList[i].value?.isEmpty() == true) {

                            mBaseActivity.showSnackMessage(
                                "Please enter ${storedData[i].formData!!.title}",
                                binding.root
                            )
                            isAllRequired = false
                            break
                        }
                    }

                }
                if (isAllRequired) {

                    val request = SaveFormRequest()
                    request.fuelForm = fuelList
                    fuelViewModel.saveFuelForm(request, mBaseActivity)


                }
            } else {
                mBaseActivity.showSnackMessage(odoReadingError, binding.root)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.removeAllViews()
        _binding = null


    }

}
