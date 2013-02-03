package org.miguelff.alicebot.ab.io.layered;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.miguelff.alicebot.ab.io.AbstractIOResource;
import org.miguelff.alicebot.ab.io.IOResource;

public class LayeredResource extends AbstractIOResource {
	
	private IOResource cached, stored;
	private LayeredResourceProvider provider;
	
	LayeredResource(LayeredResourceProvider provider, IOResource cached, IOResource stored){
		super(cached.name());
		if (! cached.name().equals(stored.name())){
			throw new IllegalStateException("cached and store resources must be have the same name. Cached="+cached+", Stored="+stored);
		}	
		this.provider = provider;
		this.cached = cached;
		this.stored = stored;
	}

	@Override
	public InputStream input() {
		return cached.input();
	}

	@Override
	public OutputStream output() {
		return cached.output();
	}

	@Override
	public List<IOResource> getNested() {
		List<IOResource> nestedCached = cached.getNested();
		if (nestedCached.isEmpty()) {
			List<IOResource> storedNested = stored.getNested();
			for (IOResource r : storedNested) {
				nestedCached.add(provider.getResource(r.name()));
			}
		}
		return nestedCached;
	}

	@Override
	public void touch() {
		cached.touch();
	}

	@Override
	public long getLastModified() {
		return cached.getLastModified();
	}

	@Override
	public boolean hasNested() {
		return cached.hasNested() || stored.hasNested();
	}

	@Override
	public boolean exists() {
		return cached.exists();
	}

}
