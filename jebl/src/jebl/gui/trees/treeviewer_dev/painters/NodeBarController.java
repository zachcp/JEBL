package jebl.gui.trees.treeviewer_dev.painters;

import org.virion.jam.controlpalettes.AbstractController;
import org.virion.jam.controlpalettes.ControllerSettings;
import org.virion.jam.panels.OptionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

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

        String[] attributes = this.nodeBarPainter.getAttributes();

        displayLowerAttributeCombo = new JComboBox(attributes);
        displayLowerAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayLowerAttributeCombo.getSelectedItem();
                nodeBarPainter.setDisplayAttribute(NodeShapePainter.LOWER_ATTRIBUTE, attribute);
            }
        });

        displayUpperAttributeCombo = new JComboBox(attributes);
        displayUpperAttributeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                String attribute = (String)displayUpperAttributeCombo.getSelectedItem();
                nodeBarPainter.setDisplayAttribute(NodeShapePainter.UPPER_ATTRIBUTE, attribute);
            }
        });

        this.nodeBarPainter.addPainterListener(new PainterListener() {
            public void painterChanged() {

            }

            public void painterSettingsChanged() {
                displayLowerAttributeCombo.removeAllItems();
                displayUpperAttributeCombo.removeAllItems();
                for (String name : nodeBarPainter.getAttributes()) {
                    displayLowerAttributeCombo.addItem(name);
                    displayUpperAttributeCombo.addItem(name);
                }

                optionsPanel.repaint();
            }
        });

        optionsPanel.addComponentWithLabel("Lower:", displayLowerAttributeCombo);
        optionsPanel.addComponentWithLabel("Upper:", displayUpperAttributeCombo);
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
}
