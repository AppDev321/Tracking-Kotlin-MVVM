package com.example.afjtracking.view.fragment.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.afjtracking.R
import com.example.afjtracking.utils.LatLngInterpolator
import com.example.afjtracking.utils.MarkerAnimation
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MapsFragment : Fragment(), OnMapReadyCallback {

    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445
     var googleMap: GoogleMap?= null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLocationMarker: Marker? = null
    private var currentLocation: Location? = null
    private var firstTimeFlag = true

    private val clickListener =
        View.OnClickListener { view ->
               if (view.id == R.id.currentLocationImageButton
                   && googleMap != null
                   && currentLocation != null
               )
                   animateCamera(currentLocation!! )
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
        this.googleMap = googleMap;
    }

    fun startCurrentLocationUpdates() {
        val locationRequest = LocationRequest . create ()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
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
                    arrayOf( Manifest.permission.ACCESS_FINE_LOCATION ),
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                );
                return;
            }
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }
    fun  isGooglePlayServicesAvailable():Boolean
    {
        val googleApiAvailability = GoogleApiAvailability . getInstance ();
        val status = googleApiAvailability . isGooglePlayServicesAvailable (mBaseActivity);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(mBaseActivity, "Install google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }
    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ){
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(mBaseActivity, "Permission denied", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates();
        }
    }
   fun animateCamera(  location:Location)
    {
        val latLng =  LatLng(location.getLatitude(), location.getLongitude());
        googleMap!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                getCameraPositionWithBearing(
                    latLng
                )
            )
        );
    }

    fun  getCameraPositionWithBearing( latLng:LatLng):CameraPosition
    {
        return  CameraPosition . Builder ().target(latLng).zoom(17.0F).build();
    }
  fun showMarker(  currentLocation:Location)
    {


        val latLng =  LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap!!.addMarker(
                 MarkerOptions ().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng)
            )
        else


            MarkerAnimation.animateMarkerToGB(
                currentLocationMarker!!,
                latLng,
                 LatLngInterpolator. Spherical ()
            )
    }


    @Override
    override fun onResume()
    {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mBaseActivity);
            startCurrentLocationUpdates();
        }
    }
    @Override
    override fun onDestroy()
    {
        super.onDestroy();
        fusedLocationProviderClient = null;
        googleMap = null;
    }


    override fun onStop() {
        super.onStop()
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback);
    }

}