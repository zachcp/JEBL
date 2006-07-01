package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.*;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodeBarController extends AbstractController {

    public NodeBarController(String title, final NodeBarPainter nodeBarPainter) {
        this.title = title;
        this.nodeBarPainter = nodeBarPainter;

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(this.nodeBarPainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodeBarPainter.setVisible(selected);
            }
        });

        String[] attributeNames = this.nodeBarPainter.getAttributeNames();

        displayLowerAttributeCombo = new JComboBox(attributeNames);
        displayLowerAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayLowerAttributeCombo.getSelectedItem();
                nodeBarPainter.setLowerAttributeName(attribute);
            }
        });

        displayUpperAttributeCombo = new JComboBox(attributeNames);
        displayUpperAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayUpperAttributeCombo.getSelectedItem();
                nodeBarPainter.setUpperAttributeName(attribute);
            }
        });

        this.nodeBarPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayLowerAttributeCombo.removeAllItems();
                displayUpperAttributeCombo.removeAllItems();
                for (String name : nodeBarPainter.getAttributeNames()) {
                    displayLowerAttributeCombo.addItem(name);
                    displayUpperAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
            }
        });

	    if (nodeBarPainter.getLowerAttributeName() == null) {
		    for (String name : attributeNames) {
			    if (name.toUpperCase().contains("LOWER")) {
				    displayLowerAttributeCombo.setSelectedItem(name);
				    break;
			    }
		    }
	    }

	    if (nodeBarPainter.getUpperAttributeName() == null) {
		    for (String name : attributeNames) {
			    if (name.toUpperCase().contains("UPPER")) {
				    displayUpperAttributeCombo.setSelectedItem(name);
				    break;
			    }
		    }
	    }

        optionsPanel.addComponentWithLabel("Lower:", displayLowerAttributeCombo);
        optionsPanel.addComponentWithLabel("Upper:", displayUpperAttributeCombo);

	    branchLineWidthSpinner = new JSpinner(new SpinnerNumberModel(4.0, 0.01, 48.0, 1.0));

	    branchLineWidthSpinner.addChangeListener(new ChangeListener() {
		    public void stateChanged(ChangeEvent changeEvent) {
			    float lineWidth = ((Double) branchLineWidthSpinner.getValue()).floatValue();
			    nodeBarPainter.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		    }
	    });
	    optionsPanel.addComponentWithLabel("Line Weight:", branchLineWidthSpinner);

	    nodeBarPainter.setStroke(new BasicStroke(4.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

    }

    public JComponent getTitleComponent() {
        return titleCheckBox;
    }

    public JPanel getPanel() {
        return optionsPanel;
    }

    public boolean isInitiallyVisible() {
        return false;
    }

    public void getSettings(ControllerSettings settings) {
    }

    public void setSettings(ControllerSettings settings) {
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

    private JComboBox displayLowerAttributeCombo;
    private JComboBox displayUpperAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeBarPainter nodeBarPainter;

	private final JSpinner branchLineWidthSpinner;
}
