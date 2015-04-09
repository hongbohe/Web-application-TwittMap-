package edu.nyu.cloud.tweetmap.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

@WebServlet("/TwitterStreamServlet")
public class TwitterStreamServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected BlockingQueue<Tweet> queue = null;
	Tweet tweetInfo = null;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		// String initial = config.getInitParameter("initial");
		SimpleQueueServiceHandler sqsHandler = new SimpleQueueServiceHandler(AmazonWebServiceFactory.constructSQS());
		DynamoDBHandler dbHandler = new DynamoDBHandler(AmazonWebServiceFactory.constructDynamoDB());
		AnalyzingTweetSink analyzeTweetSink = new AnalyzingTweetSink(new AlchemyApiSentimentAnalyzer(),2,"tweetMapTopic");
		sqsHandler.createQueue("tweetMapQueue");
		queue = new ArrayBlockingQueue<Tweet>(1000);
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
//					if ((status.getText().indexOf("you") != -1)
//							|| (status.getText().indexOf("I") != -1)) {// contains("hi")||status.getText().contains("ok"))
																		// {
						tweetInfo = new Tweet(
								String.valueOf(status.getId()), user.getName(),
								status.getCreatedAt().toString(),
								String.valueOf(geo.getLatitude()),
								String.valueOf(geo.getLongitude()),
								status.getText());
						String text = status.getId() + "\t" + user.getName()
								+ "\t" + status.getCreatedAt() + "\t"
								+ geo.getLatitude() + "\t" + geo.getLongitude()
								+ "\t" + status.getText();
						queue.offer(tweetInfo);
						dbHandler.upload(tweetInfo, sqsHandler);
						System.out.println(text);
//					}
				}
			}
		};
		twitterStream.addListener(listener);
//		FilterQuery fq = new FilterQuery();
//		//fq.track(new String[] { "Bieber", "Teletubbies" });
//		// fq.track(new String[]{"cloud twitter"});
//		fq.language(new String[] { "en" });
//		fq.locations(new double[][] { new double[] { -180, -90 },
//				new double[] { 180, 90 } });
//		// fq.locations(new double[][]{new double[]{-126.562500,30.448674},
//		// new double[]{-61.171875,44.087585
//		// }});
//		twitterStream.filter(fq);
		twitterStream.sample();
		analyzeTweetSink.initWorker(sqsHandler.getMyQueueUrl(), sqsHandler.getSqs());
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// Besides "text/event-stream;", Chrome also needs charset, otherwise
		// does not work
		// "text/event-stream;charset=UTF-8"
		response.setContentType("text/event-stream;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Connection", "keep-alive");

		PrintWriter out = response.getWriter();

		// out.print("id: " + "receiveMessage" + "\n");
		// try {
		// out.print("data: " + queue.take() + "\n\n");
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// out.flush();
		JSONArray arr = new JSONArray();

		JSONObject tmpJson;
		Tweet tmpTweetInfo;
		//System.out.println("queue size is :" + queue.size());
		try {
			for (int i = 0; i < queue.size()*1/2; ++i) {
				try {
					tmpTweetInfo = queue.take();
					tmpJson = new JSONObject();
					tmpJson.put("tweetID", tmpTweetInfo.getTweetID());
					tmpJson.put("userName", tmpTweetInfo.getUserName());
					tmpJson.put("creatAt", tmpTweetInfo.getCreateAt());
					tmpJson.put("latitude", tmpTweetInfo.getLatitude());
					tmpJson.put("longitude", tmpTweetInfo.getLongitude());
					tmpJson.put("content", tmpTweetInfo.getContent());
					arr.put(tmpJson);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			// error handling
		}

		out.print("id: " + "receiveMessage" + "\n");
		out.print("retry: " + "200" + "\n");

		out.print("data: " + arr.toString() + "\n\n");
		out.flush();
		//

		// while (true) {
		// out.print("id: " + "ServerTime" + "\n");
		// out.print("data: " + new Date() + "\n\n");
		// out.flush();
		// // out.close(); //Do not close the writer!
		// try {
		// Thread.currentThread().sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
	}
}
