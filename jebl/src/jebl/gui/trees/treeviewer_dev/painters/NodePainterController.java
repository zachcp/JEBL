package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.Controller;
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
public class NodePainterController extends AbstractController {

    public enum NodeShape {
        CIRCLE("Circle"),
        BAR("Bar");

        NodeShape(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

        private final String name;
    }

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

        shapeCombo = new JComboBox(new NodeShape[] {
                NodeShape.CIRCLE,
                NodeShape.BAR
        });

        String[] attributes = nodePainter.getAttributes();

        displayAttributeCombo = new JComboBox(attributes);
        displayAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayAttributeCombo.getSelectedItem();
                nodePainter.setDisplayAttribute(NodeBarPainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayLowerAttributeCombo = new JComboBox(attributes);
        displayLowerAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayLowerAttributeCombo.getSelectedItem();
                nodePainter.setDisplayAttribute(NodeBarPainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayUpperAttributeCombo = new JComboBox(attributes);
        displayUpperAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayUpperAttributeCombo.getSelectedItem();
                nodePainter.setDisplayAttribute(NodeBarPainter.UPPER_ATTRIBUTE, attribute);
            }
        });

        nodePainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayLowerAttributeCombo.removeAllItems();
                displayUpperAttributeCombo.removeAllItems();
                for (String name : nodePainter.getAttributes()) {
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
        switch ((NodeShape) shapeCombo.getSelectedItem()) {
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

    private final NodePainter nodePainter;
}
