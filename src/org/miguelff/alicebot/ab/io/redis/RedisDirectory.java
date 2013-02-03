package org.miguelff.alicebot.ab.io.redis;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miguelff.alicebot.ab.io.IOResource;

import redis.clients.jedis.Jedis;

public class RedisDirectory extends RedisResource {

	public RedisDirectory(String name) {
		super(name);
	}

	@Override
	public InputStream input() {
		throw new UnsupportedOperationException("Cannot get input from redis directory "+name());
	}

	@Override
	public OutputStream output() {
		throw new UnsupportedOperationException("Cannot get output from redis directory "+name());
	}

	@Override
	public List<IOResource> getNested() {
		List<IOResource> list = new ArrayList<IOResource>();
		for (String key: keysWithPrefix()){			
			list.add(RedisResourceProvider.getInstance().getResource(key));
		}
		return list;
	}

	@Override
	public boolean hasNested() {
		return true;
	}	

	private Set<String> keysWithPrefix() {
		return Redis.exec(new RedisCommand<Set<String>>() {
			@Override
			public Set<String> execute(Jedis redis) {
				return redis.keys(Redis.withPrefix(name()));
			}			
		});
	}

	@Override
	public String toString() {
		return "RedisDirectory [name()=" + name() + "]";
	}
	
}
