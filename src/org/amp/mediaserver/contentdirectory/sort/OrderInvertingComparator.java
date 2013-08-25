package org.amp.mediaserver.contentdirectory.sort;

import java.util.Comparator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * 
 */
public class OrderInvertingComparator implements Comparator<DIDLObject> {

	final Comparator<DIDLObject> comparator;
	
	public OrderInvertingComparator(final Comparator<DIDLObject> comparator) {
		this.comparator = comparator;
	}
		
	
	@Override
	public int compare(DIDLObject o1, DIDLObject o2) {
		return -1 * comparator.compare(o1, o2);
	}

}
