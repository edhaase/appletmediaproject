package org.amp.mediaserver.contentdirectory.operator;

import org.fourthline.cling.support.model.DIDLObject;

public interface Predicate {

	boolean isValidFor(final DIDLObject object);

}
