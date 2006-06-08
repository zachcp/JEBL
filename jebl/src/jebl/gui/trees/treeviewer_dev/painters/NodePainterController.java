package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.Controller;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class NodePainterController implements Controller {

	public final static String BAR_SHAPE = "Bar";

    public NodePainterController(String title, final NodePainter nodePainter) {
        this.title = title;
        this.nodePainter = nodePainter;

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(nodePainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodePainter.setVisible(selected);
            }
        });

	    shapeCombo = new JComboBox(new String[] {
			    BAR_SHAPE
	    });
	    optionsPanel.addComponentWithLabel("Shape:", shapeCombo);

        String[] attributes = nodePainter.getAttributes();
        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodePainter.setDisplayAttribute(attribute);
            }
        });

        optionsPanel.addComponentWithLabel("Display:", displayAttributeCombo);

        nodePainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayAttributeCombo.removeAllItems();
                for (String name : nodePainter.getAttributes()) {
                    displayAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
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
        return false;
    }

    public void getSettings(ControllerSettings settings) {
    }

    public void setSettings(ControllerSettings settings) {
    }

    private final JCheckBox titleCheckBox;
    private final OptionsPanel optionsPanel;

	private JComboBox shapeCombo;
    private JComboBox displayAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodePainter nodePainter;
}
