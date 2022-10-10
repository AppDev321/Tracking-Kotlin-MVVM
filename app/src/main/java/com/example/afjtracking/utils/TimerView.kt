package com.example.afjtracking.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.Chronometer
import android.widget.FrameLayout
import com.example.afjtracking.R


/**
 * TODO: document your custom view class.
 */
class TimerView : FrameLayout {

    private lateinit var timerListener: TimerListener
    private lateinit var chronometer: Chronometer
    fun getTimerVariable(): Chronometer {
        return chronometer
    }

    constructor(context: Context) : super(context) {
        initView(context, null)
    }

    constructor(context: Context, attr: AttributeSet? = null) : super(context, attr) {
        initView(context, attr)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initView(context, attrs)
    }


    fun setListener(timerListener: TimerListener) {
        this.timerListener = timerListener
    }

    private fun initView(
        context: Context,
        attrs: AttributeSet?,

        ) {
        val a = context.obtainStyledAttributes(attrs,R.styleable.TimerView)
        val textStyle    = a.getInt(R.styleable.TimerView_textStyle, 0)
        val textSize     = a.getFloat(R.styleable.TimerView_textSize, 5.0f)
        val circleColour = a.getColor(R.styleable.TimerView_textColor, Color.BLACK)


        chronometer = Chronometer(context)
        chronometer.setTextColor(circleColour)
        chronometer.textSize = textSize

        when (textStyle) {
            0 -> {
                chronometer.setTypeface(chronometer.typeface, Typeface.NORMAL)
            }
            1 -> {
                chronometer.setTypeface(chronometer.typeface, Typeface.BOLD)
            }
            2 -> {
                chronometer.setTypeface(chronometer.typeface, Typeface.ITALIC)
            }
        }

        chronometer.start()

        addView(chronometer)

        chronometer.setOnChronometerTickListener {
            val parts = it.text.split(":")
            if (parts.size > 1) {
                var seconds = 0
                var minutes = 0
                var hours = 0

                if (parts.size == 2) {
                    minutes = parts[0].toInt()
                    seconds = parts[1].toInt()

                } else if (parts.size == 3) {
                    hours = parts[0].toInt()
                    minutes = parts[1].toInt()
                    seconds = parts[2].toInt()
                }
                if (timerListener != null) {
                    timerListener.getStringTime(it.text.toString())
                    timerListener.getIntegerTime(hours, minutes, seconds)
                }
            }
        }
        a.recycle()
    }


}

interface TimerListener {
    fun getIntegerTime(hours: Int, minutes: Int, seconds: Int) {}
    fun getStringTime(time: String) {}

}