package org.amp.mediaserver;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.mock.MockUpnpService;

public class MockMediaServerApplet extends MediaServerApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7865009159269183784L;
	
	@Override
	public UpnpService createUpnpService() {
		return new MockUpnpService();
	}
}
