package org.amp.mediaserver.contentdirectory.properties;

import java.util.logging.Logger;

import org.fourthline.cling.support.model.DIDLObject;

public class PropertyDoesNotContain extends AbstractPropertyPredicate {

	final protected String value;
	final private static Logger log = Logger.getLogger(PropertyDoesNotContain.class.getName());
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructors
	//////////////////////////////////////////////////////////////////////////////////	
	public PropertyDoesNotContain(final PropertyRetriever property, final String value) throws InvalidPropertyException {
		super(property);
		this.value = value.toLowerCase();
	}
	
	public PropertyDoesNotContain(final String propertyName, final String value) throws InvalidPropertyException {
		super(propertyName);		
		if(value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		
		this.value = value;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Predicate
	//   UPnP-av-ContentDirectory-v1-Service property omission.
	//	"Any property value query (as distinct from an existence query) applied to an
	//	object that does not have that property, evaluates to false"
	//////////////////////////////////////////////////////////////////////////////////	
	@Override
	public boolean isValidFor(DIDLObject object) {
		if(!property.supportsProperty(object)) {
			return false;
		}
		
		try {
			String objvalue = property.valueFrom(object);
			if(objvalue != null) {
				if(!objvalue.toLowerCase().contains(value))
					return true;
			}
		} catch(NoSuchPropertyException e) {
			log.warning("NoSuchPropertyException: " + property.name + " - " + object.getClazz());
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}

