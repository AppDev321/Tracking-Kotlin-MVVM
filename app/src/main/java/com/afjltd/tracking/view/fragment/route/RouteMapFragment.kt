package com.afjltd.tracking.view.fragment.route


import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.afjltd.tracking.R
import com.afjltd.tracking.model.responses.InspectionCheckData
import com.afjltd.tracking.model.responses.Sheets
import com.afjltd.tracking.utils.AFJUtils
import com.afjltd.tracking.utils.LatLngInterpolator
import com.afjltd.tracking.utils.MapUtils
import com.afjltd.tracking.utils.MarkerAnimation
import com.afjltd.tracking.view.activity.NavigationDrawerActivity
import com.afjltd.tracking.view.fragment.vehicle_daily_inspection.PTSInspectionForm
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationRequest.create
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL


class RouteMapFragment : Fragment(), OnMapReadyCallback {

    var googleMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var currentLocationMarker: Marker? = null
    private var currentLocation: Location? = null
    private var firstTimeFlag = true
    private var previousLatLng: LatLng? = null


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
                // animateCamera(currentLocation!!)
                firstTimeFlag = false
            }
            showMarker(currentLocation!!)


        }
    }

    private var routeSheet: List<Sheets> = arrayListOf()

    private lateinit var mBaseActivity: NavigationDrawerActivity

    companion object {
        val argumentParams = "form_data"
        fun getInstance(listRoutes: List<Sheets>) = RouteMapFragment().apply {
            this.routeSheet = listRoutes
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        routeSheet =
            requireArguments().getParcelableArrayList<Sheets>(argumentParams) as List<Sheets>
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
       /*
            googleMap.uiSettings.isZoomControlsEnabled = true
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

        */

    }

    @SuppressLint("MissingPermission")
    fun startCurrentLocationUpdates() {
        val locationRequest = create()
        locationRequest.priority = PRIORITY_HIGH_ACCURACY
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
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    previousLatLng = LatLng(location.latitude, location.longitude)

                    val bounds = LatLngBounds.Builder()
                        .include(LatLng(location.latitude, location.longitude))
                        .include(
                            LatLng(
                                routeSheet.last().latitude!!,
                                routeSheet.last().longitude!!
                            )
                        )
                        .build()
                    googleMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                }
            }


        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        val bitmapDescriptor =
            BitmapDescriptorFactory.fromBitmap(MapUtils.getCarBitmap(mBaseActivity))
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
        if (previousLatLng != null) {
            val rotation = MapUtils.getRotation(previousLatLng!!, latLng)
            if (!rotation.isNaN()) {
                currentLocationMarker?.rotation = rotation
            }
        }
        animateCamera(currentLocation)

        showRouteChildMarkers()


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


    private fun showRouteChildMarkers() {


        for (childLocation in routeSheet) {
            val latLng =  LatLng(childLocation.latitude!!.toDouble(), childLocation.longitude!!.toDouble())
            googleMap!!.addMarker(
                MarkerOptions().position(latLng)
                    .title(childLocation.name.toString())
                    .snippet(childLocation.address.toString())
                    .flat(true)
               // .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.layout.custom_route_marker)))
            )
        }


        // Draw route between markers
        val waypoints = routeSheet.joinToString("|") { "${it.latitude},${it.longitude}" }

        val directionUrl = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${currentLocation!!.latitude},${currentLocation!!.longitude}" +
                "&destination=${routeSheet.last().latitude},${routeSheet.last().longitude}" +
                "&waypoints=$waypoints" +
                "&mode=driving" +
                "&key=${getString(R.string.map_key)}"


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = withContext(Dispatchers.IO) { URL(directionUrl).readText() }
                val colorsTxt: Array<String> =
                    mBaseActivity.resources.getStringArray(R.array.color_white_text)

                val routes = JSONObject(response).getJSONArray("routes")
                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")
                    val lineOptions = PolylineOptions()
                        .width(12f)
                        .color(Color.parseColor(colorsTxt[0]))
                        .add(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                        .addAll(decodePoly(points))
                        .add(LatLng(routeSheet.last().latitude!!, routeSheet.last().longitude!!))
                    withContext(Dispatchers.Main) {
                        googleMap!!.addPolyline(lineOptions)
                    }
                }
            } catch (e: Exception) {
                AFJUtils.writeLogs("Map Error occurred: $e")
            }
        }


    }


    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(p)
        }
        return poly
    }


    private fun getMarkerBitmapFromView(layoutResId: Int): Bitmap {
        val customMarkerView = LayoutInflater.from(mBaseActivity).inflate(layoutResId, null)
        val markerImageView = customMarkerView.findViewById<ImageView>(R.id.marker_image_view)
        markerImageView.setImageResource(R.drawable.avatar)
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        customMarkerView.layout(
            0,
            0,
            customMarkerView.measuredWidth,
            customMarkerView.measuredHeight
        )
        customMarkerView.isDrawingCacheEnabled = true
        customMarkerView.buildDrawingCache()
        val returnedBitmap = Bitmap.createBitmap(customMarkerView.drawingCache)
        customMarkerView.isDrawingCacheEnabled = false
        return returnedBitmap
    }

}
