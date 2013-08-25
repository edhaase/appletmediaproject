package org.amp.mediaserver.contentdirectory.operator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 *  Simple implementation of a Logical AND test.
 */
public final class LogicalAND implements Predicate {

	private final Predicate first, second;
	
	public LogicalAND(final Predicate a, final Predicate b) {
		if(a == null || b == null) throw new IllegalArgumentException();
		first = a;
		second = b;
	}

	@Override
	public boolean isValidFor(final DIDLObject object) {
		return first.isValidFor(object) && second.isValidFor(object);
	}
	
}
