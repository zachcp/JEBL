package org.virion.jam.disclosure;

import org.virion.jam.util.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DisclosurePanel extends JPanel {

	/**
	 *
	 * @param title The title of the panel
	 * @param panel The contents of the panel
	 * @param isOpen Whether the panel should start open
	 */
	public DisclosurePanel(final String title, final JPanel panel, boolean isOpen) {
        this(new JLabel(title), panel, isOpen, 50);
    }

	/**
	 *
	 * @param titleComponent The component to use as the title of the panel
	 * @param panel The contents of the panel
	 * @param isOpen Whether the panel should start open
	 * @param openSpeed The opening speed in milliseconds
	 */
    public DisclosurePanel(final JComponent titleComponent, final JPanel panel,
	                       boolean isOpen, int openSpeed) {

        this.panel = panel;

        setLayout(new BorderLayout());

		button = new DisclosureButton(openSpeed);

        this.titleComponent = titleComponent;

		JPanel panel1 = new JPanel(new BorderLayout(6, 0)) {

			public void paint(Graphics graphics) {
				graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};

		panel1.add(button, BorderLayout.WEST);
		panel1.add(titleComponent, BorderLayout.CENTER);
        add(panel1, BorderLayout.NORTH);

		add(panel, BorderLayout.CENTER);

		button.setSelected(isOpen);
		panel.setVisible(isOpen);

		button.addDisclosureListener(new DisclosureListener() {
			public void opening(Component component) {
				fireOpening();
			}

			public void opened(Component component) {
				panel.setVisible(true);
				fireOpened();
			}

			public void closing(Component component) {
				fireClosing();
			}

			public void closed(Component component) {
				panel.setVisible(false);
				fireClosed();
			}
		});
	}

	public void setOpen(boolean isOpen) {
		button.setSelected(isOpen);
		panel.setVisible(isOpen);
	}

	public void addDisclosureListener(DisclosureListener listener) {
		listeners.add(listener);
	}

	public void removeDisclosureListener(DisclosureListener listener) {
		listeners.remove(listener);
	}

	private void fireOpening() {
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((DisclosureListener)iter.next()).opening(this);
		}
	}

	private void fireOpened() {
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((DisclosureListener)iter.next()).opened(this);
		}
	}

	private void fireClosing() {
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((DisclosureListener)iter.next()).closing(this);
		}
	}

	private void fireClosed() {
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((DisclosureListener)iter.next()).closed(this);
		}
	}

	public DisclosureButton getDisclosureButton() {
		return button;
	}

	public Component getTitleComponent() {
		return titleComponent;
	}

	public JPanel getContentsPanel() {
		return panel;
	}

	private final DisclosureButton button;
    private final Component titleComponent;
    private final JPanel panel;
	private final java.util.List listeners = new ArrayList();

	private static BufferedImage background = null;

	static {
		try {
			background = IconUtils.getBufferedImage(DisclosurePanel.class, "images/titleBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}
