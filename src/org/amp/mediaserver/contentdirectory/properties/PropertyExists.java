package org.amp.mediaserver.contentdirectory.properties;

import org.fourthline.cling.support.model.DIDLObject;

//////////////////////////////////////////////////////////////////////////////////
// Extends the property retriever's "hasProperty" method to a predicate.
//////////////////////////////////////////////////////////////////////////////////	
public class PropertyExists extends AbstractPropertyPredicate {

	public PropertyExists(PropertyRetriever property) throws InvalidPropertyException {
		super(property);		
	}
		
	public PropertyExists(String propertyName) throws InvalidPropertyException {
		super(propertyName);
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Check the given object for the property.
	//////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean isValidFor(DIDLObject object) {	
		// If object can not support the property we return false.
		// However, support of the property does not mean it is set.
		if(!property.supportsProperty(object)) {
			return false;
		}
		
		try {
			return property.hasProperty(object);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
				
	}
}
