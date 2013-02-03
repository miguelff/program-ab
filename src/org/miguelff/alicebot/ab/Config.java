package org.miguelff.alicebot.ab;

import java.net.URI;
import java.net.URISyntaxException;

public class Config {

	public static final String BOT_NAME;

	public static final String AWS_ACCESS_KEY;
	public static final String AWS_SECRET;
	public static final String AWS_S3_BUCKET_NAME;

	public static final URI REDISTOGO_URL;

	public static final boolean MUTE_LOG;

	private static final Config INSTANCE = new Config();

	static {
		BOT_NAME = System.getenv("BOT_NAME");
		AWS_ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
		AWS_SECRET = System.getenv("AWS_SECRET");
		AWS_S3_BUCKET_NAME = System.getenv("AWS_S3_BUCKET_NAME");
		REDISTOGO_URL = urlFor(System.getenv("REDISTOGO_URL"));
		MUTE_LOG = System.getenv("MUTE_LOG") == null ? false : true;
	}
	
	public static Config getInstance() {
		return INSTANCE;
	}

	private static URI urlFor(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			ResourceProvider.Log.error(e.getMessage());
			return null;
		}
	}
	
	private Config() {}
}
