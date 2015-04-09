<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*,java.net.*"%>
<%@ page import="java.io.*"%>
<%@ page import="edu.nyu.cloud.tweetmap.*"%>
<%@ page import="edu.nyu.cloud.tweetmap.controller.*"%>
<%@ page import="twitter4j.*"%>
<%@ page import="twitter4j.conf.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>TwitterMap</title>
<script
	src="https://maps.googleapis.com/maps/api/js?v=3.exp&libraries=visualization"></script>
<script charset="UTF-8">
	// Adding 500 Data Points
	var map, pointarray, heatmap;
	var markers = [];
	var taxiData = [];
	var realTimeMarkers = [];
	var infoWindowArray = [];
	//var lastIncome = 0;
	var isShowRealTimeMap = false;
	var realTimeHeatMap;
	var liveTweets = new google.maps.MVCArray();
<%
			DownloadSample ds = new DownloadSample();
			List<GeoLocations> ls = ds.getTwitterData();
			for(GeoLocations gs:ls){
					%>
					taxiData.push(new google.maps.LatLng(parseFloat(<%=gs.getLatitude()%>),parseFloat(<%=gs.getLongitude()%>)));
<%}%>
	
	function initialize() {
		var mapOptions = {
			zoom : 2,
			center : new google.maps.LatLng(
					32.926226, 
					-117.141981),
			//center : new google.maps.LatLng(40.7300, -73.9950), NYC
			mapTypeId : google.maps.MapTypeId.SATELLITE
		};

		map = new google.maps.Map(document.getElementById('map-canvas'),
				mapOptions);

		var pointArray = new google.maps.MVCArray(taxiData);
		
		heatmap = new google.maps.visualization.HeatmapLayer({
			data : pointArray
		});
		//addMarker(map,"49.805478", "-79.96522499999998");
		heatmap.setMap(map);
		
 		realTimeHeatMap = new google.maps.visualization.HeatmapLayer({
 		    data: liveTweets,
 		    radius: 20
 		  });
		infowindow = new google.maps.InfoWindow({
			content : ''
		});
		<%for(int i = 1; i < ls.size(); ++i){%>
		addMarker(map,"<%=ls.get(i).getLatitude()%>","<%=ls.get(i).getLongitude()%>","<%=ls.get(i).getContent()%>");
	<%}%>
}
	
	function toggleHeatmap() {
		heatmap.setMap(heatmap.getMap() ? null : map);
		realTimeHeatMap.setMap(realTimeHeatMap.getMap() ? null : map);
		
	}

	function changeGradient() {
		var gradient = [ 'rgba(0, 255, 255, 0)', 'rgba(0, 255, 255, 1)',
				'rgba(0, 191, 255, 1)', 'rgba(0, 127, 255, 1)',
				'rgba(0, 63, 255, 1)', 'rgba(0, 0, 255, 1)',
				'rgba(0, 0, 223, 1)', 'rgba(0, 0, 191, 1)',
				'rgba(0, 0, 159, 1)', 'rgba(0, 0, 127, 1)',
				'rgba(63, 0, 91, 1)', 'rgba(127, 0, 63, 1)',
				'rgba(191, 0, 31, 1)', 'rgba(255, 0, 0, 1)' ]
		heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
		realTimeHeatMap.set('gradient', realTimeHeatMap.get('gradient') ? null : gradient);
		
	}

	function changeRadius() {
		heatmap.set('radius', heatmap.get('radius') ? null : 20);
		realTimeHeatMap.set('radius', realTimeHeatMap.get('radius') ? null : 20);
	}

	function changeOpacity() {
		heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
		realTimeHeatMap.set('opacity', realTimeHeatMap.get('opacity') ? null : 0.2);
		
	}
	
	function SearchOperation(){
		var text = document.getElementById("searchBar").value;
		for(i = 0; i < markers.length; ++i){
			var title = markers[i].getTitle();
			if(title.indexOf(text) >= 0){
				markers[i].setMap(map);
			}
			else{
				markers[i].setMap(null);
			}
 		}
		
	}
	
	function ShowTweetsMarker(){
		//heatmap.setMap(null);
		if(isShowRealTimeMap){
			realTimeHeatMap.setMap(null);
			isShowRealTimeMap = false;
		}
 		for(i = 0; i < markers.length; ++i){
 			markers[i].setMap(map);
 		}
	}
	
 	function ShowHeatMap(){
 		if(isShowRealTimeMap){
 			realTimeHeatMap.setMap(null);
 			isShowRealTimeMap = false;
 		}
 		for(i = 0; i < markers.length; ++i){
 			markers[i].setMap(null);
 		}
 		heatmap.setMap(map);
 	}
 	
	function addMarker(map,latitude, longitude,contents) {
		var marker = new google.maps.Marker({
			map : map,
			title: contents,
			position : new google.maps.LatLng(parseFloat(latitude),
					parseFloat(longitude))
		});
		
		google.maps.event.addListener(marker, "click", function() {
			infowindow.setContent(contents);
			infowindow.open(map, marker);
		});
		marker.setMap(map);
		markers.push(marker);
	}
	
	function ShowRealTimeMap(){
		for(i = 0; i < markers.length; ++i){
 			markers[i].setMap(null);
 		}
 		heatmap.setMap(null);
 		isShowRealTimeMap = true;
 		realTimeHeatMap.setMap(map);
	}
	
	function realTime(myarr) {
		if(isShowRealTimeMap){
			var realMarkers = [];
			var infoWindows = [];
			
			for(i = 0; i < myarr.length; ++i){
				var tweetID = myarr[i].tweetID;
				
				realTimeMarkers[tweetID]  = new google.maps.Marker({
					map : map,
					title: myarr[i].content,
					position : new google.maps.LatLng(parseFloat(myarr[i].latitude),
							parseFloat(myarr[i].longitude))
				});
				infoWindowArray[tweetID] = new google.maps.InfoWindow({
					content : ''
				});
				
				infoWindowArray[tweetID].setContent(myarr[i].content);//+" "+Object.keys(realTimeMarkers).length
				realTimeMarkers[tweetID].setMap(map);
				infoWindowArray[tweetID].open(map, realTimeMarkers[tweetID]);
				realMarkers.push(tweetID);
				var tweetLocation = new google.maps.LatLng(parseFloat(myarr[i].latitude),
						parseFloat(myarr[i].longitude));
				liveTweets.push(tweetLocation);
			}
			setTimeout(function () { 
				for(i = 0; i < realMarkers.length; i++){
					var disTweetID = realMarkers[i];
					realTimeMarkers[disTweetID].setMap(null);
				}
			},5000);
		}
	}
	
	function snsHandle(snsarr) {
		if(isShowRealTimeMap){
			var realMarkersUI = [];
			//var infoWindowsUI = [];
			
			for(i = 0; i < snsarr.length; ++i){
				var tweetID = snsarr[i].tweetID;
				realMarkersUI.push(tweetID);
				realTimeMarkers[tweetID].setIcon('http://maps.google.com/mapfiles/ms/icons/green-dot.png');
				var content = infoWindowArray[tweetID].getContent();
				content = content +"\nMood:"+snsarr.mood+"\nConfidence"+snsarr.confidence+"\n";
				infoWindowArray[tweetID].setContent(content);
				realTimeMarkers[tweetID].setMap(map);
				infoWindowArray[tweetID].open(map, realTimeMarkers[tweetID]);
				
			}
			setTimeout(function () { 
				for(i = 0; i < realMarkersUI.length; i++){
					var disTweetIDUI = realMarkers[i];
					realTimeMarkers[disTweetIDUI].setMap(null);
					delete realTimeMarkers[disTweetIDUI];
					delete infoWindowArray[disTweetIDUI];
				}
			},5000);
		}
	}
	google.maps.event.addDomListener(window, 'load', initialize);
	if (typeof (EventSource) !== "undefined") {
	   var source = new EventSource("TwitterStreamServlet");
	   source.onmessage = function(event) {
		//var dataReceived = event.data;
	   // document.getElementById("receiveMessage").innerHTML = event.data;
	    var myArr = JSON.parse(event.data);
	    realTime(myArr);
	     // + "<br><br>";
	   };
	   var uiUpdateSource = new EventSource("SNSServiceServlet");
	   uiUpdateSource.onmessage = function(event) {
			//var dataReceived = event.data;
		   // document.getElementById("receiveMessage").innerHTML = event.data;
		    var snsArr = JSON.parse(event.data);
		    snsHandle(snsArr);
		     // + "<br><br>";
		   };
	  } else {
	   document.getElementById("receiveMessage").innerHTML = "Sorry, your browser does not support server-sent events...";
	  }
</script>
</head>
<body>
	Please input your keywords:<input id="searchBar" type = "text">
	<button type="button" onClick="SearchOperation()">Search</button>
	<button type="button" onClick="ShowTweetsMarker()">MarkerMap</button>
	<button type="button" onClick="ShowHeatMap()">DensityMap</button>
	<!--  <input type="button" value="RealTime" onclick="window.location.href='http://www.google.com'">-->
	<button type="button" onClick="ShowRealTimeMap()">RealTime</button>
	<p id = "receiveMessage"></p>
	<div id="map-canvas" style="height: 600px; width: 1280px"></div>
	

</body>
</html>