package org.amp.mediaserver.contentdirectory.operator;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Always returns true.
 */
public class LogicalTruth implements Predicate {

	@Override
	public boolean isValidFor(final DIDLObject object) {
		return true;
	}

}
