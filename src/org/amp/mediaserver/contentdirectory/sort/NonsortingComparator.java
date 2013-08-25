package org.amp.mediaserver.contentdirectory.sort;

import java.util.Comparator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Always keeps all items, doesn't actually sort.
 */
public class NonsortingComparator implements Comparator<DIDLObject> {
	
	@Override
	public int compare(DIDLObject o1, DIDLObject o2) {
		return 1;
	}

}
