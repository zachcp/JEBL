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

	public DisclosurePanel(final String title, final JPanel panel, boolean isOpen) {
        this(title, panel, isOpen, false, null);
    }

    /**
     *
     * @param controls
     * @param isOpen
     * @param fastBlueStyle draw labels using blue coloured text, and expand/contract quickly when the button is clicked.
     * @param checkbox A checkbox to be used instead of the default label next to the expand/contract button.
     */
    public DisclosurePanel(final String title, final JPanel panel, boolean isOpen, boolean fastBlueStyle, JCheckBox checkbox) {

        this.title = title;
        this.panel = panel;

        setLayout(new BorderLayout());

		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {

			public void paint(Graphics graphics) {
				graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};

		button = new DisclosureButton(fastBlueStyle? 10:150);

		panel1.add(button);
        JLabel label = new JLabel(title);
        if(fastBlueStyle) {
            label.setForeground(Color.BLUE);
        }
        if(checkbox != null) {
            if(fastBlueStyle) {
                checkbox.setForeground(Color.BLUE);
            }
            panel1.add(checkbox);
        }
        else {
            panel1.add(label);
        }

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

	private final DisclosureButton button;
    private final String title;
    private final JPanel panel;
	private final java.util.List<DisclosureListener> listeners = new ArrayList<DisclosureListener>();

	private static BufferedImage background = null;

	static {
		try {
			background = IconUtils.getBufferedImage(DisclosurePanel.class, "images/titleBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}
