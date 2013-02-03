package org.miguelff.alicebot.ab;

import org.miguelff.alicebot.ab.io.IOResourceProvider;
import org.miguelff.alicebot.ab.io.layered.LayeredResourceProvider;
import org.miguelff.alicebot.ab.io.local.LocalFileSystemResourceProvider;
import org.miguelff.alicebot.ab.logging.ConsoleLog;
import org.miguelff.alicebot.ab.logging.ILog;
import org.miguelff.alicebot.ab.logging.NullLog;

/**
 * Facade to different resource providers 
 * 
 * @author miguelff
 *
 */
public abstract class ResourceProvider {
	
	public static final IOResourceProvider IO;
	
	public static final ILog Log;
	
	public static final Config ENV = Config.getInstance();
	
	static {
		IO = initializeIO();
		Log = initializeLog();
	}
	
	private static IOResourceProvider initializeIO(){
		return (Config.AWS_ACCESS_KEY != null && Config.REDISTOGO_URL != null) ? LayeredResourceProvider.REDIS_S3 : LocalFileSystemResourceProvider.getInstance();
	}

	private static ILog initializeLog() {
		return Config.MUTE_LOG ? new NullLog() : new ConsoleLog();
	}

}
