package me.BrilZliaN.LongPollingBot.io;

public class VkSendMessage extends VkRequest {
	
	private String user;
	private String message;
	private boolean hasAttachment;
	private String attachment;
	
	public VkSendMessage(String user, String message) {
		this.user = user;
		this.message = message;
		this.hasAttachment = false;
		this.classType = VkSendMessage.class;
	}
	
	public VkSendMessage(String user, String message, String attachment) {
		this.user = user;
		this.message = message;
		this.attachment = attachment;
		this.hasAttachment = true;
	}
	
	public boolean hasAttachment() {
		return hasAttachment;
	}

	public void setHasAttachment(boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUser() {
		return user;
	}
	
	public String getMessage() {
		return message;
	}

}
