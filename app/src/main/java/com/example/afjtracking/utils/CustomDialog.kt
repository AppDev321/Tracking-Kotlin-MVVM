package com.example.afjtracking.utils

import android.content.Context
import android.view.View

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

        if(isShowTitle)
        {
            dialog.setTitleVisibility(View.VISIBLE)
            dialog.setTitle(titleText)
        }
        if(isShowMessage)
        {
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
    ) :LottieDialog{
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

}

 interface CustomDialogListener {
    fun onClick(var1: LottieDialog)
    fun onCancel(var1: LottieDialog)

}

interface DialogCustomInterface: CustomDialogListener
{
    override fun onClick(var1: LottieDialog) {
    }
    override fun onCancel(var1: LottieDialog) {
    }
}