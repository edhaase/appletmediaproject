package org.amp.mediaserver.contentdirectory.structure;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.amp.mediaserver.FileExtension;
import org.amp.mediaserver.support.AvailabilityChangeListener;
import org.amp.mediaserver.support.FileNamespace;
import org.amp.mediaserver.support.PostProcessingTags;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.VideoItem;
import org.seamless.util.MimeType;

public class VideoContainer extends ContainerTemplate implements AvailabilityChangeListener<File> {
	public final static String id = "2";
	
	final Container allVideosContainer;
	final Container videoFolders;
	
	// static javax.media.Manager manager = new Manager();
	
	public VideoContainer() {
		super();
		setParentID(RootContainer.id);
		setId(id);
		setTitle("Videos");
		setCreator("System");
		setClazz(RootContainer.storageClass);
		setRestricted(true);
        setSearchable(true);
		setWriteStatus(WriteStatus.NOT_WRITABLE);
        setChildCount(2);
        
		getSearchClasses().add( new DIDLObject.Class("object.container") );
		getSearchClasses().add( new DIDLObject.Class("object.item.videoItem") );		
		getSearchClasses().add( new DIDLObject.Class("object.container.storageFolder") );
		getSearchClasses().add( new DIDLObject.Class("object.item.videoItem.videoBroadcast") );
		
		allVideosContainer = new Container("8", this, "All Video", null, RootContainer.storageClass, null);		
		allVideosContainer.setRestricted(true);
		allVideosContainer.setSearchable(true);
		allVideosContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		allVideosContainer.getSearchClasses().add( new DIDLObject.Class("object.item.videoItem") );		
		allVideosContainer.getSearchClasses().add( new DIDLObject.Class("object.item.videoItem.videoBroadcast") );
		addContainer(allVideosContainer);
		
		videoFolders = new Container("15", this, "Folders", null, RootContainer.storageClass, null);
		videoFolders.setRestricted(true);
		videoFolders.setSearchable(true);
		videoFolders.setWriteStatus(WriteStatus.NOT_WRITABLE);
		videoFolders.getSearchClasses().add( new DIDLObject.Class("object.container") );
		videoFolders.getSearchClasses().add( new DIDLObject.Class("object.item.videoItem") );		
		videoFolders.getSearchClasses().add( new DIDLObject.Class("object.container.storageFolder") );
		videoFolders.getSearchClasses().add( new DIDLObject.Class("object.item.videoItem.videoBroadcast") );
		addContainer(videoFolders);
	}

	public void add(Object source, File file) {
		MimeType[] mimes = FileExtension.find(file).type;
		if(mimes == null) return;			
		if(!mimes[0].getType().equalsIgnoreCase("video")) return;
		
		VideoItem item;
	
			
		//////////////////////////////////////////////////////////////////////////////////
		// Generate our default resource(s).
		//////////////////////////////////////////////////////////////////////////////////
		ProtocolInfo protocol;
		URI uri;		
		Res res = null;
		
		List<Res> list = new ArrayList<Res>();
				
		for(int i=0; i<mimes.length; i++) {
			uri = URI.create(String.format("http://%s/%s$%d", PostProcessingTags.HTTPSERVER.string, FileNamespace.get(file), i));
			protocol = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mimes[i].toString(), "*");
			res = new Res(protocol, (long)file.length(), uri.toASCIIString());
			list.add(res);
		}
		//////////////////////////////////////////////////////////////////////////////////
		// Create video item, add to containers.
		//////////////////////////////////////////////////////////////////////////////////
		item = new VideoItem( UUID.randomUUID().toString().toUpperCase(), 
							  allVideosContainer, file.getName(),
							  null, (Res[])null );
		
		item.setResources(list);

		allVideosContainer.addItem( item );	
			
		Item clone = new VideoItem(item);		
		clone.setParentID(videoFolders.getId());
		clone.setId(UUID.randomUUID().toString().toUpperCase());
		clone.setRefID(item.getId());
		videoFolders.addItem(clone);
	}

	@Override
	public void remove(Object source, File file) {	
		Iterator<Container> citer = getContainers().iterator();
		while(citer.hasNext()) {
			Container c = citer.next();
			Iterator<Item> iiter = c.getItems().iterator();
			while(iiter.hasNext()) {
				if(iiter.next().getTitle().equalsIgnoreCase(file.getName())) {
					iiter.remove();					
					c.setChildCount(c.getItems().size() + c.getContainers().size());
				}
			}
		}			
	}

	
}
