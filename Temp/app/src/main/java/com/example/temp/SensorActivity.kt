package com.example.temp

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.DynamicSensorCallback
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SensorActivity : Activity() {
    private var bmpDriver: Bmx280SensorDriver? = null
    private lateinit var sensorManager: SensorManager
    private var temperature: Float? = null
    private var pressure: Float? = null

    companion object {
        private val eventHandler = Handler()
        private val TAG = "[SENSOR ACTIVITY]"
    }

    private lateinit var temperatureRef: DatabaseReference
    private lateinit var pressureRef: DatabaseReference

    //auxiliary variable that helps to mock the demo values
    private var const = -1

    private val sensorEventRunnable = Runnable {
        Log.d(TAG, "TEMPERATURE = ${toCelsius(temperature)} C.")
        Log.d(TAG, "PRESSURE =  ${pressure} Pa.")
    }

    // convert the temperature to celsius from F and divide by 2 because the sensor seems to be damaged
    private fun toCelsius(temperature: Float?): Float {
        return (temperature!! - 32) * 5 / 9 / 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDatabase()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerDynamicSensorCallback(setupDynamicSensorCallbackObject())

        setupBmpDriver()
    }

    private fun setupDatabase() {
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        temperatureRef = database.getReference("temperature")
        pressureRef = database.getReference("pressure")
    }

    private fun setupDynamicSensorCallbackObject(): DynamicSensorCallback {
        return object : DynamicSensorCallback() {
            override fun onDynamicSensorConnected(sensor: Sensor?) {
                super.onDynamicSensorConnected(sensor)

                if (sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE || sensor?.type == Sensor.TYPE_PRESSURE) {
                    sensorManager.registerListener(
                        object : SensorEventListener {
                            override fun onAccuracyChanged(theSensor: Sensor?, accuracy: Int) {
                                Log.i(TAG, "onAccuracyChanged: " + accuracy)
                            }

                            override fun onSensorChanged(event: SensorEvent?) {
                                if (event != null) {
                                    // sometimes the sensor values are changing too quickly and the UI becomes confusing
                                    Thread.sleep(2000)
                                    Log.i(
                                        TAG,
                                        "onSensorChanged: ${event.sensor.stringType} = " + event.values[0]
                                    )
                                    if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE)
                                        temperature = toCelsius(event.values[0])
                                    else if (event.sensor.type == Sensor.TYPE_PRESSURE)
                                        pressure = event.values[0]

                                    const *= -1

                                    if (pressure != null && temperature != null) {

                                        // (-1 * const) - used only to mock the values for the demo - it should be removed
                                        pressureRef.setValue(pressure!! + (-1 * const))
                                        temperatureRef.setValue(temperature!! + (-1 * const))

                                        eventHandler.post(sensorEventRunnable)
                                    }
                                }
                            }
                        }, sensor, 1000
                    )
                }
            }
        }
    }

    private fun setupBmpDriver() {
        try {
            println("step 0... creating object")
            bmpDriver = Bmx280SensorDriver("I2C1")
            println("step 1... registering temperature")
            bmpDriver?.registerTemperatureSensor()
            println("step 2... registering pressure")
            bmpDriver?.registerPressureSensor()
            println("step 3... bmpDriver is set.")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering BMP280 + ${e.message}")
        }
    }
}
