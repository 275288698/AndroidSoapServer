package edu.agh.wsserver.utils.dto;

public class LocationDto {

	public final double latitude;
	public final double longitude;

	public LocationDto(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "LocationDto [latitude=" + latitude + ", longitude=" + longitude + "]";
	}
}