package org.virion.jam.disclosure;

import org.virion.jam.util.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * This is a panel that takes a main panel but allows a utility panel to slide
 * in from the top.
 *
 * @author Andrew Rambaut
 * @version $Id: FigTreePanel.java,v 1.13 2007/09/05 10:51:49 rambaut Exp $
 */
public class SlideOpenPanel extends JPanel {

	public SlideOpenPanel(JPanel mainPanel) {

		setOpaque(false);
		setLayout(new BorderLayout());

		topPanel = new JPanel(new BorderLayout()) {
			public void paint(Graphics graphics) {
				graphics.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};
		topPanel.setOpaque(false);
		topPanel.setVisible(false);
		topPanel.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray),
						BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);

		doneButton = new JButton(closeIcon);
		Dimension d = doneButton.getPreferredSize();
		doneButton.setPreferredSize(new Dimension(26, 26));
		adjustComponent(doneButton);

		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hideUtilityPanel();
			}
		});
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "doClose");
		getActionMap().put("doClose", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				doneButton.doClick();
			}
		});
	}

	public void showUtilityPanel(JPanel utilityPanel) {

		if (utilityPanel == null) {
			return;
		}

		this.utilityPanel = utilityPanel;

		topPanel.removeAll();
		topPanel.add(utilityPanel, BorderLayout.CENTER);
		topPanel.add(doneButton, BorderLayout.EAST);

		Dimension size = topPanel.getPreferredSize();

		target = utilityPanel.getPreferredSize().height + 9;
		size.height = 0;

		topPanel.setPreferredSize(size);
		topPanel.setVisible(true);
		hiding = false;
		startAnimation();
	}

	public void hideUtilityPanel() {
		target = 0;
		hiding = true;
		utilityPanel = null;
		startAnimation();
	}

	public JPanel getUtilityPanel() {
		return utilityPanel;
	}	

	protected void adjustComponent(JComponent comp) {
		// comp.putClientProperty("Quaqua.Component.visualMargin", new Insets(0,0,0,0));
		Font font = UIManager.getFont("SmallSystemFont");
		if (font != null) {
			comp.setFont(font);
		}
		comp.putClientProperty("JComponent.sizeVariant", "small");
		if (comp instanceof JButton) {
			comp.putClientProperty("JButton.buttonType", "roundRect");
			comp.setFocusable(false);
		}
		if (comp instanceof JComboBox) {
			comp.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
			comp.setFocusable(false);
		}
		if (comp instanceof JCheckBox) {
			comp.setFocusable(false);
		}
	}

	private void startAnimation() {
		timer = new Timer(animationSpeed, listener) {
            @Override
            public String toString() {
                return "SlideOpenPanelAnimationTimer";
            }
        };
		timer.setRepeats(true);
		timer.setCoalesce(false);
		timer.start();
	}

	private void stopAnimation() {
		if (timer == null) return;
		timer.stop();
		if (hiding) {
			topPanel.setVisible(false);
		}
	}

	ActionListener listener = new ActionListener() {

		public void actionPerformed(ActionEvent e) {

			int delta = (int)Math.ceil(((double)(target - topPanel.getHeight())) / 10.0);
			if (delta != 0) {
				Dimension size = topPanel.getPreferredSize();
				size.height += delta;
				topPanel.setPreferredSize(size);
				topPanel.revalidate();
				revalidate();
				repaint();
			} else {
				stopAnimation();
			}

		}
	};


	private final JPanel topPanel;
	private final JButton doneButton;

	private JPanel utilityPanel = null;

	private Timer timer = null;
	private int animationSpeed = 10;
	private int target;
	private boolean hiding;

	private static BufferedImage backgroundImage = null;
	private static Icon closeIcon;

	static {
		closeIcon = IconUtils.getIcon(SlideOpenPanel.class, "images/close.png");
		try {
			backgroundImage = IconUtils.getBufferedImage(SlideOpenPanel.class, "images/utilityBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}