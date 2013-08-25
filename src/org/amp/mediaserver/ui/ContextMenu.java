package org.amp.mediaserver.ui;

import javax.swing.JPopupMenu;


@SuppressWarnings("serial")
public class ContextMenu extends JPopupMenu {

	public ContextMenu(final ContentList list) {		
		add(new MenuItemRemove(list));
		add(new MenuItemCopyToClipboard(list));
	}

}