package org.amp.mediaserver.contentdirectory.operator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Simple creation of a logical NOT operator.
 */
public final class LogicalNOT implements Predicate {

	private final Predicate test;
	
	public LogicalNOT(final Predicate test) {
		if(test == null) {
			throw new IllegalArgumentException();
		}
		this.test = test;
	}
	
	@Override
	public boolean isValidFor(final DIDLObject object) {
		return !test.isValidFor(object);
	}

}
