package edu.nyu.cloud.tweetmap.model;

public class Sentiment {
	private Mood mood;
	private float confidence;

	public Sentiment() {
	}

	public Sentiment(Mood mood, float confidence) {
		this.mood = mood;
		this.confidence = confidence;
	}

	public Mood getMood() {
		return mood;
	}

	public float getConfidence() {
		return confidence;
	}

	public static Sentiment neutral() {
		return new Sentiment(Mood.NEUTRAL, 0f);
	}
}
