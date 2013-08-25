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
import org.fourthline.cling.support.model.item.ImageItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.Photo;
import org.seamless.util.MimeType;

public class ImageContainer extends ContainerTemplate implements AvailabilityChangeListener<File> {

	public final static String id = "3";
	
	final Container allPhotosContainer;
	final Container imageFolders;
	
	public ImageContainer() {
		super();
		setParentID( RootContainer.id );
		setId(id);
		setTitle("Pictures");
		setClazz(RootContainer.storageClass);
		setRestricted(true);
		setSearchable(true);
		setWriteStatus(WriteStatus.NOT_WRITABLE);
		setChildCount(2);
		
		getSearchClasses().add( new DIDLObject.Class("object.item.imageItem.photo") );
		getSearchClasses().add( new DIDLObject.Class("object.container.album.photoAlbum") );
		getSearchClasses().add( new DIDLObject.Class("object.container.storageFolder") );
		getSearchClasses().add( new DIDLObject.Class("object.container.album") );
		getSearchClasses().add( new DIDLObject.Class("object.item.imageItem") );
				
		allPhotosContainer = new Container("B", this, "All Pictures", null, RootContainer.storageClass, null);		
		allPhotosContainer.setRestricted(true);
		allPhotosContainer.setSearchable(true);
		allPhotosContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);
		allPhotosContainer.getSearchClasses().add( new DIDLObject.Class("object.item.imageItem.photo") );
		allPhotosContainer.getSearchClasses().add( new DIDLObject.Class("object.container.album.photoAlbum") );
		allPhotosContainer.getSearchClasses().add( new DIDLObject.Class("object.item.imageItem") );
		addContainer(allPhotosContainer);
		
		imageFolders = new Container("16", this, "Folders", null, RootContainer.storageClass, null);
		imageFolders.setRestricted(true);
		imageFolders.setSearchable(true);		
		imageFolders.setWriteStatus(WriteStatus.NOT_WRITABLE);
		imageFolders.getSearchClasses().add( new DIDLObject.Class("object.item.imageItem.photo") );
		imageFolders.getSearchClasses().add( new DIDLObject.Class("object.container") );				
		imageFolders.getSearchClasses().add( new DIDLObject.Class("object.container.storageFolder") );
		imageFolders.getSearchClasses().add( new DIDLObject.Class("object.item.imageItem") );		
		addContainer(imageFolders);				
	}

	@Override
	public void add(Object source, File file) {
		MimeType[] mimes = FileExtension.find(file).type;
		if(mimes == null) return;			
	 	if(!mimes[0].getType().equalsIgnoreCase("image")) return;
		
		ProtocolInfo protocol;
		URI uri;
		Res res;
		List<Res> resList = new ArrayList<Res>();
		
		//////////////////////////////////////////////////////////////////////////////////
		// Generate our default resources.
		//////////////////////////////////////////////////////////////////////////////////		
		for(int i=0; i<mimes.length; i++) {
			uri = URI.create(String.format("http://%s/%s$%d", PostProcessingTags.HTTPSERVER.string, FileNamespace.get(file), i));
			protocol = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mimes[i].toString(), "*");
			res = new Res(protocol, (long)file.length(), uri.toASCIIString());
			resList.add(res);
		}

		
		//////////////////////////////////////////////////////////////////////////////////
		// Create image item, add to containers.
		//////////////////////////////////////////////////////////////////////////////////
		ImageItem item = new ImageItem( UUID.randomUUID().toString().toUpperCase(),
									   imageFolders, file.getName(),
									   null, (Res[])null);
		item.setResources(resList);
		
		item.setClazz(Photo.CLASS);
		imageFolders.addItem(item);

		ImageItem clone = new ImageItem(item);
		clone.setParentID(allPhotosContainer.getId());
		clone.setId(UUID.randomUUID().toString().toUpperCase());
		clone.setRefID(item.getId());
		clone.setClazz(Photo.CLASS);

		allPhotosContainer.addItem(clone);

		imageFolders.setChildCount(imageFolders.getItems().size());
		allPhotosContainer.setChildCount(allPhotosContainer.getItems().size());
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
