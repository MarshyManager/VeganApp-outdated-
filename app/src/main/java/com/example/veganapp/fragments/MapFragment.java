package com.example.veganapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.veganapp.support_classes.OnMapAndViewReadyListener;
import com.example.veganapp.R;
import com.example.veganapp.db_classes.Restaurant;
import com.example.veganapp.support_classes.PermissionUtils;
import com.example.veganapp.support_classes.StringFormatter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MapFragment extends Fragment implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;
    protected List<Restaurant> restaurants;
    GoogleMap mMap;
    private Marker mLastSelectedMarker;
    private FusedLocationProviderClient fusedLocationClient;
    private final List<Place> places = new ArrayList<>();
    private final List<Marker> mMarkers = new ArrayList<>();
    PlacesClient placesClient;


    public MapFragment() {
    }

    public static MapFragment newInstance(List<Restaurant> restaurants) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        fragment.restaurants = restaurants;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment =
                ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        new OnMapAndViewReadyListener(mapFragment, this);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PRICE_LEVEL,
                Place.Field.PHOTO_METADATAS);
        placesClient = Places.createClient(getActivity());
        for (int i = 0; i < restaurants.size(); ++i) {
            String placeId = restaurants.get(i).getId();
            FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
            placesClient.fetchPlace(request).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                @Override
                public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                    places.add(fetchPlaceResponse.getPlace());
                    if (places.size() == restaurants.size())
                        addMarkersToMap();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        // Setting an info window adapter allows us to change the both the contents and look of the
        // info window.
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

        enableMyLocation();
    }

    private void addMarkersToMap() {
        for (int i = 0; i < places.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(places.get(i).getLatLng())
                    .title(places.get(i).getName())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_for_map)));
            mMarkers.add(marker);
            if (i == 0)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(places.get(i).getLatLng(), 12.0f));
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        mLastSelectedMarker = marker;

        return false;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mContents;

        CustomInfoWindowAdapter() {
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_marker_content, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents, mMarkers.indexOf(marker));
            return mContents;
        }

        private void render(Marker marker, View view, int pos) {

            TextView titleUi = view.findViewById(R.id.place_title);
            titleUi.setText(places.get(pos).getName());
            TextView openHours = view.findViewById(R.id.place_open_hours);
            int currentCalendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            Period period = places.get(pos).getOpeningHours().getPeriods().get(currentCalendarDay);
            openHours.setText(StringFormatter.openHours(period));
            TextView rating = view.findViewById(R.id.place_rating);
            if (places.get(pos).getRating() != null)
                rating.setText(places.get(pos).getRating().toString());
            else
                rating.setText("...");
            TextView priceLevel = view.findViewById(R.id.place_price_level);
            if (places.get(pos).getPriceLevel() != null)
                priceLevel.setText(places.get(pos).getPriceLevel().toString() + "/5");
            else
                priceLevel.setText("...");
        }
    }
}
