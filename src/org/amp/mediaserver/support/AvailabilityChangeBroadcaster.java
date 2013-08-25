package org.amp.mediaserver.support;

import java.util.LinkedList;
import java.util.List;


/**
 * Implementation of availability change listener, that broadcasts events to multiple listeners.
 */
public class AvailabilityChangeBroadcaster<T> implements AvailabilityChangeListener<T> {

	public List<AvailabilityChangeListener<T>> list = new LinkedList<AvailabilityChangeListener<T>>();
	
	@Override
	public void add(Object source, T value) {
		for(AvailabilityChangeListener<T> listen : list) {
			listen.add(source, value);
		}
	}

	@Override
	public void remove(Object source, T value) {
		for(AvailabilityChangeListener<T> listen : list) {
			listen.remove(source, value);
		}
	}

}
