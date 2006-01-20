package jebl.gui.trees.treeviewer.controlpanels;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ControlPalette extends JPanel {

	public enum DisplayMode {
		DEFAULT_OPEN, // The controls themselves specify whether they should start open or not
		INITIALLY_OPEN, // All controls start open
		INITIALLY_CLOSED, // All controls start closed
		ONLY_ONE_OPEN // Only one control is kept open at any time
	}

	public ControlPalette(int preferredWidth) {
		this(preferredWidth, DisplayMode.DEFAULT_OPEN);
	}

	public ControlPalette(int preferredWidth, DisplayMode displayMode) {
		this.preferredWidth = preferredWidth;
		this.displayMode = displayMode;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}


	public Dimension getPreferredSize() {
		return new Dimension(preferredWidth, super.getPreferredSize().height);
	}

	public void clearControlsProviders() {
		providers.clear();
	}

	public void addControlsProvider(ControlsProvider provider) {
		provider.setControlPanel(this);
		providers.add(provider);
	}

	public void fireControlsChanged() {
		for (ControlPaletteListener listener : listeners) {
			listener.controlsChanged();
		}
	}

	public void addControlPanelListener(ControlPaletteListener listener) {
		listeners.add(listener);
	}

	public void removeControlPanelListener(ControlPaletteListener listener) {
		listeners.remove(listener);
	}

	private final List<ControlPaletteListener> listeners = new ArrayList<ControlPaletteListener>();

	public void setupControls() {
		removeAll();
		disclosurePanels.clear();

		for (ControlsProvider provider : providers) {
			for (Controls controls : provider.getControls()) {
				addControls(controls);
			}
		}
		add(Box.createVerticalStrut(Integer.MAX_VALUE));
	}

	private void addControls(final Controls controls) {

		boolean open = true;

		switch (displayMode) {
			case DEFAULT_OPEN:
				open = controls.isVisible();
				break;
			case INITIALLY_CLOSED:
				open = false;
				break;
			case INITIALLY_OPEN:
				open = true;
				break;
			case ONLY_ONE_OPEN:
				if (currentlyOpen == disclosurePanels.size()) {
					open = true;
				} else {
					open = false;
				}
				break;
			default: throw new IllegalArgumentException("Unknown DisplayMode enum item");
		}

		final DisclosurePanel panel = new DisclosurePanel(controls, open);

		if (displayMode == DisplayMode.ONLY_ONE_OPEN) {
			panel.addDisclosureListener(new DisclosureListener() {
				public void opening(Component component) {
					if (currentlyOpen >= 0) {
						DisclosurePanel panel = disclosurePanels.get(currentlyOpen);
						Dimension size = panel.getSize();
						panel.setSize(size.width, size.height / 2);
					}

				}

				public void opened(Component component) {
					int newlyOpened = disclosurePanels.indexOf(component);
					if (currentlyOpen >= 0) {
						DisclosurePanel panel = disclosurePanels.get(currentlyOpen);
						currentlyOpen = newlyOpened;
						panel.setOpen(false);
					} else {
						currentlyOpen = newlyOpened;
					}
				}

				public void closing(Component component) { }

				public void closed(Component component) {
					int newlyClosed = disclosurePanels.indexOf(component);
					if (newlyClosed == currentlyOpen) {
						currentlyOpen = -1;
					}
				}
			});
		}

		disclosurePanels.add(panel);

		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(panel);
	}

	private int preferredWidth;
	private DisplayMode displayMode;
	private int currentlyOpen = 0;
	private List<ControlsProvider> providers = new ArrayList<ControlsProvider>();
	private List<DisclosurePanel> disclosurePanels = new ArrayList<DisclosurePanel>();

}
