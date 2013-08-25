package org.amp.mediaserver.contentdirectory.structure;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.amp.mediaserver.FileExtension;
import org.amp.mediaserver.support.AvailabilityChangeListener;
import org.amp.mediaserver.support.FileNamespace;
import org.amp.mediaserver.support.PostProcessingTags;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.container.MusicArtist;
import org.fourthline.cling.support.model.container.StorageFolder;
import org.fourthline.cling.support.model.item.AudioItem;
import org.fourthline.cling.support.model.item.Item;
import org.fourthline.cling.support.model.item.MusicTrack;
import org.seamless.util.MimeType;

public class AudioContainer extends ContainerTemplate implements AvailabilityChangeListener<File> {
	
	public final static String id = "1";
	
	final Container allMusicContainer;
	
	
	public AudioContainer() {
		super();		 
		setParentID( RootContainer.id );
		setId(id);
		setTitle("Music");
		setClazz(RootContainer.storageClass);
		setRestricted(true);
		setSearchable(true);
        setWriteStatus(WriteStatus.NOT_WRITABLE);       
        
        // addProperty(new UPNP.LONG_DESCRIPTION("Available description"));
        
        getSearchClasses().add(RootContainer.storageClass);
		getSearchClasses().add(AudioItem.CLASS);		
		getSearchClasses().add(StorageFolder.CLASS);
		getSearchClasses().add(MusicTrack.CLASS);
		getSearchClasses().add(MusicArtist.CLASS);
		
		allMusicContainer = new Container("4", this, "All music", null, RootContainer.storageClass, null);		
		allMusicContainer.setRestricted(true);
		allMusicContainer.setSearchable(true);
		allMusicContainer.setChildCount(0);
		allMusicContainer.setWriteStatus(WriteStatus.NOT_WRITABLE);			
		allMusicContainer.getSearchClasses().add(AudioItem.CLASS);
		allMusicContainer.getSearchClasses().add(MusicTrack.CLASS);
		addContainer(allMusicContainer);
		
		setChildCount(getContainers().size());
	}
	
	@Override
	public void add(Object source, File file) {
		MimeType[] mimes = FileExtension.find(file).type;
		if(mimes == null) return;			
		if(!mimes[0].getType().equalsIgnoreCase("audio")) return;
		
		AudioItem item;
		
		LinkedList<Res> resList = new LinkedList<Res>();
		//////////////////////////////////////////////////////////////////////////////////
		// Generate our default resource.
		//////////////////////////////////////////////////////////////////////////////////		
		ProtocolInfo protocol;
		URI uri = null;
		Res res;
				
		for(int i=0; i<mimes.length; i++) {
			uri = URI.create(String.format("http://%s/%s$%d", PostProcessingTags.HTTPSERVER.string, FileNamespace.get(file), i));
			protocol = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, mimes[i].toString(), "*");
			res = new Res(protocol, (long)file.length(), uri.toASCIIString());
			resList.add(res);
		}

		
		//////////////////////////////////////////////////////////////////////////////////
		// For wave audio, let's supply an L16 resource so the xbox plays nice.
		//		(No, seriously, the XBOX360 requires audio/L16 for WAVs.
		// TODO: Currently the HTTP content server does supply this as L16. It doesn't
		// stop anything, so we'll deal with it later.
		//////////////////////////////////////////////////////////////////////////////////
		if( mimes.equals(FileExtension.WAV.type) ) {
			/* for(Type t : AudioSystem.getAudioFileTypes()) {
				System.out.println("Type supported: " + t.getExtension() + " - " + t.toString() + " - " + t.getClass().getName());
			} */
			uri = URI.create(String.format("http://%s/%s", PostProcessingTags.HTTPSERVER.string, FileNamespace.get(file)));						
			protocol = new ProtocolInfo(Protocol.HTTP_GET, ProtocolInfo.WILDCARD, "audio/L16",  "*");
			res = new Res(protocol, (long)file.length(), uri.toASCIIString());
			
			//////////////////////////////////////////////////////////////////////////////////
			// The L16 resource requires sample rate and channels. Luckily PCM is
			// supported without additional libraries. I think.
			//////////////////////////////////////////////////////////////////////////////////
			try {
				AudioFileFormat format = AudioSystem.getAudioFileFormat(file);				
				AudioFormat f = format.getFormat();
				res.setSampleFrequency((long) f.getSampleRate());
				res.setNrAudioChannels((long) f.getChannels());
				res.setBitsPerSample((long) f.getSampleSizeInBits());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			resList.add(res);
		}
		
		//////////////////////////////////////////////////////////////////////////////////
		// Create audio item, add to containers.
		//////////////////////////////////////////////////////////////////////////////////
		item = new AudioItem( UUID.randomUUID().toString().toUpperCase(),
				  			  allMusicContainer.getId(),
				  			  file.getName(),
				  			  null,
				  			  (Res[])null );
		
		item.setResources(resList);		
		
		allMusicContainer.addItem( item );			
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
