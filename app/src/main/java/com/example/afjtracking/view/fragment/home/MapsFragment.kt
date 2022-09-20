package com.example.afjtracking.view.fragment.home


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.afjtracking.R
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.utils.LatLngInterpolator
import com.example.afjtracking.utils.MapUtils
import com.example.afjtracking.utils.MarkerAnimation
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient


class MapsFragment : Fragment(), OnMapReadyCallback {

    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445
    var googleMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLocationMarker: Marker? = null
    private var currentLocation: Location? = null
    private var firstTimeFlag = true
    private var previousLatLng :LatLng?= null

    var placesClient: PlacesClient? = null

    private val clickListener =
        View.OnClickListener { view ->
            if (view.id == R.id.currentLocationImageButton
                && googleMap != null
                && currentLocation != null
            )
                animateCamera(currentLocation!!)
        }
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation == null) return
            currentLocation = locationResult.lastLocation
            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation!!)
                firstTimeFlag = false
            }
            showMarker(currentLocation!!)
        }
    }


    private lateinit var mBaseActivity: NavigationDrawerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        val myLoc = view.findViewById(R.id.currentLocationImageButton) as ImageButton
        myLoc.setOnClickListener(clickListener)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().applicationContext, resources.getString(R.string.map_key))
        }

        // Create a new Places client instance.

        // Create a new Places client instance.
        val placesClient = Places.createClient(requireContext())
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()
        // Create a RectangularBounds object.
        // Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),  //dummy lat/lng
            LatLng(-33.858754, 151.229596)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds) //.setLocationRestriction(bounds)
                .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery("Lahore")
                .build()


        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
            var    mResult = StringBuilder()
                for (prediction in response.autocompletePredictions) {
                    mResult.append(" ").append(
                        """
                            ${prediction.getFullText(null)}
                            
                            """.trimIndent()
                    )
                }
                AFJUtils.writeLogs(java.lang.String.valueOf(mResult))
            }.addOnFailureListener { exception: Exception? ->
            if (exception is ApiException) {
                val apiException = exception
                AFJUtils.writeLogs("Places exception:$apiException")
            }
        }

    }

    @Override
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        val source = LatLng(31.490127, 74.316971) //starting point (LatLng)
        val destination = LatLng(31.474316, 74.316112) // ending point (LatLng)

    }

    fun startCurrentLocationUpdates() {
        val locationRequest = create()
        locationRequest.priority =PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 3000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    mBaseActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    mBaseActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    mBaseActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
                return
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )


    }

    fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(mBaseActivity)
        if (ConnectionResult.SUCCESS == status)
            return true
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(mBaseActivity, "Install google play services", Toast.LENGTH_LONG)
                    .show()
        }
        return false
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(mBaseActivity, "Permission denied", Toast.LENGTH_SHORT).show()
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates()
        }
    }

    fun animateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                getCameraPositionWithBearing(
                    latLng
                )
            )
        )
    }

    fun getCameraPositionWithBearing(latLng: LatLng): CameraPosition {
        return CameraPosition.Builder().target(latLng).zoom(17.0F).build()
    }

    fun showMarker(currentLocation: Location) {

        fusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener { location : Location? ->
                if(location != null)
                previousLatLng =  LatLng(location.latitude, location.longitude)
            }


        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        val bitmapDescriptor =  BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(mBaseActivity))
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap!!.addMarker(
          //    MarkerOptions ().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng)
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
            )
        else

            MarkerAnimation.animateMarkerToGB(
                currentLocationMarker!!,
                latLng,
                LatLngInterpolator.Spherical()
            )
        if(previousLatLng != null) {
            val rotation = MapUtils.getRotation(previousLatLng!!, latLng)
            if (!rotation.isNaN()) {
                currentLocationMarker?.rotation = rotation
            }
        }
       animateCamera(currentLocation)
    }


    @Override
    override fun onResume() {
        super.onResume()
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(mBaseActivity)
            startCurrentLocationUpdates()
        }
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProviderClient = null
        googleMap = null
    }


    override fun onStop() {
        super.onStop()
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

}