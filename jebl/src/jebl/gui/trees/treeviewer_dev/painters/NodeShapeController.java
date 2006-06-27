package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.controlpalettes.AbstractController;
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
public class NodeShapeController extends AbstractController {

    public NodeShapeController(String title, final NodeShapePainter nodeShapePainter) {
        this.title = title;
        this.nodeShapePainter = nodeShapePainter;

        optionsPanel = new OptionsPanel();

        titleCheckBox = new JCheckBox(getTitle());

        titleCheckBox.setSelected(this.nodeShapePainter.isVisible());

        titleCheckBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                final boolean selected = titleCheckBox.isSelected();
                nodeShapePainter.setVisible(selected);
            }
        });

        shapeCombo = new JComboBox(new NodeShapePainter.NodeShape[] {
                NodeShapePainter.NodeShape.CIRCLE,
                NodeShapePainter.NodeShape.BAR
        });

        String[] attributes = this.nodeShapePainter.getAttributes();

        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayLowerAttributeCombo = new JComboBox(attributes);
        displayLowerAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayLowerAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayUpperAttributeCombo = new JComboBox(attributes);
        displayUpperAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayUpperAttributeCombo.getSelectedItem();
                nodeShapePainter.setDisplayAttribute(NodeShapePainter.UPPER_ATTRIBUTE, attribute);
            }
        });

        this.nodeShapePainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayLowerAttributeCombo.removeAllItems();
                displayUpperAttributeCombo.removeAllItems();
                for (String name : nodeShapePainter.getAttributes()) {
                    displayLowerAttributeCombo.addItem(name);
                    displayUpperAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
            }
        });
        setupOptions();

        shapeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                setupOptions();
                optionsPanel.validate();
            }
        });

    }

    private void setupOptions() {
        optionsPanel.removeAll();
        optionsPanel.addComponentWithLabel("Shape:", shapeCombo);
        switch ((NodeShapePainter.NodeShape) shapeCombo.getSelectedItem()) {
            case CIRCLE:
                optionsPanel.addComponentWithLabel("Radius:", displayAttributeCombo);
                break;
            case BAR:
                optionsPanel.addComponentWithLabel("Lower:", displayLowerAttributeCombo);
                optionsPanel.addComponentWithLabel("Upper:", displayUpperAttributeCombo);


                break;
        }
        fireControllerChanged();
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
    private JComboBox displayLowerAttributeCombo;
    private JComboBox displayUpperAttributeCombo;

    public String getTitle() {
        return title;
    }

    private final String title;

    private final NodeShapePainter nodeShapePainter;
}
