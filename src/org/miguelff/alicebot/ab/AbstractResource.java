package org.miguelff.alicebot.ab;

abstract class AbstractResource implements IOResource {
	
	private String name;
	
	public AbstractResource(String name) {
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public boolean exists(){
		return this != IOResource.NULL;
	}
}