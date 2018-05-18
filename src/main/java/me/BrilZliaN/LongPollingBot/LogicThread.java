package me.BrilZliaN.LongPollingBot;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import me.BrilZliaN.LongPollingBot.io.VkSendQueue;
import me.BrilZliaN.LongPollingBot.json.Event;
import me.BrilZliaN.LongPollingBot.json.VkMessage;
import me.BrilZliaN.LongPollingBot.json.VkUserId;

public class LogicThread extends Thread {
	
	private VkSendQueue out;
	private RedisConnector db;
	private Queue<Event> queue;
	private long delay;
	private String club;
	private String ad;
	
	public LogicThread(VkSendQueue out, RedisConnector db, long delay, String club) {
		this.out = out;
		this.db = db;
		this.queue = new ConcurrentLinkedDeque<>();
		this.delay = delay;
		this.club = club;
		this.ad = "Вы не подписаны. Хотите *club" + this.club + " (подписаться)?";
	}
	
	public void offerEvent(Event event) {
		queue.offer(event);
	}
	
	@Override
	public void run() {
		if (queue.isEmpty()) return;
		
		while (!queue.isEmpty()) {
			Event event = queue.poll();
			switch (event.getType()) {
			case "message_new":
			case "message_reply":
			case "message_edit":
				onReceiveMessage(VkMessage.class.cast(event.getObject().get()));
				break;
			case "group_join":
				db.addUser(VkUserId.class.cast(event.getObject().get()).getUser_id(), true);
				break;
			case "group_leave":
				db.addUser(VkUserId.class.cast(event.getObject().get()).getUser_id(), false);
				break;
			}
		}
	}

	private void onReceiveMessage(VkMessage message) {
		String status = db.getUser(message.getUser_id());
		if (status != null && status.length() == 5) {
			if (db.getDelay(message.getUser_id()) > delay) {
				out.addMessage(message.getUser_id(), ad);
				db.resetDelay(message.getUser_id());
			}
		} else if (status == null || "null".equals(status)) {
			out.addIsMember(message.getUser_id());
		}
	}

}
