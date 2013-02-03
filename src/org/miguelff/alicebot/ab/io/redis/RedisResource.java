package org.miguelff.alicebot.ab.io.redis;

import java.util.Date;

import org.miguelff.alicebot.ab.io.AbstractIOResource;

import redis.clients.jedis.Jedis;

public abstract class RedisResource extends AbstractIOResource {

	protected static final String LAST_MODIFIED_FIELD = "_l";
	protected static final String CONTENTS_FIELD = "_c";

	public RedisResource(String name) {
		super(name);
	}

	@Override
	public long getLastModified() {
		return Long.parseLong(Redis.exec(new RedisCommand<String>() {
			public String execute(Jedis redis) {
				return redis.hget(name(), LAST_MODIFIED_FIELD);
			}
		}));
	}

	@Override
	public void touch() {
		Redis.exec(new RedisCommand<Void>() {
			public Void execute(Jedis redis) {
				redis.hset(name(), LAST_MODIFIED_FIELD, Long.toString(new Date().getTime()));
				return null;
			}
		});
		
	}

	@Override
	public boolean exists() {
		return Redis.exec(new RedisCommand<Boolean>() {
			public Boolean execute(Jedis redis) {
				return redis.exists(name());
			}
		});
	}

}
