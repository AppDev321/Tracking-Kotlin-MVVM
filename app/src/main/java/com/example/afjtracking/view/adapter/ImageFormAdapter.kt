package com.example.afjtracking.view.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.afjtracking.R
import com.example.afjtracking.databinding.LayoutImageBoxBinding
import com.example.afjtracking.model.responses.Form
import com.example.afjtracking.model.responses.UploadFileAPiResponse
import com.example.afjtracking.view.fragment.fileupload.FileUploadDialog
import com.example.afjtracking.view.fragment.fileupload.UploadDialogListener
import com.example.afjtracking.view.fragment.forms.viewmodel.FormsViewModel
import java.io.File


class ImageFormAdapter(
    private val requestType: String,
    private val uploadId: String,
    private val mContext: AppCompatActivity,

    private var imageList: ArrayList<Form>,
    private val mShowGallaryPicker: Boolean =false,
) : RecyclerView.Adapter<ImageFormAdapter.ImageItemViewHolder>() {


    lateinit var listners: ImageFormListner

    fun setImageFormListner(listener: ImageFormListner) {
        this.listners = listener
    }


    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageItemViewHolder {

        val itemView = DataBindingUtil.inflate(
            LayoutInflater.from(mContext), R.layout.layout_image_box,
            parent, false
        ) as LayoutImageBoxBinding
        return ImageItemViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: ImageItemViewHolder, position: Int) {
        val data = imageList[position]

        holder.itemImageView.txtImageHeading.text = data.title
        holder.itemImageView.imgPreview.visibility = View.GONE
        holder.itemImageView.imgAdd.visibility = View.VISIBLE
        holder.itemImageView.imgDel.visibility = View.GONE


        holder.itemImageView.imgAdd
            .setOnClickListener {

                val dialog = FileUploadDialog.newInstance(
                    isDocumentPickShow = mShowGallaryPicker,
                    inpsectionType = requestType, //This will be change after
                    uniqueFileId = uploadId,
                    fieldName = data.fieldName!!,
                    isRecipetBtnShow =  true ,
                    fileUploadListner = (object : UploadDialogListener {
                        override fun onUploadCompleted(completedData: UploadFileAPiResponse) {

                        }

                        override fun onFilePathReceived(path: String) {

                            data.value = uploadId
                            listners.onPreviewGenerated(data, holder.adapterPosition,path)

                            Glide.with(mContext)
                                .load(path)
                                .placeholder(
                                    AppCompatResources.getDrawable(
                                        mContext,
                                        R.drawable.ic_no_image
                                    )
                                )

                                .into(holder.itemImageView.imgPreview)
                            holder.itemImageView.imgAdd.visibility = View.GONE
                            holder.itemImageView.imgDel.visibility = View.VISIBLE
                            holder.itemImageView.imgPreview.visibility = View.VISIBLE


                            holder.itemImageView.imgPreview.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW) //
                                    .setDataAndType(
                                        if (VERSION.SDK_INT >= VERSION_CODES.N) FileProvider.getUriForFile(
                                            mContext,
                                            mContext.packageName.toString() + ".provider",
                                            File(path)
                                        ) else Uri.fromFile(File(path)),
                                        "image/*"
                                    )

                            //  mContext.startActivity(intent)

                                //previewImage(path)
                            }


                        }

                    })
                )
                dialog.isCancelable = false
                dialog.show(mContext.supportFragmentManager, null)

            }


        holder.itemImageView.imgDel.setOnClickListener {
            data.value = ""
            listners.onPreviewGenerated(data, holder.adapterPosition,"")

            holder.itemImageView.imgAdd.visibility = View.VISIBLE
            holder.itemImageView.imgDel.visibility = View.GONE
            holder.itemImageView.imgPreview.visibility = View.GONE


        }


    }

     fun addRows(data:Form)
    {
        imageList.add(data)
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    inner class ImageItemViewHolder(val itemImageView: LayoutImageBoxBinding) :
        RecyclerView.ViewHolder(itemImageView.root)


    interface ImageFormListner {
        fun onPreviewGenerated(uploadForm: Form, position: Int,filePath:String)

    }

    fun previewImage(path: String) {
  /*      val dialog = Dialog(mContext)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_preview_dialog)
        val image: ImageView = dialog.findViewById(R.id.fullimage) as ImageView
        Glide.with(mContext)
            .load(path)
            .placeholder(
                AppCompatResources.getDrawable(
                    mContext,
                    R.drawable.ic_launch
                )
            )
            .into(image)
        dialog.show()
*/

        val nagDialog = Dialog(
            mContext
        )
        nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nagDialog.setContentView(R.layout.image_preview_dialog)
      //  val btnClose: Button = nagDialog.findViewById<View>(R.id.btnIvClose) as Button
        val ivPreview = nagDialog.findViewById<View>(R.id.fullimage) as ImageView

        Glide.with(mContext)
            .load(path)

            .placeholder(
                AppCompatResources.getDrawable(
                    mContext,
                    R.drawable.ic_no_image
                )
            )
            .into(ivPreview)
       /* btnClose.setOnClickListener(object : OnClickListener() {
            fun onClick(arg0: View?) {
                nagDialog.dismiss()
            }
        })*/
        nagDialog.show()
    }
}