package org.amp.mediaserver.contentdirectory.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.container.Container;

public class Util {
	
	/////////////////////////////////////////////////////////////
	// Flatten a structure so we can see everything as a list.
	/////////////////////////////////////////////////////////////
	public static List<DIDLObject> flatten(final DIDLObject object) {
		List<DIDLObject> list = new LinkedList<DIDLObject>();
				
		list.add(object);
			
		if(object instanceof Container) {
			for(Container c : ((Container)object).getContainers()) {
				list.addAll(flatten(c));
			}
			
			list.addAll( ((Container)object).getItems() );
		}
		
		return list;		
	}
	
	
	/////////////////////////////////////////////////////////////
	// Give us a HashMap of ID values to objects (Quick-find)
	/////////////////////////////////////////////////////////////
	public static Map<String, DIDLObject> mapByID(final DIDLObject object) {
		Map<String, DIDLObject> map = new HashMap<String, DIDLObject>();
		
		for(DIDLObject subobject : flatten(object)) {
			map.put(subobject.getId(), subobject);
		}
						
		return map;
	}
	
	/////////////////////////////////////////////////////////////
	// Map by clazz (Because.. whatever).
	/////////////////////////////////////////////////////////////
	public static Map<String, List<DIDLObject>> mapByClazz(DIDLObject object) {
		Map<String, List<DIDLObject>> map = new HashMap<String, List<DIDLObject>>();
		
		for(DIDLObject sub : flatten(object)) {
			if(!map.containsKey(sub.getClazz().getFriendlyName())) {
				map.put(sub.getClazz().getFriendlyName(), new LinkedList<DIDLObject>());
			}
			
			map.get(sub.getClazz().getFriendlyName()).add(sub);
		}
						
		return map;
	}
		
}
