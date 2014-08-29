package edu.agh.wsserver.utils.location;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import edu.agh.wsserver.utils.dto.LocationDto;

public class LocationUtil {

	private LocationUtil() {}
	private static final LocationUtil instance = new LocationUtil();

	public static final String LOG_TAG = "GetCurrentLocationUtil";

	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private volatile boolean gpsEnabled = false;
	private volatile LocationDto currentLocation = null;
	private boolean isInitialized = false;

	public void init(Context ctx) {
		locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		gpsEnabled = checkGpsStatus(ctx);
		locationListener = new MyLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 2, locationListener);
		isInitialized = true;
	}

	private Boolean checkGpsStatus(Context ctx) {
		ContentResolver contentResolver = ctx.getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
		return gpsStatus;
	}

	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			currentLocation = new LocationDto(loc.getLatitude(), loc.getLongitude());
			if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
				Log.d(LOG_TAG, "Location changed: " + currentLocation);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			gpsEnabled = false;
			Log.i(LOG_TAG, "GPS is disabled.");
		}

		@Override
		public void onProviderEnabled(String provider) {
			gpsEnabled = true;
			Log.i(LOG_TAG, "GPS is enabled.");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public static LocationUtil getInstance() {
		return instance;
	}

	public static LocationDto getCurrentLocation() {
		if (instance.gpsEnabled && instance.isInitialized) {
			return instance.currentLocation;
		} else {
			String msg = !instance.isInitialized ? "Util not initialized" : "GPS disabled"; 
			Log.w(LOG_TAG, msg);
			return null;
		}
	}
}