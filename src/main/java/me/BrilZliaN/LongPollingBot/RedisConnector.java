package me.BrilZliaN.LongPollingBot;

import java.util.Random;
import java.util.logging.Level;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisConnector {
	
	final String TABLE = "clubmemberlist";
	final String TABLE_TIMESTAMP = TABLE + "_timestamp";
	
	RedisCommands<String, String> commands;
	Configuration configuration;
	
	Random random = new Random();
	
	public RedisConnector(Configuration configuration) {
		try {
			String password = configuration.get("redis.password");
			
			RedisURI redisUri = RedisURI.Builder.redis("localhost").withPort(6379).withPassword(password).build();
			RedisClient client = RedisClient.create(redisUri);

			StatefulRedisConnection<String, String> connection = client.connect();

			commands = connection.sync();
			commands.auth(password);
			
			Log.get().info("Redis readiness: " + commands.ping());
		} catch (Throwable e) {
			Log.get().log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public void addUser(String user, boolean joined) {
		commands.hset(TABLE, user, convert(joined));
	}
	
	public boolean containsUser(String user) {
		return commands.hexists(TABLE, user);
	}
	
	public String getUser(String user) {
		return commands.hget(TABLE, user);
	}
	
	public void resetDelay(String user) {
		commands.hset(TABLE_TIMESTAMP, user, Long.toUnsignedString(System.currentTimeMillis()));
	}
	
	public long getDelay(String user){
		String ans = commands.hget(TABLE_TIMESTAMP, user);
		if (ans == null || ans.equals("null") || ans.isEmpty()) {
			return System.currentTimeMillis();
		}
		return System.currentTimeMillis() - Long.parseUnsignedLong(ans);
	}
	
	private String convert(boolean bool) {
		return bool ? "true" : "false";
	}

}
