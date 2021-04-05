package com.app.ekottel.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import static com.app.ekottel.utils.GlobalVariables.LOG;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ekottel.R;
import com.app.ekottel.adapter.PlaceAutocompleteAdapter;
import com.app.ekottel.utils.ChatConstants;
import com.app.ekottel.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This activity is used to transfer balance.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ChatMapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener, OnMapReadyCallback {
    private static final String TAG ="ChatMapActivity" ;
    private GoogleMap mMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double mUpdatedLatitude;
    private double mUpdatedLongitude;
    private String mAddress;
    private Marker m1;
    TextView mTvLocationDone;
    public double latit;
    public double lngit;
    private PlaceAutocompleteAdapter mPlaceAutoCompleteAdapter;
    private AutoCompleteTextView mLocationSearchEdit;
    private TextView mSearchIcon;
    private GeoDataClient mGeoDataClient;
    private boolean isLocationConnectedCalled = false;
    private ImageView mSearchClearButton;
    private int PLACE_PICKER_REQUEST = 1;
    Marker mCurrLocationMarker;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71, 136));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_map);

        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);


        Typeface webTypeFace = Utils.getTypeface(getApplicationContext());
        LinearLayout ll_back = (LinearLayout) findViewById(R.id.ll_location_map_back_arrow);
        TextView back = (TextView) findViewById(R.id.tv_location_map_back_arrow);
        TextView tv_location_header = (TextView) findViewById(R.id.tv_location_transfer_header);
        mTvLocationDone = (TextView) findViewById(R.id.tv_location_done);
        mLocationSearchEdit = findViewById(R.id.ed_search_location_map);
        mSearchClearButton = findViewById(R.id.iv_search_text_clear_map);


        mLocationSearchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                ) {
                    LOG.debug("onEditorAction: called");
                    geoLocateMap();
                    hideKeyboardFrom();
                    return true;
                }

                return false;
            }
        });
        mSearchClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearText();
            }
        });

        // Listener for AutoCompleteTextView Items click
        mLocationSearchEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                geoLocateMap();
                hideKeyboardFrom();
            }
        });




        ll_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        back.setTypeface(webTypeFace);
        mTvLocationDone.setTypeface(webTypeFace);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(ChatMapActivity.this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        mTvLocationDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationDialog();
            }
        });

        mGeoDataClient = Places.getGeoDataClient(ChatMapActivity.this, null);
        mPlaceAutoCompleteAdapter = new PlaceAutocompleteAdapter(ChatMapActivity.this, mGeoDataClient, LAT_LNG_BOUNDS, null);
        mLocationSearchEdit.setAdapter(mPlaceAutoCompleteAdapter);
// Listener for EditText AddText...
        mLocationSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (editable.length() > 0) {
                    mSearchClearButton.setVisibility(View.VISIBLE);
                } else {
                    mSearchClearButton.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     */
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        } else {
            //If everything went fine lets get latitude and longitude
            mUpdatedLatitude = location.getLatitude();
            mUpdatedLongitude = location.getLongitude();

            LatLng point = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mAddress=getAddress(point);
            m1 = mMap
                    .addMarker(new MarkerOptions()
                            .position(point)
                            .title(getString(R.string.edit_profile_map_current_location_message))
                            // .snippet("My Location")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location
                            .getLongitude()), 15.0f));


        }
    }

    public String getAddress(LatLng latLng) {
        String retaddress = "";
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

            if (address != null) {
                retaddress = address;
            }

            //String city = addresses.get(0).getLocality();
            //String state = addresses.get(0).getAdminArea();
            //String country = addresses.get(0).getCountryName();
            //String postalCode = addresses.get(0).getPostalCode();
            //String knownName = addresses.get(0).getFeatureName();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return retaddress;
    }

    //  Method for search Location....
    private void geoLocateMap() {

        LOG.debug("geoLocateMap: called");

        try {

            String searchText = mLocationSearchEdit.getText().toString();

            Geocoder geocoder = new Geocoder(ChatMapActivity.this);
            List<Address> list = new ArrayList<>();

            try {
                list = geocoder.getFromLocationName(searchText, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (list.size() > 0) {
                Address address = list.get(0);
                LOG.debug("geoLocateMap: called and Location: " + address.toString());

                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(searchText));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

               // moveCameraAndSetLocation(latLng, 15.0f, address.getLocality());
            }else {
            Toast.makeText(getApplicationContext(), "Address Not Found",
                    Toast.LENGTH_SHORT).show();
        }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    // Method for set Camera position on Map...
    private void moveCameraAndSetLocation(LatLng latLng, float zoom, String locality) {

        LOG.debug("moveCameraAndSetLocation: called");
        try {
            mMap.clear();

            m1 = mMap
                    .addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(locality.toString())
                            // .snippet("My Location")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            mUpdatedLatitude = m1.getPosition().latitude;
            mUpdatedLongitude = m1.getPosition().longitude;
            LatLng point = new LatLng(m1.getPosition().latitude,
                    m1.getPosition().longitude);
            mAddress=getAddress(point);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        }
    }

    private void hideKeyboardFrom() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mLocationSearchEdit.getWindowToken(), 0);
    }
    private void clearText() {
        if (mLocationSearchEdit != null && mLocationSearchEdit.getText().toString().length() > 0) {
            mLocationSearchEdit.setText("");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
      Location  mLastLocation = location;
        if ( mCurrLocationMarker!= null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.clear();
        m1 = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(getString(R.string.edit_profile_map_set_location_message))
                .snippet("")
                .draggable(true)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mUpdatedLatitude = m1.getPosition().latitude;
        mUpdatedLongitude = m1.getPosition().longitude;
        LatLng point = new LatLng(m1.getPosition().latitude,
                m1.getPosition().longitude);
        mAddress=getAddress(point);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mUpdatedLatitude = marker.getPosition().latitude;
        mUpdatedLongitude = marker.getPosition().longitude;
        LatLng point = new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude);
        mAddress=getAddress(point);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mUpdatedLatitude = marker.getPosition().latitude;
        mUpdatedLongitude = marker.getPosition().longitude;
        LatLng point = new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude);
        mAddress=getAddress(point);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }



    private void showLocationDialog() {
        final Dialog dialog = new Dialog(ChatMapActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.location_alert_dialog);
        // Custom Android Allert Dialog Title
        TextView dialogButtonCancel = (TextView) dialog.findViewById(R.id.btn_location_cancel);
        TextView dialogButtonOk = (TextView) dialog.findViewById(R.id.btn_location_ok);
        // Click cancel to dismiss android custom dialog box
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        // Your android custom dialog ok action
        // Action for custom dialog ok button click
        dialogButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent();
                i.putExtra(ChatConstants.INTENT_LOCATION_LATITUDE,mUpdatedLatitude);
                i.putExtra(ChatConstants.INTENT_LOCATION_LONGITUDE,mUpdatedLongitude);
                i.putExtra(ChatConstants.INTENT_LOCATION_ADDRESS,mAddress);
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        });

        dialog.show();

    }

    // Parant view Click Listener Method for hide SoftKeyboard
    public void parantViewCliecked(View view) {
        hideKeyboardFrom();
    }

    // My Location Click Listener Method
    public void relocateToMyLocation(View view) {
        myLocation();
    }

    // Method for set My Location...
    private void myLocation() {
        mMap.clear();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            mUpdatedLatitude = location.getLatitude();
            mUpdatedLongitude = location.getLongitude();
            LatLng point = new LatLng(location.getLatitude(),
                    location.getLongitude());

            mAddress=getAddress(point);
            LOG.debug("onConnected: called and marker is: " + m1);

            m1 = mMap
                    .addMarker(new MarkerOptions()
                            .position(point)
                            .title(getString(R.string.edit_profile_map_current_location_message))
                            // .snippet("My Location")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location
                            .getLongitude()), 15.0f));

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);


     /*   if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }*/


    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        LOG.debug("onActivityResult: called");

        try {
            if (requestCode == PLACE_PICKER_REQUEST) {
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, this);
                    LOG.debug("onActivityResult: called and place: " + place.getName());
                    if (place != null)
                        moveCameraAndSetLocation(place.getLatLng(), 15.0f, place.getName().toString());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
