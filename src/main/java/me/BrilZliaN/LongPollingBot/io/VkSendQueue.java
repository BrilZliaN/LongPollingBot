package me.BrilZliaN.LongPollingBot.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import me.BrilZliaN.LongPollingBot.Configuration;
import me.BrilZliaN.LongPollingBot.Log;
import me.BrilZliaN.LongPollingBot.RedisConnector;
import me.BrilZliaN.LongPollingBot.VkAPI;
import me.BrilZliaN.LongPollingBot.json.ExecuteError;
import me.BrilZliaN.LongPollingBot.json.ExecuteIsMemberResponse;
import me.BrilZliaN.LongPollingBot.json.ExecuteResponse;

public class VkSendQueue implements Runnable {
	
	private Configuration config;
    private VkAPI api;
    private Queue<VkRequest> messageQueue;
    private Queue<VkSendRequest> requestQueue;
	private CloseableHttpClient httpClient;
	private JsonParser parser;
	private Gson gson;
	private RedisConnector redis;
	private final ExecutorService executor = Executors.newFixedThreadPool(6);
	
	private int messagesToPack = 0;
	private boolean logMessages = true;
	
	private final String SEND_ATTACHMENTMESSAGE = "API.messages.send({\"user_id\":%s,\"message\":\"%s\",\"attachment\":\"%s\",\"v\":\"5.68\"});";
	private final String SEND_MESSAGE = "API.messages.send({\"user_id\":%s,\"message\":\"%s\",\"v\":\"5.68\"});";
	private final String IS_MEMBER = "API.groups.isMember({\"user_id\":%s,\"group_id\":\"%s\",\"v\":\"5.68\"});";
	private final String VK_SCRIPT;
	
	public VkSendQueue(Configuration config, RedisConnector redis) {
    	this.config = config;
    	this.redis = redis;
    	this.api = new VkAPI(this.config.get("group.token"));
    	this.VK_SCRIPT = "https://api.vk.com/method/execute?access_token=" + api.getToken() + "&v=5.68";
    	this.messageQueue = new ConcurrentLinkedQueue<VkRequest>();
    	this.requestQueue = new ConcurrentLinkedQueue<VkSendRequest>();
    	this.messagesToPack = Math.min(Integer.parseInt(config.get("messaging.packedMessagesCount")), 25);
		this.logMessages = (config.get("server.debug") != null && config.get("server.debug").equals("true"));
		Log.get().info("VkSendQueue: message logging is " + (this.logMessages ? "ON" : "OFF"));
    	
		this.parser = new JsonParser();
		this.gson = new Gson();
		
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(500);
		cm.setDefaultMaxPerRoute(50);
	    httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}
	
	public void addMessage(String user, String message) {
		messageQueue.offer(new VkSendMessage(user, message));
	}
	
	public void addMessage(String user, String message, String attachment) {
		messageQueue.offer(new VkSendMessage(user, message, attachment));
	}

	public void addIsMember(String user) {
		messageQueue.offer(new VkIsMember(user));
	}
	
	@Override
	public void run() {
		try {
			if (requestQueue.isEmpty()) {
				if (!messageQueue.isEmpty()) {
					StringBuilder vkScript = new StringBuilder();
					int i = 0;
					vkScript.append("var ans = [];\nvar cur = [];\n");
					for (; i < messagesToPack && !messageQueue.isEmpty(); i++) {
						VkRequest req = messageQueue.poll();
						if (req.classType == VkIsMember.class) {
							VkIsMember ism = (VkIsMember) req;
							vkScript.append("cur.id = ").append(ism.getUser()).append(";\n");
							vkScript.append("cur.data = ").append(String.format(IS_MEMBER, ism.getUser(), config.get("group.id")).replace("\n", "\\n")).append("\n");
							vkScript.append("ans.push(cur);\n");
						} else if (req.classType == VkSendMessage.class) {
							VkSendMessage msg = (VkSendMessage) req;
							if (msg.hasAttachment()) {
								vkScript.append(String.format(SEND_ATTACHMENTMESSAGE, msg.getUser(), msg.getMessage(), msg.getAttachment()).replace("\n", "\\n")).append("\n");
							} else {
								vkScript.append(String.format(SEND_MESSAGE, msg.getUser(), msg.getMessage()).replace("\n", "\\n")).append("\n");
							}
						}
					}
					vkScript = vkScript.append("return ans;");
					executor.execute(new AsyncExecute(vkScript.toString(), i));
				}
			} else {
				VkSendRequest vkreq = requestQueue.poll();
				try {
					vkreq.setAnswer(executeGet(vkreq.getUrl()));
				} catch (Exception e) {
					Log.get().log(Level.SEVERE, "Couldn't execute get request from VkSendQueue", e);
				}
			}
		} catch (Exception ex) {
			Log.get().log(Level.INFO, ex.getMessage(), ex);
		}
	}
	
	public JsonElement executeGet(String url) throws ClientProtocolException, IOException {
	    HttpGet request = new HttpGet(url);
		
	    CloseableHttpResponse response = httpClient.execute(request, HttpClientContext.create());
	    JsonElement element = parser.parse(new BufferedReader(new InputStreamReader(response.getEntity().getContent())));
	    
	    request.releaseConnection();
		return element;
	}
	
	private String executePost(String url, String data, boolean read) throws ClientProtocolException, IOException {
	    HttpPost request = new HttpPost(url);
		
		NameValuePair pair = new BasicNameValuePair("code", data);
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Collections.singletonList(pair), "UTF-8");
		request.setEntity(entity);
		
	    CloseableHttpResponse response = httpClient.execute(request, HttpClientContext.create());
	    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    
	    String answer = null;
	    if (read) {
	    	answer = reader.lines().collect(Collectors.joining("\n"));;
	    }
	    
	    request.releaseConnection();
		return answer;
	}
	
	public class AsyncExecute implements Runnable {
		
		private String data;
		private int count;
		
		public AsyncExecute(String data, int count) {
			this.data = data;
			this.count = count;
		}

		@Override
		public void run() {
			try {
				String ans = executePost(VK_SCRIPT, data, true);
				ExecuteResponse response = gson.fromJson(ans, ExecuteResponse.class);
				if (logMessages)
					Log.get().info("=== VkSendQueue: Sent " + count);
				if (response.getExecute_errors() != null && !response.getExecute_errors().isEmpty()) {
					Log.get().info("Found these errors:");
					for (ExecuteError err : response.getExecute_errors()) {
						Log.get().info(err.getError_msg());
					}
					Log.get().info("---");
				}
				
				if (response.getResponse() != null && !response.getResponse().isEmpty()) {
					for (ExecuteIsMemberResponse r : response.getResponse()) {
						redis.addUser(r.getId(), r.getData() > 0);
					}
				}
				//Log.get().info(new Gson().toJson(e));
			} catch (IOException e) {
				Log.get().log(Level.SEVERE, "Couldn't execute post request from VkSendQueue", e);
			}
		}
		
		
	}

}
