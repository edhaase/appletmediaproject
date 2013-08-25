package org.amp.mediaserver.contentdirectory.operator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Always returns false.
 */
public class LogicalFalse implements Predicate {

	@Override
	public boolean isValidFor(final DIDLObject object) {
		return false;
	}

}
