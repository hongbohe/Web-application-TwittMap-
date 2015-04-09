package edu.nyu.cloud.tweetmap.controller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class AmazonWebServiceFactory {
	private static AWSCredentials credentials = null;
	static {
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/Users/linghaoli/.aws/credentials), and is in valid format.",
					e);
		}
	}

	static AmazonDynamoDBClient constructDynamoDB() {
		AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(new InstanceProfileCredentialsProvider());//credentials);
		Region usEast1 = Region.getRegion(Regions.US_EAST_1);
		dynamoDB.setRegion(usEast1);
		return dynamoDB;
	}

	static AmazonSQS constructSQS() {
		AmazonSQS sqs  = new AmazonSQSClient(new InstanceProfileCredentialsProvider());//credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usEast1);
		return sqs;
	}
	
	static AmazonSNSClient constructSNS(){
		AmazonSNSClient snsClient = new AmazonSNSClient(new InstanceProfileCredentialsProvider());//credentials);
		snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		return snsClient;
	}
}
