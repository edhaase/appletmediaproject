package org.amp.mediaserver.contentdirectory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.amp.mediaserver.contentdirectory.operator.Predicate;
import org.amp.mediaserver.contentdirectory.properties.PropertyRetriever;
import org.amp.mediaserver.contentdirectory.sort.SortCriterionComparator;
import org.amp.mediaserver.contentdirectory.structure.RootContainer;
import org.amp.mediaserver.contentdirectory.structure.Util;
import org.amp.mediaserver.httpcontentserver.HttpContentServer;
import org.amp.mediaserver.support.AvailabilityChangeListener;
import org.amp.mediaserver.support.PostProcessingTags;
import org.fourthline.cling.model.profile.RemoteClientInfo;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.fourthline.cling.support.contentdirectory.ContentDirectoryException;
import org.fourthline.cling.support.contentdirectory.DIDLParser;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.BrowseResult;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

/**
 * ContentDirectoryService implementation. 
 */
public class ContentDirectoryService extends AbstractContentDirectoryService implements AvailabilityChangeListener<File> {

	/**
	 * Error codes as specified by UPnP-av-ContentDirectory-v1-Service 
	 */
	public static final int NO_SUCH_OBJECT = 701;					// The specified ObjectID is invalid.
	public static final int UNSUPPORTED_SEARCH_CRITERIA = 708;		// The search criteria specified is not supported or is invalid.
	public static final int UNSUPPORTED_SORT_CRITERIA = 709;		// The sort criteria specified is not supported or is invalid.
	public static final int UNSUPPORTED_SEARCH_CONTAINER  = 710;	// The specified ContainerID is invalid or identifies an object that is not a container.
	public static final int RESTRICTED_OBJECT = 711;				// Operation failed because the restricted attribute of object is set to true.
	public static final int BAD_METADATA = 712;						// Operation fails because it would result in invalid or disallowed metadata in current object.
	public static final int RESTRICTED_PARENT_OBJECT = 713;			// Operation failed because the restricted attribute of parent object is set to true.
	public static final int CANNOT_PROCESS = 720;					// Cannot process the request.
	
	
	final private static Logger log = Logger.getLogger(ContentDirectoryService.class.getName());
	
	protected RootContainer rootContainer = null; 
	protected volatile Map<String,DIDLObject> idMap = null;
	protected Timer timer = new Timer();
		
	/////////////////////////////////////////////////////////////
	// Originally written in as a way to keep from rebuilding
	// the entire structure every time the available files changed,
	// this timer now just prevents us from spamming the system
	// update id.
	/////////////////////////////////////////////////////////////
	volatile boolean filesystemDirtyBit;
	class StructureRebuilder extends TimerTask {
		@Override
		public void run() {
			if(!filesystemDirtyBit)
				return;

			filesystemDirtyBit = false;
			log.finest("StructureRebuilder");
			idMap = Util.mapByID(rootContainer);
			changeSystemUpdateID();			
		}		
	}
	
	/////////////////////////////////////////////////////////////
	// Constructor runs on first browse call.
	/////////////////////////////////////////////////////////////
	public ContentDirectoryService() throws Exception {
		super();

		log.info("Startup");
			
		for(PropertyRetriever r : PropertyRetriever.values()) {
			log.finer( String.format("Adding %s to search capabilities", r.name) );
			getSearchCapabilities().add( r.name );
		}
		
		// Only add the ones we know work for now.		
		getSortCapabilities().add( PropertyRetriever.DC_TITLE.name );
		getSortCapabilities().add( PropertyRetriever.UPNP_ARTIST.name );
		
		timer.schedule(new StructureRebuilder(), 2000, 2000);
		rootContainer = new RootContainer();
	}
		
	
	/////////////////////////////////////////////////////////////
	// Browse call handler.
	/////////////////////////////////////////////////////////////
	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag,
			String filterStr, long firstResult, long maxResults,
			SortCriterion[] orderby, RemoteClientInfo clientInfo)
			throws ContentDirectoryException {
		
		try {
			DIDLParser parser = new DIDLParser();
			MyDIDLContent didlContent = new MyDIDLContent();
			SortCriterionComparator comparator = new SortCriterionComparator(orderby);

			
			//////////////////////////////////////////////////////////////////////////////////
			// Sanity.
			//////////////////////////////////////////////////////////////////////////////////
			if (getRootContainer() == null || idMap == null) {
				return new BrowseResult(parser.generate(didlContent), 0, 0);
			}

			DIDLObject didlObject = idMap.get(objectID);
			if (didlObject == null) {
				log.info("Requesting non-existant item/container: " + objectID);
				return new BrowseResult(parser.generate(didlContent), 0, 0);
			}

			//////////////////////////////////////////////////////////////////////////////////
			// Browse meta data.
			//////////////////////////////////////////////////////////////////////////////////
			long numReturned = 0;
			long totalMatches = 0;

			if (browseFlag.equals(BrowseFlag.METADATA)) {
				if (didlObject instanceof Container) {
					didlContent.addContainer((Container) didlObject);
					numReturned = totalMatches = 1;
				} else if (didlObject instanceof Item) {
					didlContent.addItem((Item) didlObject);
					numReturned = totalMatches = 1;
				}
			}
			//////////////////////////////////////////////////////////////////////////////////
			// Browse children.
			//////////////////////////////////////////////////////////////////////////////////
			else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
				if (didlObject instanceof Container) {
					Container container = (Container) didlObject;
					// Collections.sort(arg0, arg1)
					// Quick and dirty sorting. Not pretty. But kind of neat.
					TreeSet<DIDLObject> set = new TreeSet<DIDLObject>(comparator);
					set.addAll(container.getContainers());
					set.addAll(container.getItems());
					didlContent.addCollection(set, maxResults);
					

					numReturned = didlContent.getContainers().size() + didlContent.getItems().size();
					totalMatches = container.getContainers().size()	+ container.getItems().size();
				}
			}

			//////////////////////////////////////////////////////////////////
			// Generate results.
			//////////////////////////////////////////////////////////////////
			String browseResult = parser.generate(didlContent);

			//////////////////////////////////////////////////////////////////
			// Post-processing.
			//////////////////////////////////////////////////////////////////
			browseResult = browseResult.replace(PostProcessingTags.HTTPSERVER.string, 
					clientInfo.getLocalAddress().getHostAddress() + ":" + HttpContentServer.getInstance().getPort());

			
			//////////////////////////////////////////////////////////////////////////////////
			// And.. return result.
			//////////////////////////////////////////////////////////////////////////////////
			return new BrowseResult(browseResult, numReturned, totalMatches);

		} catch (ContentDirectoryException e) {
			throw e; // Handled by Cling.
		} catch (Exception e) {
			e.printStackTrace();
			// System.err.println(getClass().getName() + ": " + e.getMessage());
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
		}

	}
	
	//////////////////////////////////////////////////////////////////
	// Search requests (Required for WMP).
	//////////////////////////////////////////////////////////////////
	@Override
	public BrowseResult search(String containerId, String searchCriteriaStr,
			String filterStr, long firstResult, long maxResults,
			SortCriterion[] orderBy, RemoteClientInfo clientInfo)
			throws ContentDirectoryException {
		
		DIDLParser parser = new DIDLParser();
		MyDIDLContent didlContent = new MyDIDLContent();
		SortCriterionComparator comparator = new SortCriterionComparator(orderBy);

		log.info("Search Criteria: " + searchCriteriaStr);

		//////////////////////////////////////////////////////////////////
		// Find root container.
		//////////////////////////////////////////////////////////////////
		if (containerId.equals(""))
			containerId = "0";

		try {
			//////////////////////////////////////////////////////////////////////////////////////////////
			// Lie to the Xbox360. We're going to pretend there are containers here that really aren't,
			// since we'll never populate them. Just return always empty. Other devices now don't have
			// to deal with looking at a bunch of empty folders in their hierarchy.
			//////////////////////////////////////////////////////////////////////////////////////////////
			if (clientInfo.isXbox360Request()
			&& (containerId.equals("5") || containerId.equals("6")	|| containerId.equals("7") || containerId.equals("F"))) {
				return new BrowseResult(parser.generate(didlContent), 0, 0);
			}

			//////////////////////////////////////////////////////////////////////
			// If we made it this far, let's actually process the search request.
			//////////////////////////////////////////////////////////////////////
			if (getRootContainer() == null || idMap == null) {
				return new BrowseResult(parser.generate(didlContent), 0, 0);
			}

			DIDLObject object = idMap.get(containerId);
			if (object == null || object instanceof Item) {
				throw new ContentDirectoryException(710,
						"No such container, or ID is not a container.");
			}

			//////////////////////////////////////////////////////////////////
			// Perform search.
			//////////////////////////////////////////////////////////////////
			Predicate searchCriteria = null;
			TreeSet<DIDLObject> results = new TreeSet<DIDLObject>(comparator);

			try {
				searchCriteria = SearchCriteria.parse(searchCriteriaStr);
			
				if (searchCriteria == null) {
					// System.err.println("Unable to parse search criteria: "	+ searchCriteriaStr);
					throw new ContentDirectoryException(
							ContentDirectoryService.UNSUPPORTED_SEARCH_CRITERIA,
							"Unsupported or invalid search criteria.");
				}

				for (DIDLObject child : Util.flatten(object)) {
					if (searchCriteria.isValidFor(child))
						results.add(child);
				}

				if (firstResult > results.size()) {
					// System.err.println("Invalid start position in search.getResults()");
					throw new ContentDirectoryException(
							ContentDirectoryService.UNSUPPORTED_SEARCH_CRITERIA,
							"Unsupported or invalid search criteria.");
				}

				// System.out.println("Search completed. " + results.size() + " result(s)");

				//////////////////////////////////////////////////////////////////
				// Package results.
				//////////////////////////////////////////////////////////////////
				if (results.size() > 0) {
					int upperIndex = (int) Math.min(maxResults + firstResult,
							results.size());
					List<DIDLObject> list = new LinkedList<DIDLObject>(results)
							.subList((int) firstResult, upperIndex);
					didlContent.addCollection(list);
				}
			} catch (ContentDirectoryException c) {
				// Whoops.
				throw c;
			} catch (Exception e) {
				// System.err.println("Unknown search error: " + e.getClass() +
				// " : " + e.getMessage());
				e.printStackTrace();
				throw new ContentDirectoryException(
						UNSUPPORTED_SEARCH_CRITERIA,
						"Unsupported or invalid search criteria.");
			}

			//////////////////////////////////////////////////////////////////
			// Generate results.
			//////////////////////////////////////////////////////////////////
			String browseResult = parser.generate(didlContent);

			//////////////////////////////////////////////////////////////////
			// Post-processing.
			//////////////////////////////////////////////////////////////////
			browseResult = browseResult.replace(PostProcessingTags.HTTPSERVER.string, 
					clientInfo.getLocalAddress().getHostAddress() + ":" + HttpContentServer.getInstance().getPort());
			

			//////////////////////////////////////////////////////////////////
			// Finalize and return.
			//////////////////////////////////////////////////////////////////
			return new BrowseResult(browseResult, didlContent.getContainers()
					.size() + didlContent.getItems().size(), results.size());

		} catch (ContentDirectoryException e) {
			throw e; // Handled by Cling.
		} catch (Exception e) {
			e.printStackTrace();
			throw new ContentDirectoryException(
					ContentDirectoryErrorCode.CANNOT_PROCESS, e.toString());
		} 
	}
	//////////////////////////////////////////////////////////////////////////////////
	// List data listener events for handling updates of available files.
	//////////////////////////////////////////////////////////////////////////////////
	protected Container getRootContainer() {
		if( (idMap == null) && (rootContainer != null) ) {
			idMap = Util.mapByID(rootContainer);
		}
		return rootContainer;		
	}
	 
	//////////////////////////////////////////////////////////////////////////////////
	// Listener events for handling updates of available files.
	//////////////////////////////////////////////////////////////////////////////////
	@Override
	public void add(Object source, File value) {
		rootContainer.add(source, value);
		filesystemDirtyBit = true;
	}

	@Override
	public void remove(Object source, File value) {
		rootContainer.remove(source, value);
		filesystemDirtyBit = true;
	}
	

}