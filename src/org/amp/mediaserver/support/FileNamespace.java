/*
 * @author Edward Haase
 * 	This thing assigns unique keys to files so we don't expose absolute file paths when offering files.
 * 	Using a digest such as a SHA-1 gives us a Deterministic system (This will reduce possible glitches). 
 */
package org.amp.mediaserver.support;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * A bi-directional synchronized mapping of file objects and UUID names.
 */
public class FileNamespace implements AvailabilityChangeListener<File> {
	public ConcurrentHashMap<String, File> content = new ConcurrentHashMap<String,File>();
	public ConcurrentHashMap<File, String> reverse = new ConcurrentHashMap<File,String>();
	
	final private static Logger log = Logger.getLogger(FileNamespace.class.getName());
	
	static public File get(String key) {
		return getInstance().content.get(key);
	}
	
	static public String get(File file) {
		return getInstance().reverse.get(file);
	}


	@Override
	public void add(Object source, File file) {
		String key = UUID.randomUUID().toString().toUpperCase();
		if(content.contains(key)) {
			log.warning("UUID key overlap, attempting to regenerate.");
			key = UUID.randomUUID().toString().toUpperCase();
			if(content.contains(key)) throw new IllegalStateException("Unable to generate UUID");
		}
		
		content.put(key, file);
		reverse.put(file, key);
		log.info("New file: " + file.getName() + " - " + key);
	}


	@Override
	public void remove(Object source, File file) {
		if (!reverse.containsKey(file))
			return;

		String key = get(file);
		content.remove(key);
		reverse.remove(file);
	}

	private static FileNamespace instance = null;
	protected FileNamespace() { }

	public static FileNamespace getInstance() {
		if (instance == null) {
			instance = new FileNamespace();
		}
		return instance;
	}
}
