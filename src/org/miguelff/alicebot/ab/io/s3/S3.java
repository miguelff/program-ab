package org.miguelff.alicebot.ab.io.s3;

import org.miguelff.alicebot.ab.Config;
import org.miguelff.alicebot.ab.ResourceProvider;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * S3 Configuration class
 * 
 * @author miguel
 *
 */
class S3 { 

	static final String PATH_SEPARATOR = "/";
	private static final String BUCKET_NAME;
	private static AmazonS3Client s3;
	
	static {
		String accessKey = Config.AWS_ACCESS_KEY; 
		String secretKey = Config.AWS_SECRET;		
		BUCKET_NAME = Config.AWS_S3_BUCKET_NAME;
		s3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
		check();
	}
	
	static AmazonS3Client client(){
		return s3;
	}
	
	static String bucket(){
		return BUCKET_NAME;
	}

	private static void check() {
		try {
			boolean ok = client().listObjects(bucket()).getObjectSummaries().size() > 0;
			if (!ok){
				throw new IllegalStateException("Objects retrieved: 0");
			}else{
				ResourceProvider.Log.info("Sanity check PASSED for S3");
			}
		}catch(Exception e){
			ResourceProvider.Log.error("Sanity check FAILED for S3:");
			ResourceProvider.Log.error("===========================");
			ResourceProvider.Log.error(String.format("AWS_ACCESS_KEY: %s, ",Config.AWS_ACCESS_KEY, Config.AWS_SECRET, Config.AWS_S3_BUCKET_NAME));
			ResourceProvider.Log.error(e.getMessage());
			ResourceProvider.Log.error("===========================");
			System.exit(-1);
		}
	}
}
