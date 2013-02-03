package org.miguelff.alicebot.ab.io.s3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.miguelff.alicebot.ab.io.IOResource;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Modelles an S3 Object whose key is not prefix of others
 * 
 * @author miguelff
 *
 */
public class S3File extends S3Resource {

	public S3File(String name) {
		super(name);
	}
	
	@Override
	public InputStream input() {		
		S3Object object = s3().getObject(new GetObjectRequest(bucket(), name()));
		return object.getObjectContent();		
	}

	@Override
	public OutputStream output() {
		return new S3OutputStream();
	}

	@Override
	public boolean hasNested() {
		return false;
	}

	@Override
	public List<IOResource> getNested() {
		throw new UnsupportedOperationException("Cannot get nested files from S3File "+name());
	}
	
	@Override
	public long getLastModified() {
		return s3().getObjectMetadata(bucket(), name()).getLastModified().getTime();		
	}

	@Override
	public void touch() {
		s3().getObjectMetadata(bucket(), name()).setLastModified(new Date());
	}
	
	
	@Override
	public String toString() {
		return "S3File [name()=" + name() + "]";
	}


	/**
	 * An output stream that is flushed to S3 when closed
	 * 
	 * @author miguelff
	 *
	 */
	class S3OutputStream extends OutputStream {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		@Override
		public void write(int b) throws IOException {
			os.write(b);			
		}
		
		public void close() {
			//write the to amazon
			s3().putObject(S3.bucket(),
					name(),
					new ByteArrayInputStream(os.toByteArray()),
					new ObjectMetadata());		
		}
		
	}

}
