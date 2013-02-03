package org.miguelff.alicebot.ab.io.redis;

import org.miguelff.alicebot.ab.io.AbstractIOResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;

import redis.clients.jedis.Jedis;

public class RedisResourceProvider extends AbstractIOResourceProvider {

	private static RedisResourceProvider INSTANCE = new RedisResourceProvider();
	
	public static RedisResourceProvider getInstance(){
		return INSTANCE;
	}
	
	private RedisResourceProvider(){}
	

	@Override
	public IOResource getResource(String resourceName) {
		if (resourceName.startsWith(Redis.PATH_SEPARATOR)) {
			resourceName = resourceName.replaceFirst("/", "");
		}
		if (isDirectory(resourceName)) {
			return new RedisDirectory(resourceName);
		}
		return new RedisFile(resourceName);
	}

	private boolean isDirectory(final String resourceName) {
		return Redis.exec(new RedisCommand<Boolean>() {
			public Boolean execute(Jedis redis) {
				return hasNested(resourceName, redis) ||
					   (hasLastModified(resourceName, redis) && ! hasContent(resourceName, redis));
			}

			private boolean hasContent(String resourceName, Jedis redis) {
				return redis.hget(resourceName, RedisResource.CONTENTS_FIELD) != null;
			}

			private boolean hasLastModified(String resourceName, Jedis redis) {
				return redis.hget(resourceName, RedisResource.LAST_MODIFIED_FIELD) != null;
			}

			private boolean hasNested(final String resourceName, Jedis redis) {
				return redis.keys(Redis.withPrefix(resourceName)).size() > 0;
			}
		});
	}	

}
