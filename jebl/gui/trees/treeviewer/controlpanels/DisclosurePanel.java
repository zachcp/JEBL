package jebl.gui.trees.treeviewer.controlpanels;

import jebl.gui.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class DisclosurePanel extends JPanel {

	public DisclosurePanel(final Controls controls, boolean isOpen) {
        setLayout(new BorderLayout());

		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {

			public void paint(Graphics graphics) {
				graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};

		final DisclosureButton button = new DisclosureButton();

		panel1.add(button);
		panel1.add(new JLabel(controls.getTitle()));

		add(panel1, BorderLayout.NORTH);

		add(controls.getPanel(), BorderLayout.CENTER);

		button.setSelected(isOpen);
		controls.getPanel().setVisible(isOpen);

		button.addDisclosureListener(new DisclosureListener() {
			public void opening() {
			}

			public void opened() {
				controls.getPanel().setVisible(true);
				controls.setVisible(true);
			}

			public void closing() {
			}

			public void closed() {
				controls.getPanel().setVisible(false);
				controls.setVisible(false);
			}
		});
	}

	private static BufferedImage background = null;

	static {
		try {
			background = IconUtils.getBufferedImage(DisclosurePanel.class, "images/titleBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}
