package com.example.afjtracking.utils

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.afjtracking.R

class CustomWidget {



    fun  createCustomViews(
        activity:AppCompatActivity,
        containerView :LinearLayout,


        title: String,
        hint: String,

        listener: CustomWidgetListener,

    )
    {


        var view = activity.layoutInflater.inflate(R.layout.layout_text_view, null)
        var textTitleLable = view.findViewById<TextView>(R.id.text_label)
        textTitleLable.setTextColor(Color.BLACK)
        textTitleLable.text = title
        containerView.addView(view)


        view = activity.layoutInflater.inflate(R.layout.layout_multiline_comment_view, null)
        var inputText = view.findViewById<EditText>(R.id.edMultiline)
        inputText.hint = hint

     listener.getEditTextView(inputText)

        inputText.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if(!hasFocus) {

                    listener.onEditTextNotFocus()
                }
            }

        })

        inputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
              listener.onEditTextChange (s.toString())
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })



        containerView.addView(view)

    }

}

interface  CustomWidgetListener {

   fun  onEditTextNotFocus( )
   fun onEditTextChange( value :String)
    fun getEditTextView( value :EditText)
}


