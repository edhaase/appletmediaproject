package org.amp.mediaserver;

import java.util.TreeSet;

import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.ProtocolInfos;

/**
 * Protocol information linked to supported file extensions.
 */
public class ProtocolInfosImpl extends ProtocolInfos {

	
	private static final long serialVersionUID = 1086280766438765510L;

	/////////////////////////////////////////////////////////////////////////////////
	// Helper class.
	/////////////////////////////////////////////////////////////////////////////////
	static private class HttpProtocolInfo extends ProtocolInfo
	{
		public HttpProtocolInfo(String mimeType, String additional) {
			super( Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mimeType, additional );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////
	// Constructor.
	/////////////////////////////////////////////////////////////////////////////////
	public ProtocolInfosImpl()
	{		
		// Alternative choices here are HashSet or LinkedHashSet.
		TreeSet<String> types = new TreeSet<String>();
		for(FileExtension f : FileExtension.values()) {
			for(String mime : f.mime) {
				types.add(mime);
			}
		}

		for(String type : types) {
			add(new HttpProtocolInfo(type, "*"));
		}				
	}
	
}
