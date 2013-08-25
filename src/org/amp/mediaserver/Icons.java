package org.amp.mediaserver;
import java.io.IOException;

import org.fourthline.cling.model.meta.Icon;


public class Icons {
				
	public static Icon[] get() {
		try {
			return new Icon[] {										
					new Icon("image/jpeg", 32, 32, 24,  "icon32x32.jpg", Icon.class.getResourceAsStream("/Icon32x32.jpg")),
					new Icon("image/png", 32, 32, 24,  "icon32x32.png", Icon.class.getResourceAsStream("/Icon32x32.png")),
					new Icon("image/bmp", 32, 32, 24,  "icon32x32.bmp", Icon.class.getResourceAsStream("/Icon32x32.bmp")),
					
					new Icon("image/jpeg", 48, 48, 24,  "icon48x48.jpg", Icon.class.getResourceAsStream("/Icon48x48.jpg")),
					new Icon("image/png", 48, 48, 24,  "icon48x48.png", Icon.class.getResourceAsStream("/Icon48x48.png")),
					new Icon("image/bmp", 48, 48, 24,  "icon48x48.bmp", Icon.class.getResourceAsStream("/Icon48x48.bmp")),
					
					new Icon("image/jpeg", 120, 120, 24,  "icon120x120.jpg", Icon.class.getResourceAsStream("/Icon120x120.jpg")),
					new Icon("image/png", 120, 120, 24,  "icon120x120.png", Icon.class.getResourceAsStream("/Icon120x120.png"))
			};
		} catch (IOException e) {			
			e.printStackTrace();
		}	
		return null;
	}
	
}
