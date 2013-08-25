package org.amp.mediaserver.profile;

import java.util.UUID;

import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DLNADoc;

public class XboxDeviceDetails extends DeviceDetails {

	/*
	 * This is the structure required by the Xbox360.
	 *   This, along with MediaReceiverRegistar, appears to be the only important details.
	 *   The serial number doesn't seem to matter.
	 */
	public XboxDeviceDetails() {
		super(
				"AMP: Mediaserver:",
				new ManufacturerDetails("Microsoft Corporation", "http://www.microsoft.com"),
				new ModelDetails("Windows Media Player Sharing", null, "12.0"),
				"{" + UUID.randomUUID().toString().toUpperCase() + "}",
				null,				
				new DLNADoc[] { new DLNADoc("DMS", DLNADoc.Version.V1_5) }, null );
	}
	
}
