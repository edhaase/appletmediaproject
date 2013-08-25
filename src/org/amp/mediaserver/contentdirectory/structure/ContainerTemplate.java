package org.amp.mediaserver.contentdirectory.structure;

import org.fourthline.cling.support.model.container.Container;

/**
 * Extension of Container because sometimes I'm lazy. 
 */
public class ContainerTemplate extends Container {
	
		
	public ContainerTemplate() {
		setChildCount(Integer.valueOf(0));	
	}
	
	//////////////////////////////////////////////////////////////////////////////////
    // Blatantly ignoring the expected behavior.
	//	TODO: Stop needing this.
    ////////////////////////////////////////////////////////////////////////////////// 
    @Override
    public Integer getChildCount() {
    	if( (getContainers().size() + getItems().size()) <= 0 ) {
    		return null;
    	}
    	return Integer.valueOf(getContainers().size() + getItems().size());
    }
        
}
