package me.BrilZliaN.LongPollingBot.io;

import com.google.gson.JsonElement;

public class VkSendRequest {
	
	private String url;
	
	private volatile boolean hasAnswer = false;
	private JsonElement answer;
	
	public VkSendRequest(String url) {
		this.url = url;
	}
	
	public boolean hasAnswer() {
		return hasAnswer;
	}
	
	public JsonElement getAnswer() {
		return answer;
	}
	
	public void setAnswer(JsonElement answer) {
		this.answer = answer;
		this.hasAnswer = true;
	}
	
	public String getUrl() {
		return url;
	}

}
