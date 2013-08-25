package org.amp.mediaserver;

import java.io.File;
import java.util.ArrayList;

import org.seamless.util.MimeType;

/**
 *  List of acceptable file extensions.
 * 		HttpContentServer uses first provided mime type if not otherwise specified.
 * 		ContentDirectoryService structure generator will attempt to build a resource of each mime type,
 * 		so to increase likelyhood something can play it.
 */
public enum FileExtension {		
	ATRAC	(".aa3",	"audio/x-sony-oma"),
	WAV		(".wav",	"audio/wav", "audio/x-wav", "audio/wave"),
	MP3		(".mp3",	"audio/mpeg"),
	WMA		(".wma",	"audio/x-ms-wma"),	
	OGG		(".ogg", 	"audio/ogg", "audio/vorbis", "application/ogg", "audio/vorbis-config"),				
	OGA		(".oga", 	"audio/ogg", "audio/vorbis", "application/ogg", "audio/vorbis-config"),
	FLAC	(".flac",	"audio/flac", "audio/x-flac"),
	
	MPEG	(".mpeg",	"video/mpeg"),
	MPG		(".mpg",	"video/mpeg"),
	MP1		(".mp1",	"video/mpeg"),
	MPV		(".mpv",	"video/mpeg"),	
	AVI		(".avi",	"video/avi", "video/msvideo", "video/x-msvideo"),
	WMV		(".wmv",	"video/x-ms-wmv"),
	DVR		(".dvr-ms",	"video/x-ms-dvr"),
	DIVX	(".divx",	"video/divx"),
	XVID	(".xvid",	"video/divx"),
	MKV		(".mkv",	"video/x-matroska"),	
	
	PNG		(".png",	"image/png"),
	JPG		(".jpg",	"image/jpeg"),
	JPEG	(".jpeg",	"image/jpeg"),
	BMP		(".bmp",	"image/bmp"),
	TIFF	(".tiff",	"image/tiff"),
	GIF		(".gif",	"image/gif");
		
	
	
	final public String ext;
	final public String[] mime;
	final public MimeType[] type;
	
	FileExtension(final String ext, final String mime) {
		this.ext = ext.toLowerCase();
		this.mime = new String[] { mime };		
		this.type = new MimeType[] { MimeType.valueOf(mime) };
	}	
	
	FileExtension(final String ext, final String ... mimes) {			
		this.ext = ext.toLowerCase();
		this.mime = mimes;
			
		ArrayList<MimeType> a = new ArrayList<MimeType>();
		for(String m : mimes) {
			a.add(MimeType.valueOf(m));
		}
		this.type = (MimeType[]) a.toArray(new MimeType[a.size()]);		
	}
	
	public static FileExtension find(final String filename) {
		String name = filename.toLowerCase();
		for(FileExtension e : values()) {
			if( name.endsWith(e.ext) ) return e;
		}
		return null;
	}
	
	public static FileExtension find(File file) {
		return find(file.getName());
	}
	
	public static boolean matchesAny(String filename) {
		return (find(filename) == null)?false:true;
	}
}
