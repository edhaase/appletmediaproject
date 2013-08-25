package org.amp.mediaserver.contentdirectory.properties;

import java.util.logging.Logger;

import org.fourthline.cling.support.model.DIDLObject;

/*
 * upnp:class derivedfrom item.AudioItem is true for AudioItem and AudioItem.MusicTrack
 */
public class PropertyDerivedFrom extends AbstractPropertyPredicate {
	
	final protected String value;
	final private static Logger log = Logger.getLogger(PropertyDerivedFrom.class.getName());
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructors
	//////////////////////////////////////////////////////////////////////////////////	
	public PropertyDerivedFrom(String propertyName, String value) throws InvalidPropertyException {
		super(propertyName);
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		if(!property.equals(PropertyRetriever.UPNP_CLASS)) {			
			throw new InvalidPropertyException(String.format("%s derivedfrom %s is unsupported", propertyName, value));
		}
		
		this.value = value.toLowerCase();
	}
	
	public PropertyDerivedFrom(PropertyRetriever property, String value) throws InvalidPropertyException {
		super(property);
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		if(!property.equals(PropertyRetriever.UPNP_CLASS)) {			
			throw new InvalidPropertyException(String.format("%s derivedfrom %s is unsupported", property.name, value));
		}
		
		
		this.value = value.toLowerCase();
	}
	
	@Override
	public boolean isValidFor(DIDLObject object) {
		if(!property.supportsProperty(object)) {
			return false;
		}
		
		try {
			String objvalue = property.valueFrom(object);
			if(objvalue != null) {
				if(objvalue.toLowerCase().startsWith(value))
					return true;
			}
		} catch(NoSuchPropertyException e) {
			/*
			 *  If we don't have the property, we can't be derived from our test.
			 */			
			log.warning("Unexpected NoSuchPropertyException");
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
