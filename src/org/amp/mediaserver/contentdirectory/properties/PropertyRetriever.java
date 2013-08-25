package org.amp.mediaserver.contentdirectory.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;

public enum PropertyRetriever {
	ID			("@Id",			"getId"),
	RESTRICTED  ("@restricted", "isRestricted"),
	PARENT_ID	("@parentID",	"getParentID"),
	CHILD_CNT	("@childCount",	"getChildCount",	Container.class),
	REF_ID		("@refId",		"getRefID",			Item.class),
	UPNP_CLASS	("upnp:class",	"getClazz"),
	DC_TITLE	("dc:title",	"getTitle"),	
	DC_CREATOR	("dc:creator",	"getCreator"),
	UPNP_ARTIST	("upnp:artist", "getFirstArtist",	MusicTrack.class );
	
	public Member member = null;
	public String name;
	protected Class<? extends DIDLObject> base;
	
	
	//////////////////////////////////////////////////////////////////////////////////
	// Constructors
	//////////////////////////////////////////////////////////////////////////////////	
	PropertyRetriever(String propertyName, String methodName, Class<? extends DIDLObject> base) {		
		this.name = propertyName;
		this.base = base;

		try {			
			member = base.getMethod(methodName);
		} catch(Exception e) {
			try {
				member = base.getField(methodName);
			} catch (Exception f) {
				System.err.println(getClass().getName() + ":  " + base.getName() + "(" + methodName + ")" + ": Nothing by this name");
			}	
		}
			
	}

	PropertyRetriever(String propertyName, String methodName) {
		this(propertyName, methodName, DIDLObject.class);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Can the given DIDLObject or derived class support this property?
	//////////////////////////////////////////////////////////////////////////////////	
	public boolean supportsProperty(DIDLObject object) {
		if(!base.isInstance(object)) {
			return false;
		}
		return true;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Does the given DIDLObject actually have this property defined.
	//////////////////////////////////////////////////////////////////////////////////
	public boolean hasProperty(DIDLObject object) throws Exception {
		if(!supportsProperty(object)) {
			return false;
		}
		
		String value = valueFrom(object);
		if(value != null)
			return true;
		
		return false;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	// Attempts to call method/retrieve field and convert into a string.
	//////////////////////////////////////////////////////////////////////////////////	
	public String valueFrom(DIDLObject object) throws Exception {
		Object result;
		
		if(!base.isInstance(object)) {
			throw new NoSuchPropertyException();
		}
		
		if(member instanceof Field) {
			result = ((Field) member).get(object);
		} else {
			result = ((Method)member).invoke(object);
		}
		if(result == null) return null;
		if(result instanceof String) return (String) result;
		if(result instanceof java.lang.Class) return ((Class<?>)result).getName();
		if(result instanceof DIDLObject.Class) return ((DIDLObject.Class)result).getValue();
		if(result instanceof Number) return String.valueOf(result);
		
		return (String) result;
	}
	
	/*
	 * Results: If an object has a property, that property may be null or a value.
	 * 			If it does not, or cannot, we throw NoSuchProperty.
	 */
	public static String valueFrom(String propertyName, DIDLObject object) throws NoSuchPropertyException {
		try {
			for(PropertyRetriever r : values()) {
				if(r.name.equalsIgnoreCase(propertyName))
					return r.valueFrom(object);					
			}
			// System.err.println("No property support for " + propertyName);
		} catch(NoSuchPropertyException e) {
			throw e;
		} catch(Exception e) {
			
		}
		throw new NoSuchPropertyException();
	}
	
	/*
	 * Find the resulting ENUM matching a name.
	 */
	public static PropertyRetriever find(String propertyName) {
		for(PropertyRetriever r : values()) {
			if(r.name.equalsIgnoreCase(propertyName))
				return r;
		}
		return null;
	}
}
