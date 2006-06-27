package jebl.gui.trees.treeviewer_dev.treelayouts;

import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class RadialTreeLayoutController extends AbstractController {

	public RadialTreeLayoutController(final RadialTreeLayout treeLayout) {
		this.treeLayout = treeLayout;

		titleLabel = new JLabel("Radial Layout");
		optionsPanel = new OptionsPanel();

		final int sliderMax = 10000;
		spreadSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
		spreadSlider.setValue((int)(treeLayout.getSpread() * sliderMax / 2.0));
		spreadSlider.setMajorTickSpacing(spreadSlider.getMaximum() / 3);
		spreadSlider.setPaintTicks(true);

		spreadSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				double value = spreadSlider.getValue();
				treeLayout.setSpread((value / sliderMax));
			}
		});
		optionsPanel.addComponentWithLabel("Spread:", spreadSlider, true);
	}

	public JComponent getTitleComponent() {
		return titleLabel;
	}

	public JPanel getPanel() {
		return optionsPanel;
	}

	public boolean isInitiallyVisible() {
		return false;
	}

	public void setSettings(ControllerSettings settings) {
	}

	public void getSettings(ControllerSettings settings) {
	}

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	private final JSlider spreadSlider;

	private final RadialTreeLayout treeLayout;

}
