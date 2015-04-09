package edu.nyu.cloud.tweetmap.controller;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;

import edu.nyu.cloud.tweetmap.model.Tweet;

public class DynamoDBHandler {
	private AmazonDynamoDBClient dynamoDB;
	static final String tableName = "TwitterMapHW2";

	public DynamoDBHandler(AmazonDynamoDBClient dynamoDB) {
		super();
		this.dynamoDB = dynamoDB;
	}

	public void init() {

		try {
			// Create table if it does not exist yet
			if (Tables.doesTableExist(dynamoDB, tableName)) {
				System.out.println("Table " + tableName + " is already ACTIVE");
			} else {
				// Create a table with a primary hash key named 'name', which
				// holds a string
				CreateTableRequest createTableRequest = new CreateTableRequest()
						.withTableName(tableName)
						.withKeySchema(
								new KeySchemaElement()
										.withAttributeName("name").withKeyType(
												KeyType.HASH))
						.withAttributeDefinitions(
								new AttributeDefinition().withAttributeName(
										"name").withAttributeType(
										ScalarAttributeType.S))
						.withProvisionedThroughput(
								new ProvisionedThroughput()
										.withReadCapacityUnits(1L)
										.withWriteCapacityUnits(1L));
				TableDescription createdTableDescription = dynamoDB
						.createTable(createTableRequest).getTableDescription();
				System.out.println("Created Table: " + createdTableDescription);

				// Wait for it to become active
				System.out.println("Waiting for " + tableName
						+ " to become ACTIVE...");
				Tables.waitForTableToBecomeActive(dynamoDB, tableName);
			}
		} catch (AmazonServiceException ase) {
			System.out
					.println("Caught an AmazonServiceException, which means your request made it "
							+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
					.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	public void upload(Tweet tweet, SimpleQueueServiceCallback sqsCallback) {
		try {
			PutItemRequest putItemRequest;
			PutItemResult putItemResult;
			Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
			item.put("TwitterID", new AttributeValue(tweet.getTweetID()));
			item.put("User", new AttributeValue(tweet.getUserName()));
			item.put("CreatedAt", new AttributeValue(tweet.getCreateAt()));
			item.put("Latitude", new AttributeValue(tweet.getLatitude()));
			item.put("Longitude", new AttributeValue(tweet.getLongitude()));
			item.put("Content", new AttributeValue(tweet.getContent()));
			putItemRequest = new PutItemRequest(tableName, item);
			putItemResult = dynamoDB.putItem(putItemRequest);
			//System.out.println("Result: " + putItemResult);
			sqsCallback.sendMessage(tweet.getContent(), tweet.getTweetID());//tweet.getContent());//Need to change back to ID
		} catch (AmazonServiceException ase) {
			System.out
					.println("Caught an AmazonServiceException, which means your request made it "
							+ "to AWS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out
					.println("Caught an AmazonClientException, which means the client encountered "
							+ "a serious internal problem while trying to communicate with AWS, "
							+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	
}
