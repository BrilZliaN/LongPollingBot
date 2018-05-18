package me.BrilZliaN.LongPollingBot.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.BrilZliaN.LongPollingBot.Configuration;
import me.BrilZliaN.LongPollingBot.Log;
import me.BrilZliaN.LongPollingBot.LogicThread;
import me.BrilZliaN.LongPollingBot.RedisConnector;
import me.BrilZliaN.LongPollingBot.VkAPI;
import me.BrilZliaN.LongPollingBot.json.Event;

public class VkReceiveEvents implements Runnable {
	
	private LogicThread logic;
	private CloseableHttpClient httpClient;
	private CloseableHttpResponse response;
	private BufferedReader reader;
	private JsonParser parser;
	
	private Configuration config;
	private String key;
	private String server;
	private String ts;
	private String url = "%s?act=a_check&key=%s&ts=%s&wait=20";
	
	public VkReceiveEvents(LogicThread logic, RedisConnector redis, Configuration config, String key, String server, String ts) {
		this.logic = logic;
		this.config = config;
		this.ts = ts;
		this.key = key;
		this.server = server;
		this.parser = new JsonParser();
	}
	
	private void init() {
		safeFinish();
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(100);
		cm.setDefaultMaxPerRoute(100);
		cm.setValidateAfterInactivity(-1);
	    httpClient = HttpClients.custom().setConnectionManager(cm).build();

	    HttpGet request = new HttpGet(String.format(url, server, key, ts));
	    try {
			response = httpClient.execute(request, HttpClientContext.create());
			
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	}

	@Override
	public void run() {
		try {
			if (response == null || reader == null) {
				init();
				return;
			}
			
			JsonElement parsed = parser.parse(reader);
			if (parsed == null || parsed.isJsonNull()) {
				safeFinish();
				return;
			}
			JsonObject element = parsed.getAsJsonObject();
			if (element.has("failed")) {
				Log.get().info("LongPoll failed, retrying");
				
				VkAPI api = new VkAPI(this.config.get("group.token"));
				try {
					JsonElement e = executeGet(api.getURL("groups.getLongPollServer", "group_id=" + config.get("group.id")));
					JsonObject o = e.getAsJsonObject().get("response").getAsJsonObject();
					this.key = o.get("key").getAsString();
					this.server = o.get("server").getAsString();
					this.ts = o.get("ts").getAsString();
				} catch (Exception e) {
					Log.get().log(Level.INFO, e.getMessage(), e);
					
					System.exit(0);
				}
				
				safeFinish();
			}
			ts = element.get("ts").getAsString();
			for (JsonElement e : element.getAsJsonArray("updates")) {
				Event event = new Event(e);
				logic.offerEvent(event);
			}
		} catch (Throwable e) {
			Log.get().log(Level.SEVERE, e.getMessage(), e);
			safeFinish();
		}
	}
	
	public JsonElement executeGet(String url) throws ClientProtocolException, IOException {
	    HttpGet request = new HttpGet(url);
		
	    CloseableHttpResponse response = HttpClients.createMinimal().execute(request, HttpClientContext.create());
	    JsonElement element = new JsonParser().parse(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
	    
	    request.releaseConnection();
		return element;
	}
	
	public void safeFinish() {
		try {
			if (reader != null) reader.close();
			if (response != null) response.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		reader = null;
		response = null;
	}
	
	

}
