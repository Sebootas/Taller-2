package com.sebotas.taller2


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.sebotas.taller2.databinding.ActivityMapsBinding
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mLightSensorEventListener: SensorEventListener

    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMapsBinding

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(
            mLightSensorEventListener, mLightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(mLightSensorEventListener)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!



        mLightSensorEventListener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 20) {
                        Log.i("MAPS", "DARK MAP " + event.values[0])
                        mMap!!.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                baseContext,
                                R.raw.map_style_night
                            )
                        )
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + event.values[0])
                        mMap!!.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                baseContext,
                                R.raw.map_style_light
                            )
                        )
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }






        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        //Inicialización del objeto
        val mGeocoder = Geocoder(baseContext)

        val editText = findViewById<EditText>(R.id.editTextText)




        binding.editTextText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                //Cuando se realice la busqueda

                val addressText = editText.text.toString()

                if (addressText.isNotEmpty()) {
                    try {
                        val addresses = mGeocoder.getFromLocationName(addressText, 2)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val addressResult = addresses[0]
                            val position = LatLng(addressResult.latitude, addressResult.longitude)
                            if (mMap != null) {
                                mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15F))
                                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(position))
                                mMap!!.addMarker(
                                    MarkerOptions().position(position)
                                        .title("GEO")
                                        .snippet("Algo")
                                )


                            } else {
                                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "La dirección está vacía", Toast.LENGTH_SHORT).show()
                }
                // Indicate that the action was handled
                true
            } else {
                // Indicate that the action was not handled
                false
            }
        }









        mMap = googleMap

        mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_night))

        // Add a marker in Sydney and move the camera
        val toros = LatLng(5.063903320621079, -75.5249597207237)
        val cable = LatLng(5.056142045411864, -75.48627524765217)
        val palogrande = LatLng(5.056989091540201, -75.49003727437554)
        val mom = LatLng(5.044162401761589, -75.47793600361102)
        val dad = LatLng(5.02788376862441, -75.59534740676227)


        mMap!!.addMarker(
            MarkerOptions().position(toros).title("Ole")
                .snippet("Manizales").alpha(1F)
        )


        mMap!!.addMarker(
            MarkerOptions().position(cable).title("Torre del Cable")
                .snippet("Manizales").alpha(1F)
        )


        mMap!!.addMarker(
            MarkerOptions().position(palogrande).title("Palogrande")
                .snippet("Once Caldas Campeon de America 2004").alpha(1F)
                .icon(bitmapDescriptorFromVector(this, R.drawable.baseline_1k_24))
        )


        mMap!!.addMarker(
            MarkerOptions().position(mom).title("Mom")
                .snippet("Manizales").alpha(1F)
        )

        mMap!!.addMarker(
            MarkerOptions().position(dad).title("Dad")
                .snippet("Manizales").alpha(1F)
        )


        //mMap!!.clear()

    }


    private fun bitmapDescriptorFromVector(
        context: Context,
        vectorResId: Int
    ): BitmapDescriptor {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
