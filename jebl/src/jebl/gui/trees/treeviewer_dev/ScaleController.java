package jebl.gui.trees.treeviewer_dev;

import jebl.gui.trees.treeviewer_dev.painters.*;
import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class ScaleController extends AbstractController {

	public enum ScaleType {
	    SCALE_BAR("Scale Bar"),
	    FORWARDS_AXIS("Forwards Axis"),
		BACKWARDS_AXIS("Backwards Axis");

	    ScaleType(String name) {
	        this.name = name;
	    }

	    public String toString() {
	        return name;
	    }

	    private final String name;
	}

	private static final String CONTROLLER_TITLE = "Scale";

    private static Preferences PREFS = Preferences.userNodeForPackage(ScaleController.class);

	private static final String CONTROLLER_KEY = "scale";

    private static final String SCALE_TYPE_KEY = "scaleType";

    // The defaults if there is nothing in the preferences
    private static String DEFAULT_SCALE = ScaleType.SCALE_BAR.name();

    public ScaleController(final TreeViewer treeViewer) {

        this.treeViewer = treeViewer;

        final ScaleType defaultScale = ScaleType.valueOf(PREFS.get(CONTROLLER_KEY + "." + SCALE_TYPE_KEY, DEFAULT_SCALE));

	    scaleBarPainter = new ScaleBarPainter();
	    scaleBarPainterController = new ScaleBarPainterController(scaleBarPainter);

		forwardsScaleAxisPainter = new ScaleAxisPainter();
		forwardsScaleAxisPainterController = new ScaleAxisPainterController(forwardsScaleAxisPainter);

		backwardsScaleAxisPainter = new ScaleAxisPainter();
		backwardsScaleAxisPainterController = new ScaleAxisPainterController(backwardsScaleAxisPainter);

	    titleCheckBox = new JCheckBox(CONTROLLER_TITLE);
	    titleCheckBox.setSelected(scaleBarPainter.isVisible());

	    optionsPanel = new OptionsPanel(0, 0, "SmallSystemFont");

        scaleTypeCombo = new JComboBox(ScaleType.values());

        optionsPanel.addSpanningComponent(scaleTypeCombo);

        optionsPanel.addSeparator();

        scalePanel = new JPanel(new BorderLayout());
        scalePanel.setOpaque(false);
        setScaleType(defaultScale);

        optionsPanel.addSpanningComponent(scalePanel);

	    titleCheckBox.addChangeListener(new ChangeListener() {
	        public void stateChanged(ChangeEvent changeEvent) {
				scaleBarPainter.setVisible(titleCheckBox.isSelected());
		        forwardsScaleAxisPainter.setVisible(titleCheckBox.isSelected());
		        backwardsScaleAxisPainter.setVisible(titleCheckBox.isSelected());
	        }
	    });

        scaleTypeCombo.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent event) {
		        switch ((ScaleType)scaleTypeCombo.getSelectedItem()) {
			        case SCALE_BAR:
				        treeViewer.setScalePainter(scaleBarPainter);
				        break;
			        case FORWARDS_AXIS:
				        treeViewer.setScalePainter(forwardsScaleAxisPainter);
				        break;
			        case BACKWARDS_AXIS:
				        treeViewer.setScalePainter(backwardsScaleAxisPainter);
				        break;
		        }
	        }
        });


    }

    public JComponent getTitleComponent() {
	    return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return true;
    }

    public void initialize() {
        // nothing to do
    }

    public void setSettings(Map<String,Object> settings) {
	    String scaleTypeName = (String)settings.get(CONTROLLER_KEY + "." + SCALE_TYPE_KEY);
        final ScaleType scaleType = ScaleType.valueOf(scaleTypeName);
	    scaleTypeCombo.setSelectedItem(scaleType);

        // These controllers are internal to ScaleController so settings must be done here
        scaleBarPainterController.setSettings(settings);
        forwardsScaleAxisPainterController.setSettings(settings);
        backwardsScaleAxisPainterController.setSettings(settings);
    }

    public void getSettings(Map<String, Object> settings) {
	    final ScaleType scaleType = (ScaleType)scaleTypeCombo.getSelectedItem();
        settings.put(CONTROLLER_KEY + "." + SCALE_TYPE_KEY, scaleType.name());

        // These controllers are internal to ScaleController so settings must be done here
        scaleBarPainterController.getSettings(settings);
        forwardsScaleAxisPainterController.getSettings(settings);
        backwardsScaleAxisPainterController.getSettings(settings);
    }

    private void setScaleType(ScaleType scaleType) {
        switch (scaleType) {
	        case SCALE_BAR:
		        treeViewer.setScalePainter(scaleBarPainter);
		        scalePanel.removeAll();
		        scalePanel.add(scaleBarPainterController.getPanel(), BorderLayout.CENTER);
		        fireControllerChanged();
		        break;
	        case FORWARDS_AXIS:
		        treeViewer.setScalePainter(forwardsScaleAxisPainter);
		        scalePanel.removeAll();
		        scalePanel.add(forwardsScaleAxisPainterController.getPanel(), BorderLayout.CENTER);
		        fireControllerChanged();
		        break;
	        case BACKWARDS_AXIS:
		        treeViewer.setScalePainter(backwardsScaleAxisPainter);
		        scalePanel.removeAll();
		        scalePanel.add(backwardsScaleAxisPainterController.getPanel(), BorderLayout.CENTER);
		        fireControllerChanged();
		        break;
            default:
                new RuntimeException("Unknown ScaleType: " + scaleType);
        }

    }


    private JComboBox scaleTypeCombo;

    private final JPanel scalePanel;

	private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private final ScaleBarPainter scaleBarPainter;
	private final ScaleAxisPainter forwardsScaleAxisPainter;
	private final ScaleAxisPainter backwardsScaleAxisPainter;

    private final ScaleBarPainterController scaleBarPainterController;
    private final ScaleAxisPainterController forwardsScaleAxisPainterController;
	private final ScaleAxisPainterController backwardsScaleAxisPainterController;

    private final TreeViewer treeViewer;

}