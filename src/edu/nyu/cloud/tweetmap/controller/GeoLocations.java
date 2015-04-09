package edu.nyu.cloud.tweetmap.controller;

public class GeoLocations {
	private String latitude;
	private String longitude;
	private String content;
	public GeoLocations(String latitude, String longitude, String content) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

}
