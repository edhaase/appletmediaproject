package org.amp.mediaserver.contentdirectory.properties;

import java.util.logging.Logger;

import org.fourthline.cling.support.model.DIDLObject;

public class PropertyEquality extends AbstractPropertyPredicate {

	final protected String value;
	final private static Logger log = Logger.getLogger(PropertyEquality.class.getName());
	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	//////////////////////////////////////////////////////////////////////////////////	
	public PropertyEquality(String propertyName, String value) throws InvalidPropertyException {
		super(propertyName);
		
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		this.value = value;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	//////////////////////////////////////////////////////////////////////////////////	
	public PropertyEquality(PropertyRetriever property, String value) throws InvalidPropertyException {
		super(property);
		
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
				
		this.value = value.toLowerCase();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Comparison predicate.
	//////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isValidFor(DIDLObject object) {
		if(!property.supportsProperty(object)) {
			return false;
		}
		
		try {
			String objvalue = property.valueFrom(object);
			if(objvalue != null) {
				if(objvalue.equalsIgnoreCase(value))
					return true;
			}
		} catch(NoSuchPropertyException e) {
			log.warning("Unexpected NoSuchPropertyException");
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}

