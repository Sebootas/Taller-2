package com.sebotas.taller2


import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.Manifest
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.sebotas.taller2.LocationData
import java.io.File
import java.util.Calendar


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mSensorManager: SensorManager
    private lateinit var mLightSensor: Sensor
    private lateinit var mLightSensorEventListener: SensorEventListener

    /* Agregado */
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var lastLocationData: LocationData? = null
    /* Fin agregado */

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

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!

        /* Inicio agregado */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica si tienes permiso para acceder a la ubicación
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no tienes permiso, solicítalo al usuario
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        /* Fin agregado */

        mLightSensorEventListener = object : SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {
                if (mMap != null) {
                    if (event.values[0] < 30) {
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
                                        .title("GEO-pocision")
                                        .snippet("Pocision obtenida de la busqueda")
                                )


                            } else {
                                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Log.e("MAP_ACTIVITY", "Error en la pocision por GEO. ${e.message}")
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

        //mMap!!.clear()


        mMap!!.setOnMapLongClickListener { latLng ->
            // Reverse geocode the latitude and longitude to get the actual address
            val geocoder = Geocoder(this)
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses?.get(0)?.getAddressLine(0)
            val placeName = addresses?.get(0)?.featureName // Get the name of the place

            // Add a new marker at the long-pressed position with the name of the place
            mMap!!.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(placeName) // Use the name of the place as the marker title
                    .snippet(address)
            )

            // Show a toast with the actual address
            Toast.makeText(this, "Nuevo marcador colocado en: $placeName", Toast.LENGTH_SHORT).show()

            val distancia = calculateDistance(
                lastLocationData!!.latitud,
                lastLocationData!!.longitud,
                latLng.latitude,
                latLng.longitude
            )
            Toast.makeText(this@MapsActivity, "Distancia entre puntos: ${distancia}", Toast.LENGTH_SHORT).show()
        }

        requestLocationUpdates() // <- ejecucion por primera vez de la logica que verificara la pocision guardada con la actual
        // Programa la ejecución periódica cada 10 segundos
        handler.postDelayed(runnable, 10000) // 10000 milisegundos = 10 segundos

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

    /**
     * Función que se encarga de guardar la pocision
     */
    private fun savePositionTask(locationData: LocationData){
        // Verifica si tienes permiso para acceder a la ubicación
        if (ContextCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Si tienes permiso, obtén la ubicación actual
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    try{

                        // Guarda en json de manera local las coordenadas.
                        // Convertir el objeto de ubicación a JSON
                        val gson = Gson()
                        val locationJson = gson.toJson(locationData)

                        // Guardar el JSON en un archivo local
                        val fileName = "location.json"
                        val file = File(this@MapsActivity.filesDir, fileName)
                        file.writeText(locationJson)

                        Toast.makeText(this@MapsActivity,"Datos de ubicación guardados.",Toast.LENGTH_SHORT).show()
                        Log.d("FileSave", "Location data saved successfully to location.json")
                        Log.d("FilePath", "File path: ${file.absolutePath}")

                    }catch (e: Exception){
                        Toast.makeText(this@MapsActivity,"Error al guardar pocision en JSON.${e.message}",Toast.LENGTH_SHORT).show()
                        Log.e("MAP_ACTIVITY", "Error en el guardado de la pocision JSON: ${e.message}")
                        Log.e("FileSave", "Error saving location data: ${e.message}")

                    }
                }
                .addOnFailureListener { e ->
                    // Maneja cualquier error al obtener la ubicación
                    Toast.makeText(
                        this@MapsActivity,
                        "Error al obtener la ubicación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("MAP_ACTIVITY", "Error al obtener la ubicacion: ${e.message}")
                }
        }
    }

    /**
     * Función que se encarga de cargar la pocisión.
     */
    private fun loadPositionTask(){
        try{
            val fileName = "location.json"
            val file = File(this@MapsActivity .filesDir, fileName)

            // Leer el contenido del archivo JSON
            val locationJson = file.readText()

            // Convertir JSON a objeto de ubicación
            val gson = Gson()

            val location = gson.fromJson(locationJson, LocationData::class.java)
            val latLong = LatLng(location.latitud, location.longitud)

            mMap!!.moveCamera(CameraUpdateFactory.zoomTo(17F))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLong))
            mMap!!.addMarker(
                MarkerOptions().position(latLong)
                    .title("Ubicacion cargada")
                    .snippet("Ultima pocision registrada")
            )
        }catch(e: Exception){
            Toast.makeText(this@MapsActivity, "Error al cargar pocision desde JSON.${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MAP_ACTIVITY", "Error en la carga de la pocision JSON: ${e.message}")
        }
    }

    private fun requestLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val newLocationData = LocationData(it.latitude, it.longitude, Calendar.getInstance().toString())
                    if (lastLocationData == null || isLocationChangeSignificant(lastLocationData!!, newLocationData)) {
                        lastLocationData = newLocationData
                        savePositionTask(newLocationData)
                        loadPositionTask()
                    }
                }
            }
    }

    private fun isLocationChangeSignificant(oldLocation: LocationData, newLocation: LocationData): Boolean {
        val distanceChange = calculateDistance(oldLocation.latitud, oldLocation.longitud, newLocation.latitud, newLocation.longitud)
        return distanceChange >= 30 // 30 meters threshold for significant change
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            // Llama a tu función cada vez que se ejecute el Runnable
            requestLocationUpdates()

            // Vuelve a programar la ejecución del Runnable después de 10 segundos
            handler.postDelayed(this, 10000) // 10000 milisegundos = 10 segundos
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detén la ejecución periódica cuando la actividad se destruya
        handler.removeCallbacks(runnable)
    }



}
