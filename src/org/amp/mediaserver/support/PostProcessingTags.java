package org.amp.mediaserver.support;

/**
 * String replace tokens for post-processing of browse/search calls.
 *
 */
public enum PostProcessingTags {

	HTTPSERVER		("HTTPSERVER_ADDRESS");
	
	
	PostProcessingTags(String name) {
		string = name;
	}
	
	final public String string;
	
}
