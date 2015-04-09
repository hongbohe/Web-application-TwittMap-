package edu.nyu.cloud.tweetmap.controller;

import edu.nyu.cloud.tweetmap.model.Sentiment;

public interface SentimentAnalyzer {

	public Sentiment analyze(String text);

}