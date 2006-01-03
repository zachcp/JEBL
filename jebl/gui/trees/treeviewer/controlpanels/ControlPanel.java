package jebl.gui.trees.treeviewer.controlpanels;

import jebl.gui.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ControlPanel extends JToolBar {

	public ControlPanel(int preferredWidth) {
		this(preferredWidth, false);
	}

    public ControlPanel(int preferredWidth, boolean initiallyClosed) {
        super(JToolBar.VERTICAL);
        this.preferredWidth = preferredWidth;
        this.initiallyClosed = initiallyClosed;
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }


	public Dimension getPreferredSize() {
		return new Dimension(preferredWidth, super.getPreferredSize().height);
	}

	public void clearControlsProviders() {
		providers.clear();
	}

	public void addControlsProvider(ControlsProvider provider) {
		providers.add(provider);
	}

	public void setupControls() {
		removeAll();
		for (ControlsProvider provider : providers) {
			for (Controls controls : provider.getControls()) {
				addControls(controls);
			}
		}
	}

	private void addControls(Controls controls) {
		final JPanel panel = new JPanel(new BorderLayout());

		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {

			public void paint(Graphics graphics) {
				graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
				super.paint(graphics);
			}
		};

		DisclosureButton button = new DisclosureButton();

		panel1.add(button);
		panel1.add(new JLabel(controls.getTitle()));

		panel.add(panel1, BorderLayout.NORTH);

		final JPanel panel2 = controls.getPanel();
		panel.add(panel2, BorderLayout.CENTER);

        if (initiallyClosed) {
            button.setSelected(false);
            panel2.setVisible(false);
        } else {
            button.setSelected(true);
            panel2.setVisible(true);
        }

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panel);

		button.addDisclosureListener(new DisclosureListener() {
			public void opening() {
			}

			public void opened() {
				panel2.setVisible(true);
				panel.invalidate();
			}

			public void closing() {
			}

			public void closed() {
				panel2.setVisible(false);
				panel.invalidate();
			}
		});

	}

	private int preferredWidth;
    private boolean initiallyClosed;
    private List<ControlsProvider> providers = new ArrayList<ControlsProvider>();

	private static BufferedImage background = null;

	static {
		try {
			background = IconUtils.getBufferedImage(ControlPanel.class, "images/titleBackground.png");

		} catch (Exception e) {
			// no icons...
		}
	}
}
