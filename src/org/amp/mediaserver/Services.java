package org.amp.mediaserver;
import java.util.logging.Logger;

import org.amp.mediaserver.contentdirectory.ContentDirectoryService;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.xmicrosoft.AbstractMediaReceiverRegistrarService;



public class Services {
	//////////////////////////////////////////////////////////////////////////////
	// Provided services.
	//////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public final LocalService<ContentDirectoryService> contentService = new AnnotationLocalServiceBinder().read(ContentDirectoryService.class);
	
	@SuppressWarnings("unchecked")
	public final LocalService<ConnectionManagerService> connectionService = new AnnotationLocalServiceBinder().read(ConnectionManagerService.class);
	
	@SuppressWarnings("unchecked")
	public final LocalService<AbstractMediaReceiverRegistrarService> mediaReceiverService = new AnnotationLocalServiceBinder().read(AbstractMediaReceiverRegistrarService.class);
	
	final private static Logger log = Logger.getLogger(Services.class.getName());
	
	//////////////////////////////////////////////////////////////////////////////
	// Service retrieval
	//////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("rawtypes")
	public LocalService[] toArray() {
		return new LocalService[] { contentService, connectionService, mediaReceiverService };
	}
	
	
	//////////////////////////////////////////////////////////////////////////////
	// Constructor
	//////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Services() {
		 try {
				/////////////////////////////////////////////////////////////
				// Create content directory service.
				/////////////////////////////////////////////////////////////	    	
	    		contentService.setManager(
	    				new DefaultServiceManager<ContentDirectoryService>(contentService, ContentDirectoryService.class)
		    				{
								/////////////////////////////////////////////////////////////
								// This allows us to specify additional creation parameters.
								/////////////////////////////////////////////////////////////
		    					@Override
		                        protected ContentDirectoryService createServiceInstance() throws Exception {
		                            // return new ContentDirectoryService(getFilesystem());
		    						log.fine("Creating content directory service");
		    						return new ContentDirectoryService();
		                        }        					
		    				}
	    				);
	        		    		
	    		
				/////////////////////////////////////////////////////////////
				// Create connection service, override protocols.
				/////////////////////////////////////////////////////////////
	    		connectionService.setManager(
	    				new DefaultServiceManager<ConnectionManagerService>(connectionService, ConnectionManagerService.class)
	        				{
	        					 @Override
	        				        protected ConnectionManagerService createServiceInstance() throws Exception {
	        						 	log.fine("Creating connection manager service");
	        				            return new ConnectionManagerService(new ProtocolInfosImpl(), null);
	        				        }
	        				}
	    				);
	        	
	    		
	    		
	        		
				/////////////////////////////////////////////////////////////
				// Create media receiver registrar service for Xbox 360.
				/////////////////////////////////////////////////////////////  		    		    
	    		mediaReceiverService.setManager( new DefaultServiceManager(mediaReceiverService, AbstractMediaReceiverRegistrarService.class) );
				 
	    		   		
		 } catch(Exception e) {
			 e.printStackTrace();
		 }
	}
	
}
