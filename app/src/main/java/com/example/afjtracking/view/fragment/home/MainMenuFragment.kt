package com.example.afjtracking.view.fragment.home

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.afjtracking.R
import com.example.afjtracking.databinding.MenuDashboardBinding
import com.example.afjtracking.model.responses.VehicleMenu
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.adapter.MenuItemListner
import com.example.afjtracking.view.adapter.ViewPagerAdapter
import com.example.afjtracking.view.fragment.forms.FormsFragment


class MainMenuFragment : Fragment(), MenuItemListner {

    private var _binding: MenuDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var menuItemsList: List<VehicleMenu>

    private lateinit var mBaseActivity: NavigationDrawerActivity

    companion object {
        fun getInstance(menuItemsList: List<VehicleMenu>)= MainMenuFragment().apply {
            this.menuItemsList = menuItemsList
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
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

    override fun onMenuItemClick(item: VehicleMenu) {
        mBaseActivity.toolbarVisibility(true)

        when (item.identifier) {
            "vehicle_navigation" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.map_nav
                )
            }
            "vehicle_daily_inspection" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_vdi_inspection_list
                )
            }
            "vehicle_inspection" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_weekly_inspection
                )
            }
            "vehicle_attendance" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_attendance_form
                )
            }
            "vehicle_fuel_form",
            "vehicle_shift_job_form",
            "vehicle_report_form" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_report_form,
                    argument = bundleOf(FormsFragment.FORM_IDENTIFIER_ARGUMENT to item)
                )
            }
            "vehicle_device_info" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_device_info
                )
            }

            "vehicle_change_driver" -> {
                mBaseActivity.moveFragmentToNextFragment(
                    binding.root,
                    R.id.nav_attendance_form
                )
            }
        }
    }


}