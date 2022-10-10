package com.example.afjtracking.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

abstract class InspectionSensor : SensorEventListener {
    val TIME_STAMP = 5000
    private var accelerometerReading = FloatArray(3)
    private var gyroScopeReading = FloatArray(3)
    private var linearReading = FloatArray(3)

    val data: ArrayList<FloatArray> = arrayListOf()


    override fun onSensorChanged(event: SensorEvent) {
        val sensor: Sensor = event.sensor
        if (sensor.type == Sensor.TYPE_ACCELEROMETER) {

            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (sensor.type == Sensor.TYPE_GYROSCOPE) {

            System.arraycopy(event.values, 0, gyroScopeReading, 0, gyroScopeReading.size)
        } else {

            System.arraycopy(event.values, 0, linearReading, 0, linearReading.size)
        }

        sensorValueChanges()
        //predictActivity()
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun sensorValueChanges() {

        data.add(accelerometerReading)
        data.add(gyroScopeReading)
        data.add(linearReading)


        if (data.size > TIME_STAMP) {
            data.clear()
            data.add(accelerometerReading)
            data.add(gyroScopeReading)
            data.add(linearReading)
            sendSensorValue(data)
        }

    }

    abstract fun sendSensorValue(data: ArrayList<FloatArray>)

}