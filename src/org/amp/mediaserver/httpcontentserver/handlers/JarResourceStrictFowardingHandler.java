package org.amp.mediaserver.httpcontentserver.handlers;

import java.io.IOException;
import java.io.InputStream;
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
 * Requires the resource to exist at time of instantiation.
 */
public class JarResourceStrictFowardingHandler implements HttpRequestHandler {

	protected String resourceName;
	protected String mime;
	protected int size = 0;
	
	public JarResourceStrictFowardingHandler(String resourceName, String mimeType) throws Exception {
		this.resourceName = resourceName;
		this.mime = mimeType;
		
		URL url =  JarResourceStrictFowardingHandler.class.getResource(resourceName);
		try {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(5000);
			size = conn.getContentLength();
		} catch (IOException e) {			
			throw new Exception("Unable to access resource");
		}				
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
		
		response.setHeader(HttpHeaders.CONTENT_TYPE, mime);
		
		if(method.equalsIgnoreCase("HEAD"))
			response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(size));
		
		/////////////////////////////////////////////////////////////////////
		// Open input stream.
		/////////////////////////////////////////////////////////////////////		
		if( method.equalsIgnoreCase("GET") ) {
			InputStream is = JarResourceStrictFowardingHandler.class.getResourceAsStream(resourceName);				
			response.setEntity( new InputStreamEntity(is, size) );
		}
	}

}
