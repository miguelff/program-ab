package org.miguelff.alicebot.ab.io.s3;

import java.util.List;

import org.miguelff.alicebot.ab.io.AbstractIOResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;


public class S3ResourceProvider extends AbstractIOResourceProvider{
	
	private static S3ResourceProvider INSTANCE = new S3ResourceProvider();
	
	public static S3ResourceProvider getInstance() {
		return INSTANCE;
	}
	
	private S3ResourceProvider() {}
	
	@Override
	public IOResource getResource(String resourceName) {
		if (resourceName.startsWith(S3.PATH_SEPARATOR)) resourceName = resourceName.replaceFirst("/", "");
		if (isDirectory(resourceName)) {
			return new S3Directory(resourceName);
		}
		return new S3File(resourceName);
	}

	private boolean isDirectory(String fileName) {
		List<S3ObjectSummary> nestedResources = nestedResources(fileName);
		return (nestedResources != null && ! nestedResources.isEmpty());
	}

	private List<S3ObjectSummary> nestedResources(String resourceName) {
		ObjectListing listing = S3.client().listObjects(S3.bucket(), resourceName);
		List<S3ObjectSummary> resources = listing.getObjectSummaries();
		//exclude the own resource
		if (resources.size() > 0) resources = resources.subList(1, resources.size());
		return resources;
	}

}
