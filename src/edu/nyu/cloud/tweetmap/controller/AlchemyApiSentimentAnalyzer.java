package edu.nyu.cloud.tweetmap.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.nyu.cloud.tweetmap.model.Mood;
import edu.nyu.cloud.tweetmap.model.Sentiment;

public class AlchemyApiSentimentAnalyzer implements SentimentAnalyzer {

    private String apiKey = "14910e1c6089878917589da86aef5f10b5b0ab05";


    @Override
    public Sentiment analyze(String text) {

        try {

            URI uri = buildUri(text);

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
          //  HttpClient httpclient = new DefaultHttpClient();
            HttpClient httpclient = HttpClientBuilder.create().build();
            String responseBody = httpclient.execute(httpPost, responseHandler);


            Sentiment sentiment = parseSentiment(responseBody);
            if (sentiment == null) {
                return null;
            }


            return sentiment;

        } catch (URISyntaxException e) {
        	System.out.println(e.getMessage());
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }
        return null;
    }

    private Sentiment parseSentiment(String responseBody) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseJson = mapper.readValue(responseBody, Map.class);

        String status = (String) responseJson.get("status");

        if (!"ok".equalsIgnoreCase(status)) {
            String error = (String) responseJson.get("statusInfo");
            System.out.println(error);
            return null;
        }

        Map<String, String> docSentiment = (Map<String, String>) responseJson.get("docSentiment");

        String moodString = docSentiment.get("type");
        Mood mood = Mood.valueOf(moodString.toString().toUpperCase());
        String scoreString = docSentiment.get("score");
        float confidence = scoreString.isEmpty() ? 0 : Float.valueOf(scoreString);
        Sentiment result = new Sentiment(mood, confidence);
        return result;
    }


    private URI buildUri(String text) throws URISyntaxException {

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http")
                .setHost("access.alchemyapi.com")
                .setPath("/calls/text/TextGetTextSentiment")
                .setParameter("apikey", apiKey)
                .setParameter("text", text)
                .setParameter("outputMode", "json");

        return builder.build();
    }


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
