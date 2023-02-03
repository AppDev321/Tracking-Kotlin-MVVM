package com.afjltd.tracking.utils

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.afjltd.tracking.model.responses.SensorOrientationData
import java.lang.Math.round

abstract class InspectionSensor : SensorEventListener {
    val TIME_STAMP = 1000
    private var accelerometerReading = FloatArray(3)
    private var gyroScopeReading = FloatArray(3)
    private var linearReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


   // val data: ArrayList<FloatArray> = arrayListOf()
   val data: ArrayList<SensorOrientationData> = arrayListOf()

    override fun onSensorChanged(event: SensorEvent) {
        val sensor: Sensor = event.sensor
        if (sensor.type == Sensor.TYPE_ACCELEROMETER) {

            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (sensor.type == Sensor.TYPE_GYROSCOPE) {

            System.arraycopy(event.values, 0, gyroScopeReading, 0, gyroScopeReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        } else {

            System.arraycopy(event.values, 0, linearReading, 0, linearReading.size)
        }

        sensorValueChanges()
        //predictActivity()
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun sensorValueChanges() {


        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val degrees = (Math.toDegrees(orientation.get(0).toDouble()) + 360.0) % 360.0
        val direction = AFJUtils.getDirection(degrees)
        val angle = round(degrees * 100) / 100

        val sensorOrientationData = SensorOrientationData(
          orientation,

            degrees,
            direction,
            angle,
            System.currentTimeMillis()
        )
        data.add(sensorOrientationData)

        if (data.size > TIME_STAMP) {
            data.clear()

            sendSensorValue(sensorOrientationData)
        }

    }


    //abstract fun sendSensorValue(data: ArrayList<FloatArray>)
    abstract fun sendSensorValue(data: SensorOrientationData)
}
