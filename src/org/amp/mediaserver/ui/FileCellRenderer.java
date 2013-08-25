package org.amp.mediaserver.ui;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

class FileCellRenderer extends DefaultListCellRenderer {	
	private static final long serialVersionUID = 1L;
		
	public Component getListCellRendererComponent(JList<?> list,
			final Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if(c instanceof JLabel && value instanceof File) {
				JLabel l = (JLabel) c;
				File f = (File) value;
		
				l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));				
				l.setText(FileSystemView.getFileSystemView().getSystemDisplayName(f));
				l.setToolTipText(f.getAbsolutePath());												
		}
						
		return c;
	}
	
}
