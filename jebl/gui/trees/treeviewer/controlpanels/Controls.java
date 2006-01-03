package jebl.gui.trees.treeviewer.controlpanels;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Controls {

	public Controls(String title, JPanel panel) {
		this.title = title;
		this.panel = panel;
	}

	public String getTitle() {
		return title;
	}

	public JPanel getPanel() {
		return panel;
	}

	private String title;
	private JPanel panel;
}
