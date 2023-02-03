package com.afjltd.tracking.view.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.afjltd.tracking.model.responses.Form
import com.afjltd.tracking.model.responses.UploadFileAPiResponse
import com.afjltd.tracking.view.fragment.fileupload.FileUploadDialog
import com.afjltd.tracking.view.fragment.fileupload.UploadDialogListener
import com.afjltd.tracking.R
import com.afjltd.tracking.databinding.LayoutFileChooseBoxBinding
import java.io.File


class FileFormAdapter(
    private val requestType: String,
    private val uploadId: String,
    private val mContext: AppCompatActivity,

    private var imageList: ArrayList<Form>,
    private val mShowGallaryPicker: Boolean =false,
) : RecyclerView.Adapter<FileFormAdapter.ImageItemViewHolder>() {


    lateinit var listners: ImageFormListner

    fun setImageFormListner(listener: ImageFormListner) {
        this.listners = listener
    }


    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {

        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), R.layout.layout_file_choose_box,
            parent, false
        ) as LayoutFileChooseBoxBinding
        return ImageItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        val data = imageList[position]

        holder.itemImageView.txtImagePath.text = data.title



        holder.itemImageView.btnPickImage
            .setOnClickListener {

                val dialog = FileUploadDialog.newInstance(
                    isDocumentPickShow = mShowGallaryPicker,
                    inpsectionType = requestType, //This will be change after
                    uniqueFileId = uploadId,
                    fieldName = data.fieldName!!,
                    isImageDialog = false,
                    fileUploadListner = (object : UploadDialogListener {
                        override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                        }

                        override fun onFilePathReceived(path: String) {

                            data.value = uploadId
                            holder.itemImageView.txtImagePath.text = File(path).name
                            listners.onPreviewGenerated(data, holder.adapterPosition)

                        }

                    })
                )
                dialog.isCancelable = false
                dialog.show(mContext.supportFragmentManager, null)

            }


    }


    @SuppressLint("NotifyDataSetChanged")
    inner class ImageItemViewHolder(val itemImageView: LayoutFileChooseBoxBinding) :
        RecyclerView.ViewHolder(itemImageView.root)


    interface ImageFormListner {
        fun onPreviewGenerated(uploadForm: Form, position: Int)

    }

}