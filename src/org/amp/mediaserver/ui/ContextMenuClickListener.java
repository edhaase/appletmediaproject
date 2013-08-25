package org.amp.mediaserver.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;


public class ContextMenuClickListener extends MouseAdapter {
	final JList<?> list;
	final ContextMenu menu;

	public ContextMenuClickListener(final ContentList list, final ContextMenu menu) {
		this.list = list;
		this.menu = menu;
	}

	public void mouseReleased(MouseEvent e) {
		if (list.getSelectedIndices().length > 0 && e.getButton() == MouseEvent.BUTTON3) {
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

}