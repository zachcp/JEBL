package org.virion.jam.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ColorWellButton extends JButton {

	public ColorWellButton(Color color, final String colorChooserTitle) {
		super();
		putClientProperty("JButton.buttonType", "toolbar");
		putClientProperty("Quaqua.Button.style", "colorWell");
		setIcon(new ColorWell(color));
		setMargin(new Insets(8, 8, 8, 8));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Color color = JColorChooser.showDialog(JOptionPane.getFrameForComponent(ColorWellButton.this),
						colorChooserTitle,
						((ColorWell)getIcon()).color);
				if (color != null) {
					setSelectedColor(color);
				}
			}
		});
	}

	public Color getSelectedColor() {
		return ((ColorWell)getIcon()).color;
	}

	public void setSelectedColor(Color color) {
		((ColorWell)getIcon()).color = color;
		repaint();
	}

	private class ColorWell implements Icon {
		Color color;

		ColorWell(Color color) {
			super();
			this.color = color;
		}

		public int getIconWidth() {
			return 20;
		}

		public int getIconHeight() {
			return 20;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (color == null) return;
			g.setColor(color);
			g.fillRect(x, y, getIconWidth(), getIconHeight());
			g.setColor(color.darker());
			g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
		}
	}

}
