package edu.nyu.cloud.tweetmap.controller;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import edu.nyu.cloud.tweetmap.model.Tweet;

public class getTweetStream {
	public static void main(String[] args) {
		SimpleQueueServiceHandler sqsHandler = new SimpleQueueServiceHandler(AmazonWebServiceFactory.constructSQS());
		DynamoDBHandler dbHandler = new DynamoDBHandler(AmazonWebServiceFactory.constructDynamoDB());
		AnalyzingTweetSink analyzeTweetSink = new AnalyzingTweetSink(new AlchemyApiSentimentAnalyzer(),2,"tweetMapTopic");
		
		sqsHandler.createQueue("tweetMapQueue");
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey("N9neDgM2Rp1twapXlh0cWSwB7");
		builder.setOAuthConsumerSecret("oQ5shlD45HZCnAyABG9nvD1TxyoDKOjMwDUk4OoMD262xWCW4r");
		builder.setOAuthAccessToken("2403237015-YqhkgKeKCV7Jt2NbGQYgsCsfjIB1dAK2rgevKEM");
		builder.setOAuthAccessTokenSecret("dtan0XyCM7VNCWrM36kAUjXoRQToHLF64IaUUBnu0Y92A");

		// Configuration
		Configuration conf = builder.build();

		// TwitterStream
		TwitterStream twitterStream = new TwitterStreamFactory(conf)
				.getInstance();
		StatusListener listener = new StatusListener() {
			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
				// System.out.println("Got a status deletion notice id:"
				// +statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// System.out.println("Got track limitation notice:"+
				// numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// System.out.println("Got scrub_geo event userId:" + userId +
				// " upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				// System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			@Override
			public void onStatus(Status status) {
				GeoLocation geo = null;
				geo = status.getGeoLocation();
				User user = status.getUser();

				if (geo != null) {
//					tweetInfo = new TweetInfo(String.valueOf(status.getId()),
//							user.getName(), status.getCreatedAt().toString(),
//							String.valueOf(geo.getLatitude()),
//							String.valueOf(geo.getLongitude()),
//							status.getText());
					String text = status.getId() + "\t" + user.getName() + "\t"
							+ status.getCreatedAt() + "\t" + geo.getLatitude()
							+ "\t" + geo.getLongitude() + "\t"
							+ status.getText();
					Tweet tweet = new Tweet(String.valueOf(status.getId()),
							user.getName(), status.getCreatedAt().toString(),
							String.valueOf(geo.getLatitude()),
							String.valueOf(geo.getLongitude()),
							status.getText());
					dbHandler.upload(tweet, sqsHandler);
					//queue.offer(tweetInfo);
					System.out.println(text);
				}
			}
		};
		twitterStream.addListener(listener);
//		FilterQuery fq = new FilterQuery();
//		fq.language(new String[]{"en"});
//		fq.locations(new double[][]{new double[]{-180,-90},
//                new double[]{180,90
//                }});
////		fq.locations(new double[][]{new double[]{-126.562500,30.448674},
////                new double[]{-61.171875,44.087585
////                }});
//		twitterStream.filter(fq);
		twitterStream.sample();
//		try {
//			Thread.currentThread().sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		analyzeTweetSink.initWorker(sqsHandler.getMyQueueUrl(), sqsHandler.getSqs());
	}
	
}
