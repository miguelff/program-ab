package org.miguelff.alicebot.ab.io.layered;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miguelff.alicebot.ab.ResourceProvider;
import org.miguelff.alicebot.ab.io.AbstractIOResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;
import org.miguelff.alicebot.ab.io.IOResourceProvider;
import org.miguelff.alicebot.ab.io.redis.RedisResourceProvider;
import org.miguelff.alicebot.ab.io.s3.S3ResourceProvider;

public class LayeredResourceProvider extends AbstractIOResourceProvider {

	public static final LayeredResourceProvider REDIS_S3 = new LayeredResourceProvider(RedisResourceProvider.getInstance(), S3ResourceProvider.getInstance());

	private IOResourceProvider cache, storage;

	public LayeredResourceProvider(IOResourceProvider cache, IOResourceProvider storage) {
		this.cache = cache;
		this.storage = storage;
	}

	@Override
	public IOResource getResource(String resourceName) {
		IOResource result;
		IOResource cached = cache.getResource(resourceName);
		IOResource stored = storage.getResource(resourceName);
		
		if (cached.exists()) {
			result = new LayeredResource(this, cached, stored);
		} else {		
			if (stored.exists()) {
				createCacheEntry(cached, stored);	
				//regenerate cache value to avoid wrong ResourceType inference (Dir instead of File)
				result = getResource(resourceName);
			} else {
				result = IOResource.NULL;
			}
		}
		return result;
	}
	
	private void createCacheEntry(IOResource cached, IOResource stored) {
		if (stored.hasNested()) {
			cacheDir(cached, stored);
		}else {
			cacheFile(cached, stored);
		}
	}

	private void cacheDir(IOResource cached, IOResource stored) {
		cached.touch();
		IOResource dir = cache.getResource(cached.name());
		if (dir.hasNested() == false){
			throw new IllegalStateException("Must be a dir");
		}
	}

	private void cacheFile(IOResource cached, IOResource stored) {
		InputStream input = stored.input();
		OutputStream output = cached.output();

		byte[] buffer = new byte[4096];
		int bytesRead;
		try {
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			input.close();
			output.close();
		} catch (IOException e) {
			ResourceProvider.Log.error(e.getMessage());
		}
	}

}
