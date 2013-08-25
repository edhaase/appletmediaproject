package org.amp.mediaserver.contentdirectory.properties;

import java.util.logging.Logger;

import org.fourthline.cling.support.model.DIDLObject;

/**
 * Evaluates a search statement of "[property name] contains [value]" for given objects.
 *  Example: "upnp:artist contains [artist]"
 */
public class PropertyContains extends AbstractPropertyPredicate {

	final protected String value;
	final private static Logger log = Logger.getLogger(PropertyContains.class.getName());
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructors
	//////////////////////////////////////////////////////////////////////////////////	
	public PropertyContains(final PropertyRetriever property, final String value) throws InvalidPropertyException {
		super(property);		
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		this.value = value;
	}
	
	public PropertyContains(final String propertyName, final String value) throws InvalidPropertyException {
		super(propertyName);		
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		this.value = value;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Predicate
	//////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean isValidFor(DIDLObject object) {
		if(!property.supportsProperty(object)) {
			return false;
		}
		
		try {
			String objvalue = property.valueFrom(object);
			if(objvalue != null) {
				if(objvalue.toLowerCase().contains(value))
					return true;
			}
		} catch(NoSuchPropertyException e) {
			log.warning("Catching NoSuchPropertyException (This should no longer be occuring).");
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
