package org.miguelff.alicebot.ab.io.s3;

import org.miguelff.alicebot.ab.Config;

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
	}
	
	static AmazonS3Client client(){
		return s3;
	}
	
	static String bucket(){
		return BUCKET_NAME;
	}
}
