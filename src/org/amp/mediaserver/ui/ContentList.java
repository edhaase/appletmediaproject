package org.amp.mediaserver.ui;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.TitledBorder;

import org.amp.mediaserver.FileExtension;
import org.amp.mediaserver.support.AvailabilityChangeListener;


@SuppressWarnings("serial")
public class ContentList extends JList<File> {
	
	protected NonEventingListModel contentModel = new NonEventingListModel();
	protected AvailabilityChangeListener<File> listener = null;
	
	//////////////////////////////////////////////////////////////////////
	// Constructor
	//////////////////////////////////////////////////////////////////////
	public ContentList(AvailabilityChangeListener<File> changeListener) {
		if(changeListener == null) {
			throw new IllegalArgumentException("changeListener cannot be null");
		}

		
		listener = changeListener;
		
		setModel(contentModel);
		setFixedCellHeight(getFontMetrics(getFont()).getHeight());		
		setDropMode(DropMode.INSERT);
		setDragEnabled(true);
		setAlignmentX(LEFT_ALIGNMENT);
		setCellRenderer(new FileCellRenderer());
		setTransferHandler(new ListTransferHandler());
		
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), "Shared media"));
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION); // Currently required for fast removal.
	}
	
	
	//////////////////////////////////////////////////////////////////////
	// Transfer handler
	//////////////////////////////////////////////////////////////////////
	class ListTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(TransferHandler.TransferSupport info) {
		
			if (!ContentList.this.isEnabled() || !info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}
			return true;
		}
	
		public void importObject(File file) {			
			if( ((File)file).isDirectory() ) {
				for( File f : file.listFiles() ) {
					importObject(f);
				}
			} else {				
				if (!contentModel.items.contains(file)) {
					if ( file.canRead() &&
						 FileExtension.matchesAny(file.getName())) {
						try {
							if (listener != null)
								listener.add(ContentList.this, file);
							contentModel.items.add((File) file);
						} catch(Exception e) {
							// We caught an error. Don't add it to the UI.
						}
					}
				}
			}			
		}
		

		//////////////////////////////////////////////////////////////////////
		// 
		//////////////////////////////////////////////////////////////////////
		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferHandler.TransferSupport info) {
			if (!info.isDrop()) {
				return false;
			}

			// Check for FileList flavor
			if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}

			// Get the fileList that is being dropped.
			Transferable t = info.getTransferable();
			List<File> data;
			try {
				data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
			} catch (Exception e) {
				return false;
			}

			for (Object file : data) {
				importObject((File)file);
			}
						
			revalidate();
			repaint();
			
			return true;
		}

	}
	
			

}
