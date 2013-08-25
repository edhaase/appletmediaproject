package org.amp.mediaserver.httpcontentserver;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * For testing purposes, validates existing content length (if there is one) matches
 * what the HttpEntity is actually offering.
 */
public class ResponseContentStrict implements HttpResponseInterceptor {

	final private static Logger log = Logger.getLogger(ResponseContentStrict.class.getName());
	
	public void process(final HttpResponse response, final HttpContext context)
			throws HttpException, IOException {

		if(response == null) {
			throw new IllegalArgumentException("HTTP response may not be null");
		}

		HttpEntity entity = response.getEntity();
		if(entity == null)
			return;

		if(!response.containsHeader(HTTP.CONTENT_LEN))
			return;
		
		
		if(Long.valueOf(response.getFirstHeader(HTTP.CONTENT_LEN).getValue())
				.intValue() != entity.getContentLength()) {
			// throw new
			// RuntimeException("Content-Length header does not match entity supplied content length");
			log.warning(String
					.format("Content-length already included, %s was supplied. Entity offers %d",
							response.getFirstHeader(HTTP.CONTENT_LEN)
									.getValue(), entity.getContentLength()));
		}

	}
}
