package org.amp.mediaserver.httpcontentserver.handlers;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class NotFoundHandler implements HttpRequestHandler {

	final private static Logger log = Logger.getLogger(NotFoundHandler.class.getName());
	
	@Override
	public void handle( HttpRequest request, 
						HttpResponse response,
						HttpContext context)
			throws HttpException, IOException {

		log.fine(request.getRequestLine().getUri());
		
		response.setEntity(
				new  StringEntity("<HTML><BODY>This 404 page provided by the applet media server project.</BODY></HTML>", ContentType.TEXT_HTML)
				
				);
					
		// TODO Auto-generated method stub
		response.setStatusCode(HttpStatus.SC_NOT_FOUND);
	}

}
