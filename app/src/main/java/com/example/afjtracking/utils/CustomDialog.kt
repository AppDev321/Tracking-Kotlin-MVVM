package com.example.afjtracking.utils


import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.afjtracking.R


class CustomDialog {


    fun showTaskCompleteDialog(
        context: Context,
        isShowTitle: Boolean = false,
        isShowMessage: Boolean = false,
        titleText: String = "",
        msgText: String = "",
        showOKButton: Boolean = false,
        showCancelButton: Boolean = false,
        okButttonText: String = "",
        cancelButtonText: String = "",
        lottieFile: Int,
        listner: DialogCustomInterface


    ) {
        val dialog: LottieDialog = LottieDialog(context)
        dialog.setAnimation(lottieFile)
            .setAnimationRepeatCount(LottieDialog.INFINITE)
            .setAutoPlayAnimation(true)
            .setTitleVisibility(View.GONE)
            .setMessageVisibility(View.GONE)
            .setCancelable(false)
            .setOKButtonHide(View.GONE)
            .setCancelButtonHide(View.GONE)

        if (isShowTitle) {
            dialog.setTitleVisibility(View.VISIBLE)
            dialog.setTitle(titleText)
        }
        if (isShowMessage) {
            dialog.setMessageVisibility(View.VISIBLE)
            dialog.setMessage(msgText)
        }

        if (showCancelButton) {
            dialog.setCancelButtonHide(View.VISIBLE)
            val btnCancel = dialog.lottieCancelButton
            btnCancel.text = cancelButtonText
            btnCancel.setOnClickListener { listner.onCancel(dialog) }
        }
        if (showOKButton) {
            dialog.setOKButtonHide(View.VISIBLE)
            val butonOK = dialog.lottieOKButton
            butonOK.text = okButttonText
            butonOK.setOnClickListener { listner.onClick(dialog) }
        }





        dialog.show()
    }


    fun initializeProgressDialog(
        context: Context,
        lottieFile: Int
    ): LottieDialog {
        val dialog = LottieDialog(context)
        dialog.setAnimation(lottieFile)
            .setAnimationRepeatCount(LottieDialog.INFINITE)
            .setAutoPlayAnimation(true)
            .setTitleVisibility(View.GONE)
            .setMessageVisibility(View.VISIBLE)
            .setMessage("Please Wait")
            .setCancelable(false)
            .setOKButtonHide(View.GONE)
            .setCancelButtonHide(View.GONE)
            .setBothButtonHide(View.GONE)

        return dialog
    }


    fun showIncomingCallDialog(
        context: Activity?,
        title: String,
        positiveListener: (() -> Unit)? = null,
        negativeListener: (() -> Unit)? = null,
        canceledOnTouchOutside: Boolean = false
    ): AlertDialog? {
        if (context == null) return null

        lateinit var buttonAnswer: Button
        lateinit var buttonDecline: Button
        return AlertDialog.Builder(context).apply {

        }.create().apply {
            setCanceledOnTouchOutside(canceledOnTouchOutside)
            val view = context.layoutInflater.inflate(R.layout.custom_call_notification, null)
            buttonAnswer = view.findViewById<Button>(R.id.btnAnswer)
            buttonDecline = view.findViewById<Button>(R.id.btnDecline)
            val txtCallerName = view.findViewById<TextView>(R.id.callerName)
            txtCallerName.text = title
            setView(view)
            buttonAnswer.setOnClickListener {
                this.dismiss()
                positiveListener?.invoke()
            }
            buttonDecline.setOnClickListener {
                this.dismiss()
                negativeListener?.invoke()
            }

            show()
        }
    }


    fun showSimpleAlertMsg(
        context: Context?, title: String? = null, message: String? = null,
        textPositive: String? = null, positiveListener: (() -> Unit)? = null,
        textNegative: String? = null, negativeListener: (() -> Unit)? = null,
        cancelable: Boolean = false, canceledOnTouchOutside: Boolean = false
    ): AlertDialog? {
        if (context == null) return null
        return AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(textPositive) { dialog, which ->
                positiveListener?.invoke()
            }
            setNegativeButton(textNegative) { dialog, which ->
                negativeListener?.invoke()
            }
            setCancelable(cancelable)

        }.create().apply {
            setCanceledOnTouchOutside(canceledOnTouchOutside)
            show()
        }
    }


    fun createCustomTextImageDialog(
        context: Context,
        cancelable: Boolean = false,
        title: String? = null, message: String? = null, imageUrl: String? = null,
        textPositive: String? = null, positiveListener: (() -> Unit)? = null,
        textNegative: String? = null, negativeListener: (() -> Unit)? = null,
        canceledOnTouchOutside: Boolean = false
    ) {

        val factory = LayoutInflater.from(context)
        val view: View = factory.inflate(R.layout.custom_notification_dialog, null)

        val alertDialog = AlertDialog.Builder(context)
            .setView(view)
            .create().apply {
                setCancelable(cancelable)
                setCanceledOnTouchOutside(canceledOnTouchOutside)
                // window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }


        val positiveButton = view.findViewById<Button>(R.id.btn_dialog_positive)
        val negativeButton = view.findViewById<Button>(R.id.btn_dialog_negative)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitleNotificaiton)
        val txtDesc = view.findViewById<TextView>(R.id.txtDescNotificaiton)
        val img = view.findViewById<ImageView>(R.id.img_notfication)

        if (textNegative == null) negativeButton.visibility = View.GONE
        if (textPositive == null) positiveButton.visibility = View.GONE
        if (title == null) txtTitle.visibility = View.GONE
        if (message == null) txtDesc.visibility = View.GONE
        if (imageUrl == null) img.visibility = View.GONE

        txtDesc.text = message
        txtTitle.text = title
        positiveButton.text = textPositive
        negativeButton.text = textNegative

        if (imageUrl != null) {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        img.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })

        }

        negativeButton.setOnClickListener {
            alertDialog.dismiss()
            negativeListener?.invoke()
        }
        positiveButton.setOnClickListener {
            alertDialog.dismiss()
            positiveListener?.invoke()
        }
        alertDialog.show()

    }


}

interface CustomDialogListener {
    fun onClick(var1: LottieDialog)
    fun onCancel(var1: LottieDialog)

}

interface DialogCustomInterface : CustomDialogListener {
    override fun onClick(var1: LottieDialog) {
    }

    override fun onCancel(var1: LottieDialog) {
    }
}