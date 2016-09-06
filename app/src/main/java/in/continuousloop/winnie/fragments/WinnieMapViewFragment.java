package in.continuousloop.winnie.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.continuousloop.winnie.R;
import in.continuousloop.winnie.api.APIManager;
import in.continuousloop.winnie.constants.AppConstants;
import in.continuousloop.winnie.model.FourSquareVenue;
import in.continuousloop.winnie.utils.PermissionsManager;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A fragment to show winnie locations on a map view.
 */
public class WinnieMapViewFragment extends Fragment
        implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks {

    @BindView(R.id.map)
    MapView mapView;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    private boolean permissionGranted;

    private static final String TAG = "WN/WinnieMapViewFrgmt";

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.bind(this, rootView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        permissionGranted = false;
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (permissionGranted) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Configure map view settings
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        googleMap.setOnMyLocationButtonClickListener(this);
        _enableMyLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {

        // Clear all markers
        googleMap.clear();

        // Make the api call to fetch venues and refresh markers
        _refreshMapMarkers();

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    /**
     * Refresh map after permissions are granted
     */
    public void locationPermissionsGranted() {
        permissionGranted = true;
        googleApiClient.connect();

        _enableMyLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        _refreshMapMarkers();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Handle connection suspended
    }

    /**
     * Refresh map markers with venues at current location.
     * TODO: handle permissions checks more cleanly
     */
    private void _refreshMapMarkers() {

        if (!permissionGranted) {
            return;
        }

        Location userLocation;
        try {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (userLocation == null) {
                return;
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Permissions not granted to fetch location");
            Toast.makeText(getActivity(), R.string.permission_required_toast,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        APIManager.getInstance().getVenuesAtLocation(userLocation.getLatitude(), userLocation.getLongitude())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venues -> {

                    LatLng location;
                    for (FourSquareVenue venue: venues) {
                        location = new LatLng(venue.getLatitude(), venue.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(location).title(venue.getName()).snippet(venue.getCategoryName()));
                    }

                    location = new LatLng(venues.get(0).getLatitude(), venues.get(0).getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));
                });
    }

    /**
     * Check if permissions have already been granted to access location. If not, ask for permissions
     */
    private void _enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionsManager.requestPermission((AppCompatActivity)getActivity(), AppConstants.LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            // Access to the location has been granted to the app.
            googleMap.setMyLocationEnabled(true);
            permissionGranted = true;
            googleApiClient.connect();
        }
    }
}