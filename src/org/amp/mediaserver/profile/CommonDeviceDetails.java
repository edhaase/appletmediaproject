package org.amp.mediaserver.profile;
import java.net.URI;
import java.util.UUID;

import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;

public class CommonDeviceDetails extends DeviceDetails {

	public CommonDeviceDetails() {	
		super(
				"AMP: Mediaserver",
				new ManufacturerDetails("SmallGain", "https://github.com/edhaase"),	// This seems to be important?
				new ModelDetails("Digital Media Server", null, "1.0", URI.create("http://appletmediaproject.com")),
				"{" + UUID.randomUUID().toString().toUpperCase() + "}",
				null,
				null, null );		
	}
		
}
