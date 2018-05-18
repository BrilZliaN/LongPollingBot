package me.BrilZliaN.LongPollingBot;

public class VkAPI {

	private String token;
	
	// https://oauth.vk.com/authorize?client_id=5608155&scope=docs,wall,friends,groups,messages,offline&redirect_uri=https://oauth.vk.com/blank.html&display=page&v=5.68&response_type=token
	// 1d30a10a14c42382691b8063d8cd6e5acd2f4d99fbf010dd987432d0bc1e7debbb5cdfbc26542a6383427
	public VkAPI(String token) {
		this.token = token;
	}
	
	public String getURL(String method, String args) {
		return String.format("https://api.vk.com/method/%s?%s&access_token=%s&v=5.68", method, args, token);
	}
	
	public static String getURL(String method, String args, String token) {
		return String.format("https://api.vk.com/method/%s?%s&access_token=%s&v=5.68", method, args, token);
	}
	
	public String getToken() {
		return token;
	}

}
