package com.afjltd.tracking.view.fragment.home


import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afjltd.tracking.R
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.LatLngInterpolator
import com.afjltd.tracking.utils.MapUtils
import com.afjltd.tracking.utils.MarkerAnimation
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
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
    }

    @Override
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        val source = LatLng(31.490127, 74.316971) //starting point (LatLng)
        val destination = LatLng(31.474316, 74.316112) // ending point (LatLng)

    }

    @SuppressLint("MissingPermission")
    fun startCurrentLocationUpdates() {
        val locationRequest = create()
        locationRequest.priority =PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 3000
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )


    }

    private fun isGooglePlayServicesAvailable(): Boolean {
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

    @SuppressLint("MissingPermission")
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