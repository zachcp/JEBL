package jebl.gui.trees.treeviewer.controlpanels;

import jebl.gui.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DisclosurePanel extends JPanel {

	public DisclosurePanel(final Controls controls, boolean isOpen) {

		this.controls = controls;

        setLayout(new BorderLayout());

		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {

			public void paint(Graphics graphics) {
				graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};

		button = new DisclosureButton();

		panel1.add(button);
		panel1.add(new JLabel(controls.getTitle()));

		add(panel1, BorderLayout.NORTH);

		add(controls.getPanel(), BorderLayout.CENTER);

		button.setSelected(isOpen);
		controls.getPanel().setVisible(isOpen);

		button.addDisclosureListener(new DisclosureListener() {
			public void opening(Component component) {
				fireOpening();
			}

			public void opened(Component component) {
				controls.getPanel().setVisible(true);
				controls.setVisible(true);
				fireOpened();
			}

			public void closing(Component component) {
				fireClosing();
			}

			public void closed(Component component) {
				controls.getPanel().setVisible(false);
				controls.setVisible(false);
				fireClosed();
			}
		});
	}

	public void setOpen(boolean isOpen) {
		button.setSelected(isOpen);
		controls.getPanel().setVisible(isOpen);
		controls.setVisible(isOpen);
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
	private final Controls controls;
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
