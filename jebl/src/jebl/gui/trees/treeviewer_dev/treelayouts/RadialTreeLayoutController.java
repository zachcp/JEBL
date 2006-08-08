package jebl.gui.trees.treeviewer_dev.treelayouts;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.util.Map;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class RadialTreeLayoutController extends AbstractController {

	public RadialTreeLayoutController(final RadialTreeLayout treeLayout) {
		this.treeLayout = treeLayout;

		titleLabel = new JLabel("Radial Layout");
		optionsPanel = new OptionsPanel();

		final int sliderMax = 100;
		final JSlider spreadSlider = new JSlider(SwingConstants.HORIZONTAL, 0, sliderMax, 0);
		spreadSlider.setValue((int)(treeLayout.getSpread() * sliderMax / 2.0));

		spreadSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				double value = spreadSlider.getValue();
				treeLayout.setSpread((value / sliderMax));
			}
		});
		optionsPanel.addComponentWithLabel("Spread:", spreadSlider, true);

//		double spread = treeLayout.getSpread();
//		final JSpinner spreadSpinner = new JSpinner(new SpinnerNumberModel(spread, 0, 2, 0.01));
//
//		optionsPanel.addComponentWithLabel("Spread:", spreadSpinner, true);
//
//		spreadSpinner.addChangeListener(new ChangeListener() {
//		    public void stateChanged(ChangeEvent changeEvent) {
//		        final double spread = (Double)spreadSpinner.getValue();
//				treeLayout.setSpread(spread);
//		    }
//		});
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

    public void setSettings(Map<String,Object> settings) {
    }

    public void getSettings(Map<String, Object> settings) {
    }

	private final JLabel titleLabel;
	private final OptionsPanel optionsPanel;

	//private final JSlider spreadSlider;

	private final RadialTreeLayout treeLayout;

}
