package edu.agh.wsserver.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;
import edu.agh.wsserver.utils.dto.SensorDto;

public class DeviceUtils {
	public static final String LOG_TAG = "DeviceUtils";

	private DeviceUtils(){}

	private static Context ctx;

	/**
	 * Provides functionality for fetching device sensors info
	 */
	public static final SensorDto[] getDeviceSensors(){
		if (ctx == null) {
			Log.e(LOG_TAG, "Context is null");
			return null;
		}
		SensorManager sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

		if (sensorManager == null) {
			Log.e(LOG_TAG, "SensorManager not available");
			return null;
		}

		List<SensorDto> sensorDtos = new ArrayList<SensorDto>();
		for (Sensor s : sensorManager.getSensorList(Sensor.TYPE_ALL)) {
			sensorDtos.add(new SensorDto(s.getName(), s.getVendor(), s.getVersion(), s.getType(), s.getMaximumRange(), s.getResolution(), s.getPower(), s.getMinDelay()));
		}
		return sensorDtos.toArray(new SensorDto[sensorDtos.size()]);
	}

	public static void setContext(Context ctx) {
		DeviceUtils.ctx = ctx;
	}
}