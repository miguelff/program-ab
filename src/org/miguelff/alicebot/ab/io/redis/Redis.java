package org.miguelff.alicebot.ab.io.redis;

import java.net.URI;

import org.miguelff.alicebot.ab.Config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;


public class Redis {

	static final String PATH_SEPARATOR = "/";
	private static JedisPool pool;
			
	static {
		URI redisURI = Config.REDISTOGO_URL;
	    pool = new JedisPool(new JedisPoolConfig(),
	            redisURI.getHost(),
	            redisURI.getPort(),
	            Protocol.DEFAULT_TIMEOUT,
	            redisURI.getUserInfo() != null ? redisURI.getUserInfo().split(":",2)[1] : null);
	}
			
	
	static <T> T exec(RedisCommand<T> c){
		Jedis resource = pool.getResource();
		T result = c.execute(resource);
		pool.returnResource(resource);
		return result;
	}
	
	static String withPrefix(String key) {
		//at least one char more
		return key + "?*";
	}
	
}
