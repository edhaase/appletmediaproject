package org.amp.mediaserver.contentdirectory.sort;

import java.util.Comparator;

import org.amp.mediaserver.contentdirectory.properties.AbstractPropertyPredicate;
import org.amp.mediaserver.contentdirectory.properties.InvalidPropertyException;
import org.amp.mediaserver.contentdirectory.properties.PropertyRetriever;
import org.fourthline.cling.support.model.DIDLObject;

/**
 * Using PropertyRetriever mapping to compare properties. This won't cover all cases.
 *  Date fields and numbers need to be considered seperately.
 */
public class PropertyComparator extends AbstractPropertyPredicate implements Comparator<DIDLObject> {
			
	public PropertyComparator(String propertyName) throws InvalidPropertyException {				
		super(propertyName);
	}
	
	public PropertyComparator(PropertyRetriever property) throws InvalidPropertyException {
		super(property);
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// The workhorse.
	//////////////////////////////////////////////////////////////////////////////////	
	@Override
	public int compare(DIDLObject o1, DIDLObject o2) {
		if(!isValidFor(o1) || !isValidFor(o2)) {
			return 1;
		}
				
		try {
			String value1 = property.valueFrom(o1);
			String value2 = property.valueFrom(o2);	
			
			int r = value1.compareTo(value2);
			
			/* System.out.println(
					String.format("Compare %s against %s, result: %d", value1, value2, r)				
					); */
			
			return r;
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}				
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Checks a given object to see if the comparator supports it.
	//////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isValidFor(final DIDLObject object) {
		if(property.supportsProperty(object))
			return true;			
		return false;
	}

}
