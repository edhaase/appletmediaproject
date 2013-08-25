package org.amp.mediaserver.profile;

import java.util.logging.Logger;

import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.profile.HeaderDeviceDetailsProvider;
import org.fourthline.cling.model.profile.RemoteClientInfo;

public class DetailsProvider extends HeaderDeviceDetailsProvider {

	static final DeviceDetails common = new CommonDeviceDetails();
	static final DeviceDetails xbox360 = new XboxDeviceDetails();
	
	final private static Logger log = Logger.getLogger(DetailsProvider.class.getName());
	
	public DetailsProvider() {
		super(common, null);
		
		getHeaderDetails().put(new HeaderDeviceDetailsProvider.Key("User-Agent", "Xbox.*"), xbox360);
		getHeaderDetails().put(new HeaderDeviceDetailsProvider.Key("X-AV-Client-Info", ".*PLAYSTATION 3.*"), common);
		// getHeaderDetails().put(new HeaderDeviceDetailsProvider.Key("User-Agent", ".*Windows-Media-Player.*"), common);
	}
	
	public DeviceDetails provide(RemoteClientInfo info) {		
		log.info( String.format("User-agent of control point %s: %s", info.getRemoteAddress(), info.getRequestUserAgent()) );
		return super.provide(info);
	}
}
