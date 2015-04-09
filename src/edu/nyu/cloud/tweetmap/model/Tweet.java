package edu.nyu.cloud.tweetmap.model;

public class Tweet {
	private String tweetID;
	private String userName;
	private String createAt;
	private String latitude;
	private String longitude;
	private String content;
	private Sentiment sentiment;

	public Tweet() {

	}

	public Tweet(String tweetID, String userName, String createAt,
			String latitude, String longitude, String content) {
		super();
		this.tweetID = tweetID;
		this.userName = userName;
		this.createAt = createAt;
		this.latitude = latitude;
		this.longitude = longitude;
		this.content = content;
		this.sentiment = Sentiment.neutral();
	}

	public Sentiment getSentiment() {
		return sentiment;
	}

	public void setSentiment(Sentiment sentiment) {
		this.sentiment = sentiment;
	}

	public String getTweetID() {
		return tweetID;
	}

	public void setTweetID(String tweetID) {
		this.tweetID = tweetID;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCreateAt() {
		return createAt;
	}

	public void setCreateAt(String createAt) {
		this.createAt = createAt;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
