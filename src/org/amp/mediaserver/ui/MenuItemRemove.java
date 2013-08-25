package org.amp.mediaserver.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;


class MenuItemRemove extends JMenuItem implements ActionListener {

	private static final long serialVersionUID = 4426783089584267254L;

	private final ContentList list;
	
	MenuItemRemove(final ContentList list) {
		super("Remove");
		this.list = list;
		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ListSelectionModel selectionModel = list.getSelectionModel();
		
		if(selectionModel.isSelectionEmpty()) return;
		
		if( selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_INTERVAL_SELECTION
		 || selectionModel.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION ) {
	
			for(int i = selectionModel.getMinSelectionIndex(); i< selectionModel.getMaxSelectionIndex()+1; i++) {
				File file = list.contentModel.getElementAt(i);
				
				if(list.listener != null)
					list.listener.remove(list, file);
			}
			
			list.contentModel.items.subList(selectionModel.getMinSelectionIndex(),
											selectionModel.getMaxSelectionIndex()+1).clear();
			selectionModel.clearSelection();				
		} else {				
			int[] selectedIndices = list.getSelectedIndices();	
			for (int i = selectedIndices.length - 1; i >= 0; i--) {
				File file = list.contentModel.items.remove(i);	
				if(list.listener != null)
					list.listener.remove(list, file);
			}
			selectionModel.clearSelection();
		}
		
		list.revalidate();
		list.repaint();
	}
}