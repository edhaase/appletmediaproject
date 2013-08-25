package org.amp.mediaserver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.concurrent.ExecutionException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.amp.mediaserver.httpcontentserver.HttpContentServer;
import org.amp.mediaserver.profile.DetailsProvider;
import org.amp.mediaserver.support.AvailabilityChangeBroadcaster;
import org.amp.mediaserver.support.FileNamespace;
import org.amp.mediaserver.support.SystemLogHandler;
import org.amp.mediaserver.ui.ContentList;
import org.amp.mediaserver.ui.ContextMenu;
import org.amp.mediaserver.ui.ContextMenuClickListener;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.UDAVersion;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;



public class MediaServerApplet extends JApplet {
	

	private static final long serialVersionUID = 1L;
	private static final UDN udn = UDN.uniqueSystemIdentifier("AMP-MediaServer");
	
	protected LocalDevice localDevice = null;
	protected UpnpService upnpService = null;
	protected Services    services = null;
	protected AvailabilityChangeBroadcaster<File> broadcaster = new AvailabilityChangeBroadcaster<File>();
	
	protected ContentList contentList = null;
	protected JScrollPane scrollPane = null;
	protected ContextMenu contextMenu = null;
	
	final private static Logger log = Logger.getLogger(MediaServerApplet.class.getName());
	

	@Override
	public String[][] getParameterInfo() {
		final String param_info[][] = { 
											{ "disable_logging", "void", "Existence of this param disables jVM console logging for this applet" }, 
											{ "max_http_conn", "OFF or 0-INTEGER.MAX", "Maximum number of http connections before server waits" },
									  };
	    return param_info;
	}
	
	//////////////////////////////////////////////////////////////////////
	// Applet lifecycle init.
	//////////////////////////////////////////////////////////////////////
	public void init() {		
		try {				
			if( getParameter("disable_logging") != null ) {
				LogManager.getLogManager().readConfiguration( MediaServerApplet.class.getResourceAsStream("/logging-off.properties") );
			} else {
				LogManager.getLogManager().readConfiguration( MediaServerApplet.class.getResourceAsStream("/logging.properties") );	
			}
			Logger.getLogger("").addHandler(new SystemLogHandler());	
		} catch (SecurityException | IOException e2) {
			e2.printStackTrace();
			onError();
			return;
		}
		
		log.info("Initializing applet media project");
		
		//////////////////////////////////////////////////////////////////////
		// Other stuff.
		//////////////////////////////////////////////////////////////////////
		// log.fine("Generating user interface");
		contentList = new ContentList(broadcaster);
		contentList.setEnabled(false);
						
		scrollPane = new JScrollPane(contentList);
		contextMenu = new ContextMenu(contentList);
		contentList.addMouseListener(new ContextMenuClickListener(contentList, contextMenu));
				
		add(scrollPane);
				
		ServiceLoadWorker worker = new ServiceLoadWorker() {						
			@Override
			public void done() {
				Boolean result = false;
				
				try {
					result = get();
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				//////////////////////////////////////////////////////////////////////
				// ..Top of page alert provided by twitter bootstrap?
				//////////////////////////////////////////////////////////////////////
				if (result == false) {
					onError();
				} else {
					onReady();
				}
			}			
		};
		
		
		worker.execute();
	
		// UIManager		
		broadcaster.list.add(FileNamespace.getInstance());							
	}


	public void onError() {
		URL url;
		try {
			url = new URL("javascript:errorAlert()");
			getAppletContext().showDocument(url);
		} catch (MalformedURLException e) {	}		
		
		// Clean up after an error.
		// remove(scrollPane);
		add(new JLabel("Whoops.",
				UIManager.getIcon("OptionPane.errorIcon"),
				JLabel.CENTER));
		
		if(upnpService != null) {
			shutdown();
		}
		broadcaster.list.clear();
	}
	
	public void onReady() {
		URL url;
		try {
			url = new URL("javascript:readyToShare()");
			getAppletContext().showDocument(url);
		} catch (MalformedURLException e) { }
	}
	
	//////////////////////////////////////////////////////////////////////
	// Applet lifecycle destroy.
	//////////////////////////////////////////////////////////////////////
	public void destroy() {		
		// log.fine("Lifecycle destroy");

		//////////////////////////////////////////////////////////////////////
		// Launch shutdown on another thread so we don't hang the GUI.
		//////////////////////////////////////////////////////////////////////
		new Thread(new Runnable() {
			public void run() {
				shutdown();
			}
		});
		
	}

	public UpnpService createUpnpService() {				
		return new UpnpServiceImpl(new UpnpServiceConfigurationImpl());
	}
	
	public synchronized void shutdown() {
		// Log shutdown starting.
		if (upnpService != null) {			
			upnpService.shutdown();
			upnpService = null;
		}
		
		try {
			HttpContentServer.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// Run this on a seperate thread to keep from overloading the GUI/EDT.
	//////////////////////////////////////////////////////////////////////
	class ServiceLoadWorker extends SwingWorker<Boolean,Void>
	{
		@Override
		protected Boolean doInBackground() throws Exception {
			try {
				services = new Services();

				localDevice = new LocalDevice( new DeviceIdentity(udn, 5),
											   new UDAVersion(1, 0),
											   new UDADeviceType("MediaServer", 1),
											   new DetailsProvider(),
											   Icons.get(),
											   services.toArray(), null );

				upnpService = createUpnpService();

				//////////////////////////////////////////////////////////////////////
				// HTTP content server creation, with custom parameters.
				//////////////////////////////////////////////////////////////////////
				String maxConn = getParameter("max_http_conn");
				if(maxConn != null) {
					// System.out.println("Setting maximum http connections.");
					if(maxConn.equalsIgnoreCase("OFF")) {
						log.info("Creating free-range HTTP server");
						// httpService = new ContentServerWithExecutor();
						HttpContentServer.start(0);
					} else {						
						int value = Integer.valueOf(maxConn).intValue();
						if(value < 1) value = 1;
						log.info("Maximum connections allowed: " + value);
						// httpService = new ContentServerWithExecutor(value);
						HttpContentServer.start(value);
					}					 
				} else {
					// No parameter set? Let's default to a limit 75 at a time.
					// This is running out a browser, to be fair.
					// httpService = new ContentServerWithExecutor(75);
					HttpContentServer.start(75);
				}
				broadcaster.list.add(HttpContentServer.getInstance());
								
				//////////////////////////////////////////////////////////////////////
				// Add device.
				//////////////////////////////////////////////////////////////////////
				upnpService.getRegistry().addDevice(localDevice);

				// services.contentService.getManager().getImplementation().
				broadcaster.list.add(services.contentService.getManager().getImplementation());
				
				//////////////////////////////////////////////////////////////////////
				// Let's establish a warning message so the user doesn't prematurely
				// end their sharing. By providing this code here, instead of in the
				// host html, we can be sure that this code will only be loaded when
				// the applet reaches a full running state. -- But it's not critical.
				//////////////////////////////////////////////////////////////////////
				try {
					String stuff = "window.addEventListener(\"beforeunload\", function (e) {"
							+ " var confirmationMessage = \"Navigating away from this page will shutdown the media server.\"; "
							+ "(e || window.event).returnValue = confirmationMessage;"
							+ "					  return confirmationMessage;  });";

					URL s = new URL("javascript:" + stuff);
					getAppletContext().showDocument(s);
				} catch (MalformedURLException e) {
					// This isn't a critical component. Ignore any failure.
				}			

				contentList.setEnabled(true);
				return true;
			} catch (AccessControlException se) {
				//////////////////////////////////////////////////////////////////////
				// Access control exceptions will occur when the applet is
				// running inside a sandbox. When this happens, we need to provide a
				// visual clue that something is wrong.
				//////////////////////////////////////////////////////////////////////
				se.printStackTrace();
			} catch (ValidationException e) {
				//////////////////////////////////////////////////////////////////////
				// This shouldn't occur if we're doing our testing.
				//////////////////////////////////////////////////////////////////////				
				e.printStackTrace();						
			} catch (Exception e) {
				//////////////////////////////////////////////////////////////////////
				// Something broke and we are not prepared for it.
				//////////////////////////////////////////////////////////////////////				
				e.printStackTrace();
			}
			
			return false;
		}
	}

}
