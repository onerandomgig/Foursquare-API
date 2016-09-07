package in.continuousloop.winnie.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.jakewharton.rxrelay.PublishRelay;

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
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "WN/WinnieMapViewFrgmt";

    private static final float MAP_DEFAULT_ZOOM = 14.0f;
    private static final Integer MAP_STATE_IDLE = 0;
    private static final Integer MAP_STATE_MOVE = 1;

    @BindView(R.id.map) MapView mapView;
    @BindView(R.id.map_refresh_progress) ProgressBar refreshProgress;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    private Marker currentlyTappedMarker;

    private boolean permissionGranted;
    private PublishRelay<Integer> mapMovedRelay;
    private PublishRelay<Integer> refreshProgressRelay;

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

        refreshProgress.setMax(100);
        refreshProgress.setProgress(0);

        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        permissionGranted = false;
        mapMovedRelay = PublishRelay.create();
        refreshProgressRelay = PublishRelay.create();

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

        // Configure map view settings
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setScrollGesturesEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);

        // Setup map gesture listeners
        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnCameraIdleListener(this);
        googleMap.setOnCameraMoveListener(this);
        googleMap.setOnMarkerClickListener(this);

        // Refresh locations when user moves the map
        mapMovedRelay.startWith(MAP_STATE_IDLE)
                .map(state -> state == MAP_STATE_IDLE)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isStoppedMoving -> {
                    if (isStoppedMoving) {
                        googleMap.clear();
                        VisibleRegion vr = googleMap.getProjection().getVisibleRegion();
                        _refreshMapMarkers(vr.latLngBounds.getCenter(), googleMap.getCameraPosition().zoom, false);
                    }
                }, error -> {
                    Log.e(TAG, "Error when refreshing map");
                });

        refreshProgressRelay.startWith(0)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(progress -> {
                    refreshProgress.setProgress(progress);

                    if (progress == 100) {
                        refreshProgress.setProgress(0);
                    }
                });

        _enableMyLocation();
    }

    @Override
    public boolean onMyLocationButtonClick() {

        // Clear all markers
        googleMap.clear();

        // Make the api call to fetch venues and refresh markers
        LatLng latLng = _getCurrentUserLocation();
        if (latLng != null) {
            _refreshMapMarkers(latLng, MAP_DEFAULT_ZOOM, true);
        }

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (currentlyTappedMarker != null) {
            currentlyTappedMarker.hideInfoWindow();
        }

        currentlyTappedMarker = marker;
        currentlyTappedMarker.showInfoWindow();
        return true;
    }

    @Override
    public void onCameraIdle() {
        mapMovedRelay.call(MAP_STATE_IDLE);
    }

    @Override
    public void onCameraMove() {
        mapMovedRelay.call(MAP_STATE_MOVE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LatLng latLng = _getCurrentUserLocation();
        if (latLng != null) {
            _refreshMapMarkers(latLng, MAP_DEFAULT_ZOOM, true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Handle connection suspended
    }

    /**
     * Refresh map after permissions are granted
     */
    public void locationPermissionsGranted() {
        permissionGranted = true;
        googleApiClient.connect();

        _enableMyLocation();
    }

    /**
     * Refresh map markers with venues at current location.
     * TODO: handle permissions checks more cleanly
     */
    private void _refreshMapMarkers(LatLng latLng, float zoom, boolean moveCamera) {

        refreshProgressRelay.call(60);
        APIManager.getInstance().getVenuesAtLocation(latLng.latitude, latLng.longitude, refreshProgressRelay)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(venues -> {

                    LatLng location;
                    for (FourSquareVenue venue: venues) {
                        location = new LatLng(venue.getLatitude(), venue.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(location).title(venue.getName()).snippet(venue.getCategoryName()));
                    }

                    refreshProgressRelay.call(90);

                    if (moveCamera) {
                        location = new LatLng(venues.get(0).getLatitude(), venues.get(0).getLongitude());
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom));
                    }

                    refreshProgressRelay.call(100);
                }, error -> {
                    Crashlytics.logException(error);
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

    /**
     * Get the current location of the user
     * @return {@link LatLng}
     */
    @Nullable
    private LatLng _getCurrentUserLocation() {
        if (!permissionGranted) {
            return null;
        }

        Location userLocation;
        try {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (userLocation == null) {
                return null;
            } else {
                return new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Permissions not granted to fetch location");
            Toast.makeText(getActivity(), R.string.permission_required_toast,
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}