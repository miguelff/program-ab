package org.miguelff.alicebot.ab.io;

import java.io.InputStream;
import java.io.OutputStream;


public abstract class AbstractIOResourceProvider implements IOResourceProvider  {

	@Override
	public InputStream inputFor(String resourceName) {
		return getResource(resourceName).input();
	}

	@Override
	public OutputStream outputFor(String resourceName) {
		return getResource(resourceName).output();
	}

}