package edu.nyu.cloud.tweetmap.controller;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;

import edu.nyu.cloud.tweetmap.model.SNSMessage;
public enum SNSHelper {
	INSTANCE;
	
//	private AWSCredentials credentials = new ProfileCredentialsProvider("default")
//	.getCredentials();//= new BasicAWSCredentials(//insert ACCESS, //insert SECRET);
//	private AmazonSNSClient amazonSNSClient = new AmazonSNSClient(credentials);
	private AmazonSNSClient amazonSNSClient = AmazonWebServiceFactory.constructSNS();
	
	public void confirmTopicSubmission(SNSMessage message) {
		ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
		 							.withTopicArn(message.getTopicArn())
									.withToken(message.getToken());
		ConfirmSubscriptionResult resutlt = amazonSNSClient.confirmSubscription(confirmSubscriptionRequest);
		System.out.println("subscribed to " + resutlt.getSubscriptionArn());
		
	}
	
}