package me.BrilZliaN.LongPollingBot;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Configuration {
	
	private Map<String, String> kv;
	
	public Configuration() {
		kv = new HashMap<String, String>();
	}
	
	public Configuration(File file) throws Exception {
		this();
		if (file.exists()) {
			String json = readFile(file);
			readJson(json);
		}
	}
	
	public String get(String key) {
		return kv.get(key);
	}
	
	public boolean contains(String key) {
		return kv.containsKey(key);
	}
	
	public void set(String key, String value) {
		kv.put(key, value);
	}
	
	private String readFile(File file) throws Exception {
		return String.join("\n", Files.readAllLines(file.toPath()));
	}
	
	private void readJson(String source) {
		JsonParser parser = new JsonParser();
		readJsonObjectRecursively("", parser.parse(source).getAsJsonObject());
	}
	
	private void readJsonObjectRecursively(String str, JsonObject object) {
		for (Entry<String, JsonElement> e : object.entrySet()) {
			JsonElement element = e.getValue();
			String key = str + "." + e.getKey();
			if (element.isJsonObject()) {
				readJsonObjectRecursively(key, element.getAsJsonObject());
			} else {
				kv.put(key.substring(1), element.getAsString());
			}
		}
	}

}
