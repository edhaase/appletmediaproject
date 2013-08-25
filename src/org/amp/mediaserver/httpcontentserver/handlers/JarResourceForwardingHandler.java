package org.amp.mediaserver.httpcontentserver.handlers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * Resource not required to exist (returns 404 if unavailable).
 */
public class JarResourceForwardingHandler implements HttpRequestHandler {

	protected String resourceName;
	protected String mime;
	
	public JarResourceForwardingHandler(String resourceName, String mimeType) {
		this.resourceName = resourceName;
		this.mime = mimeType;		
	}

	@Override
	public void handle( HttpRequest request, 
						HttpResponse response,
						HttpContext context)
			throws HttpException, IOException {
		
		/////////////////////////////////////////////////////////////////////
		// Limit this to head/get requests.
		/////////////////////////////////////////////////////////////////////
		String method = request.getRequestLine().getMethod(); // .toUpperCase(Locale.ENGLISH);
		
		if( !method.equalsIgnoreCase("GET")
		 && !method.equalsIgnoreCase("HEAD")) {
			response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
			return;
		}
		
		URL url =  JarResourceForwardingHandler.class.getResource(resourceName);
		int size = 0;
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			conn.setConnectTimeout(5000);
			size = conn.getContentLength();
		} catch (IOException e) {			
			response.setStatusCode(HttpStatus.SC_NOT_FOUND);
			return;
		}
		
		response.setHeader(HttpHeaders.CONTENT_TYPE, mime);

		if(method.equalsIgnoreCase("HEAD"))
			response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(size));
		
		/////////////////////////////////////////////////////////////////////
		// Open input stream.
		/////////////////////////////////////////////////////////////////////		
		if( method.equalsIgnoreCase("GET") ) {													
			response.setEntity( new InputStreamEntity(conn.getInputStream(), size) );
		}
	}

}
