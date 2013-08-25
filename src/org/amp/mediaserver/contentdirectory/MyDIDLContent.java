package org.amp.mediaserver.contentdirectory;

import java.util.Collection;

import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

/**
 * Extension of DIDLContent with support for adding collections.
 */
public class MyDIDLContent extends DIDLContent {
	
	/////////////////////////////////////////////////////////////////////
	// And now we handle collections.
	/////////////////////////////////////////////////////////////////////
	public void addCollection(Collection<? extends DIDLObject> collection, long max) {
		if(collection == null) {
			return;
		}
		
		int results = 0;
		for(DIDLObject child : collection) {
			if(results++ > max) return;
			
			if(child instanceof Item){
				addItem((Item) child);
			}
			
			if(child instanceof Container) {
				addContainer((Container) child);
			}
		}
	}
	
	public void addCollection(Collection<? extends DIDLObject> collection) {
		addCollection(collection, collection.size());
	}
	
		
}
