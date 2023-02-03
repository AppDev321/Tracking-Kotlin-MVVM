package com.afjltd.tracking.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.R


abstract class PaginatedAdapter<ITEM, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val ITEM = 0
    private val LOADING = 1


    private var isLoadingAdded: Boolean = false
    private val mDataSet: MutableList<ITEM> = ArrayList()
    private var mListener: OnPaginationListener? = null
    private var mStartPage = 0
    private var mCurrentPage = 0
    private var mPageSize = 10
    private var mRecyclerView: RecyclerView? = null
    private var loadingNewItems = true
    abstract fun createCustomViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return when (viewType) {
            ITEM -> {
                createCustomViewHolder(parent, viewType)
            }
            LOADING -> {

                val inflater = LayoutInflater.from(parent.context)
                val view: View = inflater.inflate(R.layout.load_more_progress, null)
                LoadingViewHolder(view) as VH
            }
            else -> throw IllegalArgumentException("Wrong view type")
        }
    }

    abstract fun customBindHolder(holder: VH, position: Int)
    override fun onBindViewHolder(holder: VH, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                customBindHolder(holder, position)
            }
            LOADING -> {
            }
        }
    }

    private fun remove(r: ITEM) {
        val position = mDataSet.indexOf(r)
        if (position > -1) {
            mDataSet.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    fun isEmpty(): Boolean {
        return itemCount == 0
    }


    fun addLoadingFooter(item: ITEM) {
        isLoadingAdded = true
        mDataSet.add(item)
        notifyItemInserted(mDataSet.size - 1)
    }

    fun removeLoadingFooter() {

        isLoadingAdded = false
        val position = mDataSet.size - 1
        if (position < 0) return
        val result = getItem(position)

        if (result != null) {
            if(mDataSet.size >0) {
                mDataSet.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }


    override fun getItemViewType(position: Int): Int {

        return if (position == mDataSet.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    override fun getItemCount(): Int {
        return mDataSet.size
    }

    fun submitItems(collection: Collection<ITEM>) {
        mDataSet.addAll(collection)
        notifyDataSetChanged()
        if (mListener != null) {
            mListener!!.onCurrentPage(mCurrentPage)
            if (collection.size == mPageSize) {
                loadingNewItems = false
            } else {
                mListener!!.onFinish()
            }
        }
    }

    fun submitItem(item: ITEM) {
        mDataSet.add(item)
        notifyDataSetChanged()
    }

    fun submitItem(item: ITEM, position: Int) {
        mDataSet.add(position, item)
        notifyDataSetChanged()
    }

    fun clear() {

        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
        // mDataSet.clear()
        notifyDataSetChanged()
    }

    protected fun getItem(position: Int): ITEM {
        return mDataSet[position]
    }


    var recyclerView: RecyclerView?
        get() = mRecyclerView
        set(recyclerView) {
            mRecyclerView = recyclerView
            initPaginating()
            setAdapter()
        }

    fun setDefaultRecyclerView(activity: Activity, recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        mRecyclerView = recyclerView
        initPaginating()
        setAdapter()
    }

    private fun setAdapter() {
        mRecyclerView!!.adapter = this
    }

    fun setPageSize(pageSize: Int) {
        mPageSize = pageSize
    }
    fun getPageSize()=  mPageSize


    fun setStartPage(mFirstPage: Int) {
        mStartPage = mFirstPage
        mCurrentPage = mFirstPage
    }

    fun getStartPage()=mStartPage


    fun getCurrentPage()= mCurrentPage


    private fun initPaginating() {
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                val totalItemCount = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val endHasBeenReached = lastVisible + 2 >= totalItemCount
                if (totalItemCount > 0 && endHasBeenReached) {
                    if (mListener != null) {
                        if (!loadingNewItems) {
                            loadingNewItems = true
                            mListener!!.onNextPage(++mCurrentPage)
                        }
                    }
                }
            }
        })
    }

    fun setOnPaginationListener(onPaginationListener: OnPaginationListener?) {
        mListener = onPaginationListener
    }

    interface OnPaginationListener {
        fun onCurrentPage(page: Int)
        fun onNextPage(page: Int)
        fun onFinish()
    }


}

