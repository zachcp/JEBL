package org.virion.jam.controlpanels;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Controls {

	public Controls(String title, JPanel panel, boolean isVisible) {
		this.title = title;
		this.panel = panel;
		this.isVisible = isVisible;
	}

	public String getTitle() {
		return title;
	}

	public JPanel getPanel() {
		return panel;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean visible) {
		isVisible = visible;
	}

	private String title;
	private JPanel panel;
	private boolean isVisible;
}
