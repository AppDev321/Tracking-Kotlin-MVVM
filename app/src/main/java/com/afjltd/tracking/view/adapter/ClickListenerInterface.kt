package com.afjltd.tracking.view.adapter


interface ClickListenerInterface {
    fun<T> handleContinueButtonClick(data: T)
    fun<T> routeLocationDirectionClick(data:T){}
}