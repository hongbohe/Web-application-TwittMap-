package edu.nyu.cloud.tweetmap.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import edu.nyu.cloud.tweetmap.model.Sentiment;

public class AnalyzingTweetSink {

	private SentimentAnalyzer analyzer;
	private final ExecutorService executor;
	private final AmazonSNSClient snsClient = AmazonWebServiceFactory.constructSNS();
	private int numOfWorkers;
	private final String  topicArn;
	public void initWorker(String myQueueUrl, AmazonSQS sqs) {
		for (int i = 0; i < numOfWorkers; i++) {
			executor.submit(new AnalyzeWorker(sqs, myQueueUrl));
		}
	}

	public AnalyzingTweetSink(SentimentAnalyzer analyzer, int numOfWorkers,String topicName) {
		super();
		this.analyzer = analyzer;
		this.numOfWorkers = numOfWorkers;
		this.executor = Executors.newFixedThreadPool(numOfWorkers);
		//create a new SNS topic
		CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
		CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
		this.topicArn = createTopicResult.getTopicArn();
		System.err.println("CreateTopicRequest");//tweetMapUniqueURL  SNSServiceServlet
		SubscribeRequest subsRequest = new SubscribeRequest(topicArn, "http", "http://twittermaphw2test1.elasticbeanstalk.com/SNSServiceServlet");//216.165.95.74:8080/TwitterMapHW2/SNStestServlet");//tweetmapuniqueurl.elasticbeanstalk.com/SNSServiceServlet");//SnsServlet");
		SubscribeResult subsResult = snsClient.subscribe(subsRequest);
		for(int i = 0; i < 10; ++i) System.err.println("subscribed to topic with id" + subsResult.getSubscriptionArn());
		System.err.println("ReceiveSubscribe "+ subsResult.getSubscriptionArn());
//		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", "http://localhost:8080/TwitterMapHW2/SNSServiceServlet");
//		snsClient.subscribe(subRequest);
	}

	class AnalyzeWorker implements Runnable {
		private AmazonSQS sqs;
		private String myQueueUrl;

		public AnalyzeWorker(AmazonSQS sqs, String myQueueUrl) {
			super();
			this.sqs = sqs;
			this.myQueueUrl = myQueueUrl;
		}

		@Override
		public void run() {
			while (true) {
				ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
						myQueueUrl).withMessageAttributeNames("All")
						.withAttributeNames("");//.withWaitTimeSeconds(5);
				List<Message> messages = sqs.receiveMessage(
						receiveMessageRequest).getMessages();
				if(messages == null) continue;
				//System.err.println("messages has: "+messages.size());
				for (Message message : messages) {
					//System.err.println("body: "+message.getBody());
					String messageRecieptHandle = message.getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageRecieptHandle));
					System.err.println("Message: "+message.getMessageAttributes().get("tweetID").getStringValue()+" deleted");
					Sentiment sentiment = analyzer.analyze(message.getBody());
					String msg = message.getMessageAttributes().get("tweetID").getStringValue();
					if(sentiment != null) {
						System.err.println(message.getMessageAttributes().get("tweetID").getStringValue()+"\tMood: "+sentiment.getMood()+"\tConfidence: "+sentiment.getConfidence());
						msg = msg+" "+sentiment.getMood()+" "+sentiment.getConfidence();
					}
					else {
						System.err.println("No sentiment");
						msg = msg+" badMood 0";
					}
					PublishRequest publishRequest = new PublishRequest(topicArn, msg);
					PublishResult publishResult = snsClient.publish(publishRequest);
					//print MessageId of message published to SNS topic
					System.out.println("MessageId - " + publishResult.getMessageId());
					// System.out.println(
					// "tweetID: "+message.getMessageAttributes().get("tweetID").getStringValue());
				}
				// Sentiment sentiment = analyzer.analyze();

				// if (sentiment != null) {
				// .setSentiment(sentiment);
				// }
			}
		}
	}

}
