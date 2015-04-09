package edu.nyu.cloud.tweetmap.controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;


public class DownloadSample {
	 static AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
	    static String tableName = "TwitterMap";
	    static int cnt = 0;
	    public static void main(String[] args) throws Exception {
	    	DownloadSample ds = new DownloadSample();
	    	ds.getTwitterData();
	    }


	    public List<GeoLocations> getTwitterData() {
	        
//	        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
//			expressionAttributeValues.put(":pr", new AttributeValue().withN("100"));
	        
//	        ScanRequest scanRequest = new ScanRequest()
//	            .withTableName(tableName);
	            //.withFilterExpression("Price < :pr")
	            //.withExpressionAttributeValues(expressionAttributeValues)
	            //.withProjectionExpression("Id, Title, ProductCategory, Price");

//	        ScanResult result = client.scan(scanRequest);
	        
	        //System.out.println("Scan of " + tableName + " for items with a price less than 100.");
//	        System.out.println("Scan of " + tableName + " for items ");
//	        for (Map<String, AttributeValue> item : result.getItems()) {
//	            System.out.println("");
//	            printItem(item);
//	            System.out.println(result.getCount());
//	        }
	    	//int cnt = 0;
	        Map<String, AttributeValue> lastKeyEvaluated = null;
	        List<GeoLocations> loc = new ArrayList<GeoLocations>();
	        do {
	            ScanRequest scanRequest = new ScanRequest()
	                .withTableName(tableName)
//	                .withLimit(10)
	                .withExclusiveStartKey(lastKeyEvaluated);

	            ScanResult result = client.scan(scanRequest);
	           // cnt += result.getCount();
	            
	            for (Map<String, AttributeValue> item : result.getItems()){
	                //printItem(item);
	            	cnt++;
	            	
	                loc.add(storeItem(item));
	            }
	            lastKeyEvaluated = result.getLastEvaluatedKey();
	        } while (lastKeyEvaluated != null);
	        return loc;
	    }
	    
	    private static GeoLocations storeItem(Map<String, AttributeValue> attributeList) {
	    	GeoLocations info =  new GeoLocations(attributeList.get("Latitude").getS(),attributeList.get("Longitude").getS(),attributeList.get("Content").getS().replace("\"", " "));
	    	if(cnt == 22) System.out.println("aaaa"+"\t"+info.getContent());
	    	return info;
	    }
	    private static void printItem(Map<String, AttributeValue> attributeList) {
	    	System.out.println(attributeList.get("TwitterID").getS()+"\t"+attributeList.get("Latitude").getS()+"\t"+attributeList.get("Latitude").getS());
	        /*for (Map.Entry<String, AttributeValue> item : attributeList.entrySet()) {
	            String attributeName = item.getKey();
	            AttributeValue value = item.getValue();
	            System.out.println(attributeName + " "
	                    + (value.getS() == null ? "" : "S=[" + value.getS() + "]")
	                    + (value.getN() == null ? "" : "N=[" + value.getN() + "]")
	                    + (value.getB() == null ? "" : "B=[" + value.getB() + "]")
	                    + (value.getSS() == null ? "" : "SS=[" + value.getSS() + "]")
	                    + (value.getNS() == null ? "" : "NS=[" + value.getNS() + "]")
	                    + (value.getBS() == null ? "" : "BS=[" + value.getBS() + "] \n"));
	        }*/
	    }
}
