package org.miguelff.alicebot.ab.io.redis;

import redis.clients.jedis.Jedis;

public interface RedisCommand<T> {

	public T execute(Jedis redis);
	
}
