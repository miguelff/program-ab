package org.miguelff.alicebot.ab.io.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.miguelff.alicebot.ab.io.IOResource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class RedisFile extends RedisResource {

	public RedisFile(String name) {
		super(name);
	}

	@Override
	public InputStream input() {	
		String contents = Redis.exec(new RedisCommand<String>() {
			public String execute(Jedis redis) {
				return redis.hget(name(), CONTENTS_FIELD);
			}			
		});
		return new ByteArrayInputStream(contents.getBytes());
	}

	@Override
	public OutputStream output() {
		return new RedisOutputStream();
	}

	@Override
	public List<IOResource> getNested() {
		throw new UnsupportedOperationException("Cannot get nested files from Redis RedisFile "+name());
	}

	@Override
	public boolean hasNested() {
		return false;
	}
	
	@Override
	public String toString() {
		return "RedisFile [name()=" + name() + "]";
	}

	
	class RedisOutputStream extends OutputStream {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			os.write(b);			
		}
		
		public void close() {
			Redis.exec(new RedisCommand<Void>() {
				@Override
				public Void execute(Jedis redis) {
					Transaction t = redis.multi();
					t.hset(name(), CONTENTS_FIELD, os.toString());
					t.hset(name(), LAST_MODIFIED_FIELD, Long.toString(new Date().getTime()));
					t.exec();
					return null;
				}
			});			
		}
	}

}
