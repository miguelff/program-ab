package org.miguelff.alicebot.ab.io;


abstract class AbstractIOResource implements IOResource {
	
	private String name;
	
	public AbstractIOResource(String name) {
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public boolean exists(){
		return this != IOResource.NULL;
	}
}