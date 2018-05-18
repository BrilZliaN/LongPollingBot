package me.BrilZliaN.LongPollingBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.BrilZliaN.LongPollingBot.io.VkReceiveEvents;
import me.BrilZliaN.LongPollingBot.io.VkSendQueue;

public class Server {
	
	private Logger log;
	private Configuration config;
	
	private RedisConnector redis;
	
	private LogicThread logic;
	private VkReceiveEvents in;
	private VkSendQueue out;
	
	private final ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(4);
	
	public Server() {
		log = Log.get();
		
		loadConfiguration();
		loadRedis();
		loadThreads();
	}
	
	private void loadConfiguration() {
		try {
			config = new Configuration(new File("config.json"));
			
			boolean debug = config.get("server.debug") != null && config.get("server.debug").equals("true");
			log.info("Debugging mode is " + (debug ? "ON" : "OFF"));
		} catch (Exception e) {
			log.severe("Cannot read configuration file! Shutting down");
			log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
	}
	
	/*private void loadMySQL() {
		try {
			log.info("Connecting to the MySQL server...");
			mysql = new MySQL();
			mysql.connect(config.get("mysql.user"), config.get("mysql.password"), config.get("mysql.host"), config.get("mysql.database"));
			log.info("Successfully connected to the MySQL server.");
		} catch (Exception e) {
			log.severe("Cannot connect to the MySQL server! Shutting down");
			log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(1);
		}
	}*/
	
	private void loadRedis() {
		redis = new RedisConnector(config);
	}
	
	private void loadThreads() {
		out = new VkSendQueue(config, redis);
		logic = new LogicThread(out, redis, Long.parseUnsignedLong(config.get("messaging.notificationDelay")), config.get("group.id"));
		
		VkAPI api = new VkAPI(this.config.get("group.token"));
		try {
			JsonElement e = executeGet(api.getURL("groups.getLongPollServer", "group_id=" + config.get("group.id")));
			JsonObject o = e.getAsJsonObject().get("response").getAsJsonObject();
			in = new VkReceiveEvents(logic, redis, config, o.get("key").getAsString(), o.get("server").getAsString(), o.get("ts").getAsString());
		} catch (Exception e) {
			Log.get().log(Level.INFO, e.getMessage(), e);
			
			System.exit(0);
			
		}
		
		int delay = Integer.parseInt(config.get("messaging.delay"));
		scheduleExecutor.scheduleWithFixedDelay(out, 0, delay, TimeUnit.MILLISECONDS);
		scheduleExecutor.scheduleAtFixedRate(logic, 0, delay, TimeUnit.MILLISECONDS);
		scheduleExecutor.scheduleAtFixedRate(in, 0, delay, TimeUnit.MILLISECONDS);
	}
	
	public JsonElement executeGet(String url) throws ClientProtocolException, IOException {
	    HttpGet request = new HttpGet(url);
		
	    CloseableHttpResponse response = HttpClients.createMinimal().execute(request, HttpClientContext.create());
	    JsonElement element = new JsonParser().parse(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
	    
	    request.releaseConnection();
		return element;
	}

}
