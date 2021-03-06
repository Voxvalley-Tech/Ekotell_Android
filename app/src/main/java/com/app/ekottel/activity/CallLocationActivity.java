package com.app.ekottel.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.utils.AppConstants;
import com.app.ekottel.utils.Utils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.multidex.MultiDex;

import static com.app.ekottel.utils.GlobalVariables.LOG;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

//import com.google.android.maps.GeoPoint;


public class CallLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

	private GoogleMap googleMap;
	Toolbar toolbar;
	ImageView pinimage;
	TextView locationaddressview;
	RelativeLayout sharefinallocationview;


	public static Double final_lat = 0.0;
	public static Double final_lng = 0.0;
	public static String final_address = "";
	public static String callcontext = "";

	public static Double remote_final_lat = 0.0;
	public static Double remote_final_lng = 0.0;
	public static String remote_final_address = "";
	public static String remote_callcontext = "";


	private FusedLocationProviderClient mFusedLocationProviderClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_location_layout);
		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());


		toolbar = (Toolbar) findViewById(R.id.toolbar);
		pinimage = (ImageView) findViewById(R.id.pin);
		locationaddressview = (TextView) findViewById(R.id.textView4);
		sharefinallocationview = (RelativeLayout) findViewById(R.id.rlv1);
		Button startcallview = (Button) findViewById(R.id.startcall);
		EditText contextview = (EditText) findViewById(R.id.context);

		toolbar.setTitle("Konverz");
		toolbar.setSubtitle("Location");
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		contextview.setText(callcontext);
		startcallview.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LOG.info("Yes closing location");
				callcontext = contextview.getText().toString();

				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppConstants.INTENT_LOCATION_LATITUDE, final_lat);
				resultIntent.putExtra(AppConstants.INTENT_LOCATION_LONGITUDE, final_lng);
				resultIntent.putExtra(AppConstants.INTENT_LOCATION_ADDRESS, final_address);
				resultIntent.putExtra("callcontext", callcontext);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();

			}
		});


		MapFragment mapFragment = (MapFragment) getFragmentManager()
				.findFragmentById(R.id.map);

		mapFragment.getMapAsync(this);


	}

	@Override
	public void onBackPressed() {
		try {
			Intent resultIntent = new Intent();
			setResult(Activity.RESULT_CANCELED, resultIntent);
			finish();
		} catch (Exception ex) {
		}
	}

	@Override
	public void onMapReady(GoogleMap map) {

		googleMap = map;

		setUpMap();

	}

	public void setUpMap() {
		try {
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
			googleMap.setMyLocationEnabled(true);
			googleMap.setTrafficEnabled(true);
			googleMap.setIndoorEnabled(true);
			googleMap.setBuildingsEnabled(true);
			googleMap.getUiSettings().setZoomControlsEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);
			googleMap.setMinZoomPreference(14.0f);
			getDeviceLocation();


			//getLastLocation();

			googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
				@Override
				public void onCameraIdle() {
					LOG.info("onCameraIdle idle");

					//pinimage.animate().cancel();
					//pinimage.setForegroundGravity(0);

					LatLng latLng = googleMap.getCameraPosition().target;
					String address = getAddress(latLng);

					final_lat = latLng.latitude;
					final_lng = latLng.longitude;
					final_address = address;


					DecimalFormat newFormat = new DecimalFormat("#.####");

					toolbar.setSubtitle("Location:lat:" + newFormat.format(latLng.latitude) + " lng:" + newFormat.format(latLng.longitude));
					LOG.info("location.getLatitude() : " + latLng.latitude);
					LOG.info("location.getLongitude() : " + latLng.longitude);

					LOG.info("location address : " + address);
					locationaddressview.setText(address);


				}
			});

			googleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
				@Override
				public void onCameraMoveStarted(int var) {
					LOG.info("onCameraMoveStarted move start");
					//pinimage.animate().y(0.1f);


				}
			});
			googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
				@Override
				public void onCameraMove() {
					LOG.info("onCameraMove moving");
				}
			});
			googleMap.setOnCameraMoveCanceledListener(new GoogleMap.OnCameraMoveCanceledListener() {
				@Override
				public void onCameraMoveCanceled() {
					LOG.info("onCameraMoveCanceled stopped");
				}
			});



/*
			googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(0,0))
					.draggable(true)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

			googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {

				@Override
				public void onMarkerDragStart(Marker marker) {
					LOG.info("Draagging start");
				}

				@Override
				public void onMarkerDragEnd(Marker marker) {
					LOG.info("Draagging end");
					toolbar.setSubtitle("Location:lat:" + Double.toString(marker.getPosition().latitude) + " lng:" + Double.toString(marker.getPosition().longitude));
					LOG.info("Marker location.getLatitude() : " + marker.getPosition().latitude);
					LOG.info("Marker location.getLongitude() : " + marker.getPosition().longitude);

				}

				@Override
				public void onMarkerDrag(Marker marker) {
					LOG.info("Draagging");
				}
			});
*/


		} catch (Exception ex) {
			Utils.logStacktrace(ex);
		}

	}


	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}


	public void getLastLocation() {
		try {
			// Get last known recent location using new Google Play Services SDK (v11+)
			FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

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
			locationClient.getLastLocation()
					.addOnSuccessListener(new OnSuccessListener<Location>() {
						@Override
						public void onSuccess(Location location) {
							// GPS location can be null if GPS is switched off
							if (location != null) {
								onLocationChanged(location);
							}
						}
					})
					.addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							LOG.info("MapDemoActivity Error trying to get last GPS location");
							Utils.logStacktrace(e);
						}
					});
		} catch (Exception ex) {
			Utils.logStacktrace(ex);
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
		}


		return retaddress;
	}

	public void onLocationChanged(Location location) {
		try {
			// New location has now been determined
			//String msg = "Updated Location: " +
			//		Double.toString(location.getLatitude()) + "," +
			//		Double.toString(location.getLongitude());
			//Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			// You can now create a LatLng Object for use with maps


			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
			googleMap.animateCamera(zoom);





			/*googleMap.addMarker(new MarkerOptions()
					.position(latLng)
					.title("IamLive")
					.draggable(true)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
*/

/*
		String mTitle = "IamLive";
		String geoUri = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude() + " (" + mTitle + ")";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);

		*/

		} catch (Exception ex) {
			Utils.logStacktrace(ex);
		}
	}




/*
	protected void startLocationUpdates() {
		LocationRequest mLocationRequest;

		long UPDATE_INTERVAL = 10 * 1000;
		long FASTEST_INTERVAL = 2000;
		// Create the location request to start receiving updates
		mLocationRequest = new LocationRequest();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Create LocationSettingsRequest object using location request
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
		builder.addLocationRequest(mLocationRequest);
		LocationSettingsRequest locationSettingsRequest = builder.build();

		// Check whether location settings are satisfied
		// https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
		SettingsClient settingsClient = LocationServices.getSettingsClient(this);
		settingsClient.checkLocationSettings(locationSettingsRequest);

		// new Google API SDK v11 uses getFusedLocationProviderClient(this)
		getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
					@Override
					public void onLocationResult(LocationResult locationResult) {
						// do work here
						onLocationChanged(locationResult.getLastLocation());
					}
				},
				Looper.myLooper());
	}
*/


	private void getDeviceLocation() {
		try {
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
			Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
			locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
				@Override
				public void onComplete(@NonNull Task<Location> task) {
					if (task.isSuccessful()) {
						// Set the map's camera position to the current location of the device.
						Location location = task.getResult();
						LatLng currentLatLng = new LatLng(location.getLatitude(),
								location.getLongitude());
						CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,
								14.0f);
						googleMap.moveCamera(update);
					}
				}
			});

		} catch (Exception e) {
			Log.e("Exception: %s", e.getMessage());
		}
	}



}