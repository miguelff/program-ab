package org.miguelff.alicebot.ab.logging;


public class ConsoleLog implements ILog {

	@Override
	public void info(Object o) {
		System.out.println("INFO: "+ o);
	}

	@Override
	public void error(Object o) {
		System.out.println("ERROR: "+ o);
	}

}
