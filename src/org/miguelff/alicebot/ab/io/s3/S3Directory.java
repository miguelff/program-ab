package org.miguelff.alicebot.ab.io.s3;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miguelff.alicebot.ab.io.IOResource;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Modelles an s3 Object whose key is prefix of other objects
 * 
 * @author miguelff
 *
 */
public class S3Directory extends S3Resource {

	private static final String DIRECTORY_METADATA_FILE = "__metadata__.pab";

	private String directoryMetadataKey;

	private Map<String, IOResource> nestedFiles;

	public S3Directory(String resource) {
		super(resource);
		directoryMetadataKey = resource + (resource.endsWith("/") ? "" : "/") + DIRECTORY_METADATA_FILE;
	}

	@Override
	public InputStream input() {
		throw new UnsupportedOperationException("Cannot get input from s3 directory "+name());
	}

	@Override
	public OutputStream output() {
		throw new UnsupportedOperationException("Cannot get output from s3 directory "+name());
	}

	@Override
	public boolean hasNested() {
		return nestedFiles().size() > (hasMetadata() ? 1 : 0);
	}

	private boolean hasMetadata() {
		return nestedFiles().containsKey(directoryMetadataKey);
	}

	@Override
	public List<IOResource> getNested() {
		Map<String, IOResource> nestedFilesExceptMetadata;
		if (hasMetadata()) {
			nestedFilesExceptMetadata = new HashMap<String, IOResource>(nestedFiles());
			nestedFilesExceptMetadata.remove(directoryMetadataKey);
		} else {
			nestedFilesExceptMetadata = nestedFiles();
		}
		return new ArrayList<IOResource>(nestedFilesExceptMetadata.values());
	}

	@Override
	public void touch() {
		s3().putObject(S3.bucket(),
						directoryMetadataKey,
						new ByteArrayInputStream(Long.toString(new Date().getTime()).getBytes()),
						new ObjectMetadata());		
		if (! hasMetadata()) {
			nestedFiles().put(directoryMetadataKey, new S3File(directoryMetadataKey));
		}
	}


	@Override
	public long getLastModified() {
		if (! hasMetadata()) {
			touch();
			// we assume we've created it now
			return new Date().getTime();
		}else {
			return nestedFiles().get(directoryMetadataKey).getLastModified();
		}
	}

	@Override
	public boolean exists() {
		return !nestedFiles().isEmpty() || super.exists();
	}
	
	@Override
	public String toString() {
		return "S3Directory [name()=" + name() + "]";
	}

	private Map<String, IOResource> nestedFiles() {
		if (this.nestedFiles == null){
			this.nestedFiles = fetchNestedFiles();
		}
		return nestedFiles;
	}

	private Map<String, IOResource> fetchNestedFiles() {
		Map<String, IOResource> nestedFiles = new HashMap<String, IOResource>();
		
		ObjectListing listing = S3.client().listObjects(S3.bucket(), name());
		List<S3ObjectSummary> resources = listing.getObjectSummaries();
		//exclude the own resource
		if (resources.size() > 0) resources = resources.subList(1, resources.size());
		while(listing.isTruncated()){
			resources.addAll((listing = S3.client().listNextBatchOfObjects(listing)).getObjectSummaries());
		}
		for(S3ObjectSummary r: resources) {
			String fileName = r.getKey();
			boolean isChildren = fileName.replace(name(), "").split(S3.PATH_SEPARATOR).length <= 2;
			if (isChildren){
				nestedFiles.put(fileName, S3ResourceProvider.getInstance().getResource(fileName));
			}
		}
		return nestedFiles;
	}
}
