package me.BrilZliaN.LongPollingBot.json;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Event {
	
	private static Gson GSON;
	
	static {
		GSON = new Gson();
	}
	
	private String type;
	private Optional<VkObject> object;
	private long groupId;
	
	public Event(JsonObject jsonObject) {
		type = jsonObject.get("type").getAsString();
		groupId = jsonObject.get("group_id").getAsLong();
		object = Optional.ofNullable(parseVkObject(jsonObject.get("object")));
	}
	
	public Event(JsonElement jsonElement) {
		this(jsonElement.getAsJsonObject());
	}
	
	private VkObject parseVkObject(JsonElement element) {
		Class<? extends VkObject> clazz = null;
		switch(type) {
		case "message_new":
		case "message_reply":
		case "message_edit":
			clazz = VkMessage.class;
			break;
		case "group_join":
		case "group_leave":
			clazz = VkUserId.class;
			break;
		}
		
		if (clazz == null)
			return null;
		
		return GSON.fromJson(element, clazz);
	}

	public String getType() {
		return type;
	}

	public Optional<VkObject> getObject() {
		return object;
	}

	public long getGroupId() {
		return groupId;
	}
	
	public boolean hasObject() {
		return object.isPresent();
	}

}
