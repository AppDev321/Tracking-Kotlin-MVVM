package com.example.afjtracking.view.fragment.home

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Transformations.map
import com.example.afjtracking.R
import com.example.afjtracking.databinding.MenuDashboardBinding
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.InspectionSensor
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.MenuItemListner
import com.example.afjtracking.view.adapter.MenuModel
import com.example.afjtracking.view.adapter.ViewPagerAdapter


class MainMenuFragment : Fragment(), MenuItemListner {

    private var _binding: MenuDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuItemsList: List<List<MenuModel>>

    private lateinit var mBaseActivity: NavigationDrawerActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val menuList1 = arrayListOf(
            MenuModel(
                0,
                "Navigation",
                R.drawable.location
            ),
            MenuModel(
                1,
                "Daily Inspection",
                R.drawable.inspection
            ),
            MenuModel(
                2,
                "Inspection",
                R.drawable.weekly_inspection
            ),
            MenuModel(
                3,
                "Attendance",
                R.drawable.qr_scan
            ),
            MenuModel(
                4,
                "Fuel Form",
                R.drawable.fuel_pump
            ),
            MenuModel(
                5,
                "Report Form",
                R.drawable.incident
            ),
            MenuModel(
                6,
                "Device Info",
                R.drawable.info
            ),
        )
        /* val menuList2= arrayListOf(
             MenuModel(7,"Vehicle Info", ResourcesCompat.getDrawable(resources, R.drawable.ic_device_info, null)),
             MenuModel(8,"Device Info", ResourcesCompat.getDrawable(resources, R.drawable.ic_device_info, null)),
         )*/

        menuItemsList = listOf(menuList1)






    }





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = MenuDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val pagerAdapter = ViewPagerAdapter(requireContext(), menuItemsList, this)
        binding.pager.adapter = pagerAdapter
        binding.dots.attachViewPager(binding.pager)
        binding.dots.setDotTint(Color.BLACK)



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuModel) {
        mBaseActivity.toolbarVisibility(true)

        when (item.id) {
            0 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.map_nav
                )
            }
            1 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_vdi_inspection_list
                )
            }
            2 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_weekly_inspection
                )
            }
            3 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_attendance_form
                )
            }
            4 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_fuel_form
                )
            }
            5 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_report_form
                )
            }
            6 -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_device_info
                )
            }
        }
    }


}