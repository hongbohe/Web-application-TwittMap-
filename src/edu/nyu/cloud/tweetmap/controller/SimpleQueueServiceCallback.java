package edu.nyu.cloud.tweetmap.controller;

import com.amazonaws.services.sqs.AmazonSQS;

public interface SimpleQueueServiceCallback {
	//msgID is tweetID which is statusID
	public void sendMessage(String message,String msgID);
}
