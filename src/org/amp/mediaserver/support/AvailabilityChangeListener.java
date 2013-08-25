package org.amp.mediaserver.support;

public interface AvailabilityChangeListener<T> {

	/**
	 * @param source Object generating the event.
	 * @param value The object that is being added. 
	 */
	void add(Object source, T value);	
	
	void remove(Object source, T value);
	
}
