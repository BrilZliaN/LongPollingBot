package me.BrilZliaN.LongPollingBot.io;

public class VkIsMember extends VkRequest {
	
	private String user;
	
	public VkIsMember(String user) {
		this.user = user;
		this.classType = VkIsMember.class;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

}
