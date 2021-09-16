package com.example.tempreader

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class ScreenFragment : Fragment() {

    companion object {
        private val TAG = "TEMP_PRESSURE_READER"
        fun newInstance() = ScreenFragment()
    }

    private lateinit var temperatureRef: DatabaseReference
    private lateinit var pressureRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.screen_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        temperatureRef = database.getReference("temperature")
        pressureRef = database.getReference("pressure")

        temperatureRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                Log.d(TAG, "Temperature = $value")
                view?.findViewById<TextView>(R.id.tempValue)?.text = value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "[Temperature] failed to read value.", error.toException())
            }
        })

        pressureRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Long::class.java)
                Log.d(TAG, "Pressure =  $value")
                view?.findViewById<TextView>(R.id.pressureValue)?.text = value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "[Pressure] failed to read value.", error.toException())
            }
        })
    }
}