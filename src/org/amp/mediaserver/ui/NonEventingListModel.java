package org.amp.mediaserver.ui;

import java.io.File;
import java.util.ArrayList;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 * This is a ListModel that doesn't fire interval-changed messages.
 * 		This means a JList object will not revalidate/repaint when content changes and
 * 		must be done manually. This solves a lag issue in the UI.
 */
public class NonEventingListModel implements ListModel<File> {

	public ArrayList<File> items = new ArrayList<File>();
	
	@Override
	public File getElementAt(int arg0) {
		return items.get(arg0);
	}

	@Override
	public int getSize() {
		return items.size();
	}


	/*
	 * While we can't throw an unsupported error here, we can quietly ignore the operation.
	 */
	@Override
	public void addListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeListDataListener(ListDataListener arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
