package org.virion.jam.controlpanels;

import javax.swing.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class Controls {

	public Controls(String title, JPanel panel, boolean isVisible) {
		this(title, panel, isVisible, false);
	}

	public Controls(String title, JPanel panel, boolean isVisible, boolean isPinned) {
		this.title = title;
		this.panel = panel;
		this.isVisible = isVisible;
		this.isPinned = isPinned;
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

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean pinned) {
		isPinned = pinned;
	}

	private String title;
	private JPanel panel;
	private boolean isVisible;

	private boolean isPinned;
}
