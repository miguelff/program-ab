package org.miguelff.alicebot.ab.io.s3;

import org.miguelff.alicebot.ab.io.AbstractIOResource;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;

/**
 * Modelles an AmazonS3 object 
 * 
 * @author miguelff
 *
 */
public abstract class S3Resource extends AbstractIOResource {	
	
	private final AmazonS3 s3;

	private final String bucket;
		
	public S3Resource(String name) {
		super(name);
		this.s3 = S3.client();
		this.bucket = S3.bucket();		
	}
	
	public AmazonS3 s3() {
		return s3;
	}

	public String bucket() {
		return bucket;
	}

	@Override
	public boolean exists() {
		ObjectListing listing = s3.listObjects(bucket, name());
		return listing != null && ! listing.getObjectSummaries().isEmpty();		
	}

}
