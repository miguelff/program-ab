package org.miguelff.alicebot.ab.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


final class NullResource implements IOResource {

	static final IOResource instance = new NullResource();
	
	private NullResource(){}
	
	@Override
	public String name() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream input() {
		throw new UnsupportedOperationException();
	}

	@Override
	public OutputStream output() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<IOResource> getNested() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void touch() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNested() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists() {
		return false;
	}
	
}