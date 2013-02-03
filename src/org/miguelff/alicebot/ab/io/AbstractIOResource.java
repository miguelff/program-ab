package org.miguelff.alicebot.ab.io;


public abstract class AbstractIOResource implements IOResource {
	
	private String name;
	
	public AbstractIOResource(String name) {
		this.name = normalize(name);
	}

	public String name(){
		return name;
	}

	private String normalize(String name) {
		return name.replace("//","/");
	}

}