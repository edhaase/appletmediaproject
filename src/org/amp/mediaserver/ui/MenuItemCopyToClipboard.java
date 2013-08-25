package org.amp.mediaserver.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;

import org.amp.mediaserver.httpcontentserver.HttpContentServer;
import org.amp.mediaserver.support.FileNamespace;


class MenuItemCopyToClipboard extends JMenuItem implements ActionListener {
	
	private ContentList list = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = -9014382352230319558L;

	public MenuItemCopyToClipboard(final ContentList list) {
		super("Copy URL to clipboard");
		this.list = list;
		addActionListener(this);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		ListSelectionModel selectionModel = list.getSelectionModel();
		
		if(selectionModel.isSelectionEmpty()) return;
		
		File file = (File)list.contentModel.items.get( selectionModel.getMinSelectionIndex() );

		String myString = null;
		try {
			myString = String.format("http://127.0.0.1:%d/%s", HttpContentServer.getInstance().getPort(),  FileNamespace.get(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		StringSelection stringSelection = new StringSelection(myString);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard ();
		clipboard.setContents (stringSelection, null);
	}
}


