package com.example.afjtracking.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.example.afjtracking.R

class ViewPagerAdapter
    (
    private val mContext: Context,
    private val groupedMenuItemsList: List<List<MenuModel>>,
    private val listner: MenuItemListner
) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val recyclerView = RecyclerView(mContext)
                 val  params =  RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
        GridLayoutManager(
            mContext,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            recyclerView.layoutManager = this
        }
        recyclerView.layoutParams = params
        recyclerView.adapter = MenuModelAdapter(mContext,groupedMenuItemsList[position],listner)
        container.addView(recyclerView)
        return recyclerView
    }


    override fun getCount(): Int {
        return groupedMenuItemsList.size
    }
    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }
}