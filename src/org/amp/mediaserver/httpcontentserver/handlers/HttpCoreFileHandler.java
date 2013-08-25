package org.amp.mediaserver.httpcontentserver.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

import org.amp.mediaserver.FileExtension;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.seamless.util.MimeType;

public class HttpCoreFileHandler implements HttpRequestHandler {

	protected File file;
	protected String mime;
	
	public HttpCoreFileHandler(File file) {
		this.file = file;
		String mimes[] = FileExtension.find(file).mime;
		this.mime = mimes[0];
	}

	public HttpCoreFileHandler(File file, String mimeType) {
		this.file = file;
		this.mime = mimeType;
	}
	
	@Override
	public void handle( HttpRequest request, 
						HttpResponse response,
						HttpContext context)
			throws HttpException, IOException {
		
						
		String method = request.getRequestLine().getMethod();
						
		
		if( !method.equalsIgnoreCase("GET")
		 && !method.equalsIgnoreCase("HEAD")) {
			response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		
		URI uri = URI.create(request.getRequestLine().getUri());
			
		//////////////////////////////////////////////////////////////////////////////
		// Quick and dirty mime type override in a somewhat-safe way
		//////////////////////////////////////////////////////////////////////////////
		String f = uri.getPath();
		if(f != null && f.contains("$")) {
			Scanner s = new Scanner(f.substring(f.indexOf('$')+1));
			if(s.hasNextInt()) {
				int i = s.nextInt();
				MimeType[] types = FileExtension.find(file).type;
				if(i < 0) i = 0;
				if(i >= types.length) i = types.length-1;
				response.setHeader(HttpHeaders.CONTENT_TYPE, types[i].getType());
			} else
				response.setHeader(HttpHeaders.CONTENT_TYPE, mime);
			
			s.close();
		} else {
			response.setHeader(HttpHeaders.CONTENT_TYPE, mime);
		}
		
		response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
		
				
		//////////////////////////////////////////////////////////////////////////////
		// Handle partial range content
		//////////////////////////////////////////////////////////////////////////////		
		Header rangeHeader = request.getFirstHeader(HttpHeaders.RANGE);
	
		long size = file.length();
		long first = 0;
		long last = size;
						
		if( rangeHeader != null) {				
			// System.out.println("Range: [" + rangeHeader.getValue() + "]"); // bytes=182448128-183767093
			String partial = rangeHeader.getValue().substring(rangeHeader.getValue().lastIndexOf('=') + 1);
			String[] parts = partial.split("-");	
			
			first = Integer.valueOf(parts[0]);
			if(parts.length >= 2) {
				last =  Integer.valueOf(parts[1]);
			} else {
				last = file.length()-1;
			}
			
			size = (last-first)+1;
			
			if( (first < 0) || (last > file.length()) ) {
				/* System.err.println(
						String.format("%s: Requested range not satisfiable (%d-%d)", getClass().getName(), first, last) 
						); */
				response.setStatusCode(HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
				return;
			}
			
			response.setHeader(
					HttpHeaders.CONTENT_RANGE,
					String.format("bytes %d-%d/%d", first, last, file.length())
			);			
			response.setStatusCode(HttpStatus.SC_PARTIAL_CONTENT);
		} else {					
			response.setStatusCode(HttpStatus.SC_OK);				
		}

		//////////////////////////////////////////////////////////////////////////////
		// Open input file
		//////////////////////////////////////////////////////////////////////////////		
		if( method.equalsIgnoreCase("GET") ) {
			InputStream is = new FileInputStream(file);
			is.skip(first);							
			response.setEntity( new InputStreamEntity(is, size) );
		} else {
			response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(size));
		}
	}

}
