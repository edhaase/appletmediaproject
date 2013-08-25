package org.amp.mediaserver.contentdirectory.properties;

import org.amp.mediaserver.contentdirectory.operator.Predicate;

/**
 * Enforces a valid property.
 */
public abstract class AbstractPropertyPredicate implements Predicate {
	protected final PropertyRetriever property;
	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	//////////////////////////////////////////////////////////////////////////////////	
	public AbstractPropertyPredicate(final String propertyName) throws InvalidPropertyException {
		property = PropertyRetriever.find(propertyName);
		if(property == null) {
			throw new InvalidPropertyException(propertyName + " unsupported at this time.");
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// 
	//////////////////////////////////////////////////////////////////////////////////	
	public AbstractPropertyPredicate(final PropertyRetriever property) throws InvalidPropertyException {
		if(property == null) {
			throw new InvalidPropertyException("Property must not be null");
		}
		this.property = property;
	}
	
}
