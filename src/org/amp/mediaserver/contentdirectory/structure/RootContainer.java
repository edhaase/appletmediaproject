package org.amp.mediaserver.contentdirectory.structure;

import java.io.File;

import org.amp.mediaserver.support.AvailabilityChangeListener;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.WriteStatus;



public class RootContainer extends ContainerTemplate implements AvailabilityChangeListener<File> {

	public final static DIDLObject.Class storageClass = new DIDLObject.Class("object.container");
	public final static String id = "0";
	
	final AudioContainer audio;
	final VideoContainer video;
	final ImageContainer image;
	
	public RootContainer() {
		super();
		
		setParentID("-1");
		setId(id);
		setTitle("Root");
		setCreator("System");
		setClazz(storageClass);
		setRestricted(true);
		setSearchable(true);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
        
        audio = new AudioContainer();
        video = new VideoContainer();
        image = new ImageContainer();
        
        addContainer( audio );
        addContainer( video );
        addContainer( image );
	}

	@Override
	public void add(Object source, File value) {
		audio.add(source, value);
		video.add(source, value);
		image.add(source, value);
	}

	@Override
	public void remove(Object source, File value) {
		audio.remove(source, value);
		video.remove(source, value);
		image.remove(source, value);
	}

	
}
